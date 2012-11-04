package l2m.gameserver.network.clientpackets;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import l2p.commons.math.SafeMath;
import l2m.gameserver.Config;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.data.xml.holder.ItemHolder;
import l2m.gameserver.data.xml.holder.ResidenceHolder;
import l2m.gameserver.model.GameObject;
import l2m.gameserver.model.Manor;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.entity.residence.Castle;
import l2m.gameserver.model.instances.ManorManagerInstance;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.items.PcInventory;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.SystemMessage2;
import l2m.gameserver.templates.item.ItemTemplate;
import l2m.gameserver.templates.manor.CropProcure;

public class RequestProcureCrop extends L2GameClientPacket
{
  private int _manorId;
  private int _count;
  private int[] _items;
  private long[] _itemQ;
  private List<CropProcure> _procureList = Collections.emptyList();

  protected void readImpl()
  {
    _manorId = readD();
    _count = readD();
    if ((_count * 16 > _buf.remaining()) || (_count > 32767) || (_count < 1))
    {
      _count = 0;
      return;
    }
    _items = new int[_count];
    _itemQ = new long[_count];
    for (int i = 0; i < _count; i++)
    {
      readD();
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
    int slots = 0;
    long weight = 0L;
    try
    {
      for (int i = 0; i < _count; i++)
      {
        int itemId = _items[i];
        long count = _itemQ[i];

        CropProcure crop = castle.getCrop(itemId, 0);
        if (crop == null) {
          return;
        }
        int rewradItemId = Manor.getInstance().getRewardItem(itemId, castle.getCrop(itemId, 0).getReward());
        long rewradItemCount = Manor.getInstance().getRewardAmountPerCrop(castle.getId(), itemId, castle.getCropRewardType(itemId));

        rewradItemCount = SafeMath.mulAndCheck(count, rewradItemCount);

        ItemTemplate template = ItemHolder.getInstance().getTemplate(rewradItemId);
        if (template == null) {
          return;
        }
        weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(count, template.getWeight()));
        if ((!template.isStackable()) || (activeChar.getInventory().getItemByItemId(itemId) == null)) {
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
      _procureList = castle.getCropProcure(0);

      for (int i = 0; i < _count; i++)
      {
        int itemId = _items[i];
        long count = _itemQ[i];

        int rewradItemId = Manor.getInstance().getRewardItem(itemId, castle.getCrop(itemId, 0).getReward());
        long rewradItemCount = Manor.getInstance().getRewardAmountPerCrop(castle.getId(), itemId, castle.getCropRewardType(itemId));

        rewradItemCount = SafeMath.mulAndCheck(count, rewradItemCount);

        if (!activeChar.getInventory().destroyItemByItemId(itemId, count)) {
          continue;
        }
        ItemInstance item = activeChar.getInventory().addItem(rewradItemId, rewradItemCount);
        if (item == null)
        {
          continue;
        }
        activeChar.sendPacket(SystemMessage2.obtainItems(rewradItemId, rewradItemCount, 0));
      }

    }
    catch (ArithmeticException ae)
    {
      _count = 0;
    }
    finally
    {
      activeChar.getInventory().writeUnlock();
    }

    activeChar.sendChanges();
  }
}