package l2p.gameserver.clientpackets;

import java.nio.ByteBuffer;
import l2p.commons.math.SafeMath;
import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.data.xml.holder.ItemHolder;
import l2p.gameserver.data.xml.holder.ResidenceHolder;
import l2p.gameserver.model.GameObject;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.residence.Castle;
import l2p.gameserver.model.instances.ManorManagerInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.SystemMessage2;
import l2p.gameserver.templates.item.ItemTemplate;
import l2p.gameserver.templates.manor.SeedProduction;

public class RequestBuySeed extends L2GameClientPacket
{
  private int _count;
  private int _manorId;
  private int[] _items;
  private long[] _itemQ;

  protected void readImpl()
  {
    _manorId = readD();
    _count = readD();

    if ((_count * 12 > _buf.remaining()) || (_count > 32767) || (_count < 1))
    {
      _count = 0;
      return;
    }

    _items = new int[_count];
    _itemQ = new long[_count];

    for (int i = 0; i < _count; i++)
    {
      _items[i] = readD();
      _itemQ[i] = readQ();
      if (_itemQ[i] >= 1L)
        continue;
      _count = 0;
      return;
    }
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if ((activeChar == null) || (_count == 0)) {
      return;
    }
    if (activeChar.isActionsDisabled())
    {
      activeChar.sendActionFailed();
      return;
    }

    if (activeChar.isInStoreMode())
    {
      activeChar.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
      return;
    }

    if (activeChar.isInTrade())
    {
      activeChar.sendActionFailed();
      return;
    }

    if (activeChar.isFishing())
    {
      activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
      return;
    }

    if ((!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP) && (activeChar.getKarma() > 0) && (!activeChar.isGM()))
    {
      activeChar.sendActionFailed();
      return;
    }

    GameObject target = activeChar.getTarget();

    ManorManagerInstance manor = (target != null) && ((target instanceof ManorManagerInstance)) ? (ManorManagerInstance)target : null;
    if ((!activeChar.isGM()) && ((manor == null) || (!activeChar.isInRange(manor, 200L))))
    {
      activeChar.sendActionFailed();
      return;
    }

    Castle castle = (Castle)ResidenceHolder.getInstance().getResidence(Castle.class, _manorId);
    if (castle == null) {
      return;
    }
    long totalPrice = 0L;
    int slots = 0;
    long weight = 0L;
    try
    {
      for (int i = 0; i < _count; i++)
      {
        int seedId = _items[i];
        long count = _itemQ[i];
        long price = 0L;
        long residual = 0L;

        SeedProduction seed = castle.getSeed(seedId, 0);
        price = seed.getPrice();
        residual = seed.getCanProduce();

        if (price < 1L) {
          return;
        }
        if (residual < count) {
          return;
        }
        totalPrice = SafeMath.addAndCheck(totalPrice, SafeMath.mulAndCheck(count, price));

        ItemTemplate item = ItemHolder.getInstance().getTemplate(seedId);
        if (item == null) {
          return;
        }
        weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(count, item.getWeight()));
        if ((!item.isStackable()) || (activeChar.getInventory().getItemByItemId(seedId) == null)) {
          slots++;
        }
      }
    }
    catch (ArithmeticException ae)
    {
      sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
      return;
    }

    activeChar.getInventory().writeLock();
    try
    {
      if (!activeChar.getInventory().validateWeight(weight)) {
        sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
        return;
      }
      if (!activeChar.getInventory().validateCapacity(slots)) {
        sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
        return;
      }
      if (!activeChar.reduceAdena(totalPrice, true))
      {
        sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
        return;
      }
      castle.addToTreasuryNoTax(totalPrice, false, true);

      for (int i = 0; i < _count; i++)
      {
        int seedId = _items[i];
        long count = _itemQ[i];

        SeedProduction seed = castle.getSeed(seedId, 0);
        seed.setCanProduce(seed.getCanProduce() - count);
        castle.updateSeed(seed.getId(), seed.getCanProduce(), 0);

        activeChar.getInventory().addItem(seedId, count);
        activeChar.sendPacket(SystemMessage2.obtainItems(seedId, count, 0));
      }
    }
    finally
    {
      activeChar.getInventory().writeUnlock();
    }

    activeChar.sendChanges();
  }
}