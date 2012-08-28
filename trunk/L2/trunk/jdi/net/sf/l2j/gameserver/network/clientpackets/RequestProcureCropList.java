package net.sf.l2j.gameserver.network.clientpackets;

import java.nio.ByteBuffer;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.CastleManorManager.CropProcure;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Manor;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2ManorManagerInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.util.Util;

public class RequestProcureCropList extends L2GameClientPacket
{
  private static final String _C__D0_09_REQUESTPROCURECROPLIST = "[C] D0:09 RequestProcureCropList";
  private int _size;
  private int[] _items;

  protected void readImpl()
  {
    _size = readD();
    if ((_size * 16 > _buf.remaining()) || (_size > 500))
    {
      _size = 0;
      return;
    }
    _items = new int[_size * 4];
    for (int i = 0; i < _size; i++)
    {
      int objId = readD();
      _items[(i * 4 + 0)] = objId;
      int itemId = readD();
      _items[(i * 4 + 1)] = itemId;
      int manorId = readD();
      _items[(i * 4 + 2)] = manorId;
      long count = readD();
      if (count > 2147483647L) count = 2147483647L;
      _items[(i * 4 + 3)] = (int)count;
    }
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    L2Object target = player.getTarget();

    if (!(target instanceof L2ManorManagerInstance)) {
      target = player.getLastFolkNPC();
    }
    if ((!player.isGM()) && ((target == null) || (!(target instanceof L2ManorManagerInstance)) || (!player.isInsideRadius(target, 150, false, false))))
    {
      return;
    }
    if (_size < 1)
    {
      sendPacket(new ActionFailed());
      return;
    }
    L2ManorManagerInstance manorManager = (L2ManorManagerInstance)target;

    int currentManorId = manorManager.getCastle().getCastleId();

    int slots = 0;
    int weight = 0;

    for (int i = 0; i < _size; i++)
    {
      int itemId = _items[(i * 4 + 1)];
      int manorId = _items[(i * 4 + 2)];
      int count = _items[(i * 4 + 3)];

      if ((itemId == 0) || (manorId == 0) || (count == 0))
        continue;
      if (count < 1)
        continue;
      if (count > 2147483647)
      {
        Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + 2147483647 + " items at the same time.", Config.DEFAULT_PUNISH);

        SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);

        sendPacket(sm);
        return;
      }

      try
      {
        CastleManorManager.CropProcure crop = CastleManager.getInstance().getCastleById(manorId).getCrop(itemId, 0);
        int rewardItemId = L2Manor.getInstance().getRewardItem(itemId, crop.getReward());
        L2Item template = ItemTable.getInstance().getTemplate(rewardItemId);
        weight += count * template.getWeight();

        if (!template.isStackable())
          slots += count;
        else if (player.getInventory().getItemByItemId(itemId) == null) {
          slots++;
        }
      }
      catch (NullPointerException e)
      {
      }
    }

    if (!player.getInventory().validateWeight(weight))
    {
      sendPacket(new SystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED));
      return;
    }

    if (!player.getInventory().validateCapacity(slots))
    {
      sendPacket(new SystemMessage(SystemMessageId.SLOTS_FULL));
      return;
    }

    InventoryUpdate playerIU = new InventoryUpdate();

    for (int i = 0; i < _size; i++)
    {
      int objId = _items[(i * 4 + 0)];
      int cropId = _items[(i * 4 + 1)];
      int manorId = _items[(i * 4 + 2)];
      int count = _items[(i * 4 + 3)];

      if ((objId == 0) || (cropId == 0) || (manorId == 0) || (count == 0)) {
        continue;
      }
      if (count < 1) {
        continue;
      }
      CastleManorManager.CropProcure crop = null;
      try
      {
        crop = CastleManager.getInstance().getCastleById(manorId).getCrop(cropId, 0);
      }
      catch (NullPointerException e)
      {
        continue;
      }
      if ((crop == null) || (crop.getId() == 0) || (crop.getPrice() == 0)) {
        continue;
      }
      int fee = 0;

      int rewardItem = L2Manor.getInstance().getRewardItem(cropId, crop.getReward());

      if (count > crop.getAmount()) {
        continue;
      }
      int sellPrice = count * L2Manor.getInstance().getCropBasicPrice(cropId);

      int rewardPrice = ItemTable.getInstance().getTemplate(rewardItem).getReferencePrice();

      if (rewardPrice == 0) {
        continue;
      }
      int rewardItemCount = sellPrice / rewardPrice;
      if (rewardItemCount < 1)
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.FAILED_IN_TRADING_S2_OF_CROP_S1);
        sm.addItemName(cropId);
        sm.addNumber(count);
        player.sendPacket(sm);
      }
      else
      {
        if (manorId != currentManorId) {
          fee = sellPrice * 5 / 100;
        }
        if (player.getInventory().getAdena() < fee)
        {
          SystemMessage sm = new SystemMessage(SystemMessageId.FAILED_IN_TRADING_S2_OF_CROP_S1);
          sm.addItemName(cropId);
          sm.addNumber(count);
          player.sendPacket(sm);
          sm = new SystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
          player.sendPacket(sm);
        }
        else
        {
          L2ItemInstance itemDel = null;
          L2ItemInstance itemAdd = null;
          if (player.getInventory().getItemByObjectId(objId) == null) {
            continue;
          }
          L2ItemInstance item = player.getInventory().getItemByObjectId(objId);
          if (item.getCount() < count) {
            continue;
          }
          itemDel = player.getInventory().destroyItem("Manor", objId, count, player, manorManager);
          if (itemDel == null)
            continue;
          if (fee > 0)
            player.getInventory().reduceAdena("Manor", fee, player, manorManager);
          crop.setAmount(crop.getAmount() - count);
          if (Config.ALT_MANOR_SAVE_ALL_ACTIONS)
            CastleManager.getInstance().getCastleById(manorId).updateCrop(crop.getId(), crop.getAmount(), 0);
          itemAdd = player.getInventory().addItem("Manor", rewardItem, rewardItemCount, player, manorManager);

          if ((itemDel == null) || (itemAdd == null))
          {
            continue;
          }
          playerIU.addRemovedItem(itemDel);
          if (itemAdd.getCount() > rewardItemCount)
            playerIU.addModifiedItem(itemAdd);
          else {
            playerIU.addNewItem(itemAdd);
          }

          SystemMessage sm = new SystemMessage(SystemMessageId.TRADED_S2_OF_CROP_S1);

          sm.addItemName(cropId);
          sm.addNumber(count);
          player.sendPacket(sm);

          if (fee > 0)
          {
            sm = new SystemMessage(SystemMessageId.S1_ADENA_HAS_BEEN_WITHDRAWN_TO_PAY_FOR_PURCHASING_FEES);
            sm.addNumber(fee);
            player.sendPacket(sm);
          }

          sm = new SystemMessage(SystemMessageId.DISSAPEARED_ITEM);
          sm.addItemName(cropId);
          sm.addNumber(count);
          player.sendPacket(sm);

          if (fee > 0)
          {
            sm = new SystemMessage(SystemMessageId.DISSAPEARED_ADENA);
            sm.addNumber(fee);
            player.sendPacket(sm);
          }

          sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
          sm.addItemName(rewardItem);
          sm.addNumber(rewardItemCount);
          player.sendPacket(sm);
        }
      }
    }
    player.sendPacket(playerIU);

    StatusUpdate su = new StatusUpdate(player.getObjectId());
    su.addAttribute(14, player.getCurrentLoad());
    player.sendPacket(su);
  }

  public String getType()
  {
    return "[C] D0:09 RequestProcureCropList";
  }
}