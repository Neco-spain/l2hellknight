package l2m.gameserver.network.clientpackets;

import java.nio.ByteBuffer;
import l2p.commons.math.SafeMath;
import l2p.commons.util.Rnd;
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
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.network.serverpackets.SystemMessage2;
import l2m.gameserver.network.serverpackets.components.IStaticPacket;
import l2m.gameserver.templates.item.ItemTemplate;
import l2m.gameserver.templates.manor.CropProcure;
import org.apache.commons.lang3.ArrayUtils;

public class RequestProcureCropList extends L2GameClientPacket
{
  private int _count;
  private int[] _items;
  private int[] _crop;
  private int[] _manor;
  private long[] _itemQ;

  protected void readImpl()
  {
    _count = readD();
    if ((_count * 20 > _buf.remaining()) || (_count > 32767) || (_count < 1))
    {
      _count = 0;
      return;
    }
    _items = new int[_count];
    _crop = new int[_count];
    _manor = new int[_count];
    _itemQ = new long[_count];
    for (int i = 0; i < _count; i++)
    {
      _items[i] = readD();
      _crop[i] = readD();
      _manor[i] = readD();
      _itemQ[i] = readQ();
      if ((_crop[i] >= 1) && (_manor[i] >= 1) && (_itemQ[i] >= 1L) && (ArrayUtils.indexOf(_items, _items[i]) >= i))
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

    int currentManorId = manor == null ? 0 : manor.getCastle().getId();

    long totalFee = 0L;
    int slots = 0;
    long weight = 0L;
    try
    {
      for (int i = 0; i < _count; i++)
      {
        int objId = _items[i];
        int cropId = _crop[i];
        int manorId = _manor[i];
        long count = _itemQ[i];

        ItemInstance item = activeChar.getInventory().getItemByObjectId(objId);
        if ((item == null) || (item.getCount() < count) || (item.getItemId() != cropId)) {
          return;
        }
        Castle castle = (Castle)ResidenceHolder.getInstance().getResidence(Castle.class, manorId);
        if (castle == null) {
          return;
        }
        CropProcure crop = castle.getCrop(cropId, 0);
        if ((crop == null) || (crop.getId() == 0) || (crop.getPrice() == 0L)) {
          return;
        }
        if (count > crop.getAmount()) {
          return;
        }
        long price = SafeMath.mulAndCheck(count, crop.getPrice());
        long fee = 0L;
        if ((currentManorId != 0) && (manorId != currentManorId)) {
          fee = price * 5L / 100L;
        }
        totalFee = SafeMath.addAndCheck(totalFee, fee);

        int rewardItemId = Manor.getInstance().getRewardItem(cropId, crop.getReward());

        ItemTemplate template = ItemHolder.getInstance().getTemplate(rewardItemId);
        if (template == null) {
          return;
        }
        weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(count, template.getWeight()));
        if ((!template.isStackable()) || (activeChar.getInventory().getItemByItemId(cropId) == null)) {
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
      if (activeChar.getInventory().getAdena() < totalFee)
      {
        activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
        return;
      }
      for (int i = 0; i < _count; i++)
      {
        int objId = _items[i];
        int cropId = _crop[i];
        int manorId = _manor[i];
        long count = _itemQ[i];

        ItemInstance item = activeChar.getInventory().getItemByObjectId(objId);
        if ((item == null) || (item.getCount() < count) || (item.getItemId() != cropId)) {
          continue;
        }
        Castle castle = (Castle)ResidenceHolder.getInstance().getResidence(Castle.class, manorId);
        if (castle == null) {
          continue;
        }
        CropProcure crop = castle.getCrop(cropId, 0);
        if ((crop == null) || (crop.getId() == 0) || (crop.getPrice() == 0L)) {
          continue;
        }
        if (count > crop.getAmount()) {
          continue;
        }
        int rewardItemId = Manor.getInstance().getRewardItem(cropId, crop.getReward());
        long sellPrice = count * crop.getPrice();
        long rewardPrice = ItemHolder.getInstance().getTemplate(rewardItemId).getReferencePrice();

        if (rewardPrice == 0L) {
          continue;
        }
        double reward = sellPrice / rewardPrice;
        long rewardItemCount = ()reward + (Rnd.nextDouble() <= reward % 1.0D ? 1 : 0);

        if (rewardItemCount < 1L)
        {
          SystemMessage sm = new SystemMessage(1491);
          sm.addItemName(cropId);
          sm.addNumber(count);
          activeChar.sendPacket(sm);
        }
        else
        {
          long fee = 0L;
          if ((currentManorId != 0) && (manorId != currentManorId)) {
            fee = sellPrice * 5L / 100L;
          }
          if (!activeChar.getInventory().destroyItemByObjectId(objId, count)) {
            continue;
          }
          if (!activeChar.reduceAdena(fee, false))
          {
            SystemMessage sm = new SystemMessage(1491);
            sm.addItemName(cropId);
            sm.addNumber(count);
            activeChar.sendPacket(new IStaticPacket[] { sm, Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA });
          }
          else
          {
            crop.setAmount(crop.getAmount() - count);
            castle.updateCrop(crop.getId(), crop.getAmount(), 0);
            castle.addToTreasuryNoTax(fee, false, false);

            if (activeChar.getInventory().addItem(rewardItemId, rewardItemCount) == null) {
              continue;
            }
            activeChar.sendPacket(new IStaticPacket[] { new SystemMessage(1490).addItemName(cropId).addNumber(count), SystemMessage2.removeItems(cropId, count), SystemMessage2.obtainItems(rewardItemId, rewardItemCount, 0) });
            if (fee > 0L)
              activeChar.sendPacket(new SystemMessage(1607).addNumber(fee));
          }
        }
      }
    } finally {
      activeChar.getInventory().writeUnlock();
    }

    activeChar.sendChanges();
  }
}