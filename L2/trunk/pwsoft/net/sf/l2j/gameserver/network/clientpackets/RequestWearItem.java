package net.sf.l2j.gameserver.network.clientpackets;

import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.TradeController;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2TradeList;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2MercManagerInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MerchantInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.templates.L2Item;

public final class RequestWearItem extends L2GameClientPacket
{
  protected static final Logger _log = Logger.getLogger(RequestWearItem.class.getName());
  protected Future<?> _removeWearItemsTask;
  private int _unknow;
  private int _listId;
  private int _count;
  private int[] _items;
  protected L2PcInstance _activeChar;

  protected void readImpl()
  {
    _activeChar = ((L2GameClient)getClient()).getActiveChar();
    _unknow = readD();
    _listId = readD();
    _count = readD();

    if (_count < 0) _count = 0;
    if (_count > 100) _count = 0;

    _items = new int[_count];

    for (int i = 0; i < _count; i++)
    {
      int itemId = readD();
      _items[i] = itemId;
    }
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) return;

    if ((!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP) && (player.getKarma() > 0)) return;

    L2Object target = player.getTarget();
    if ((!player.isGM()) && ((target == null) || ((!(target instanceof L2MerchantInstance)) && (!(target instanceof L2MercManagerInstance))) || (!player.isInsideRadius(target, 150, false, false))))
    {
      return;
    }
    L2TradeList list = null;

    L2MerchantInstance merchant = (target != null) && ((target instanceof L2MerchantInstance)) ? (L2MerchantInstance)target : null;
    if (merchant == null)
    {
      return;
    }

    List lists = TradeController.getInstance().getBuyListByNpcId(merchant.getNpcId());

    if (lists == null)
    {
      return;
    }

    for (L2TradeList tradeList : lists)
    {
      if (tradeList.getListId() == _listId)
      {
        list = tradeList;
      }
    }

    if (list == null)
    {
      return;
    }

    _listId = list.getListId();

    if ((_count < 1) || (_listId >= 1000000))
    {
      player.sendActionFailed();
      return;
    }

    long totalPrice = 0L;

    int slots = 0;
    int weight = 0;

    for (int i = 0; i < _count; i++)
    {
      int itemId = _items[i];

      if (!list.containsItemId(itemId))
      {
        return;
      }

      L2Item template = ItemTable.getInstance().getTemplate(itemId);
      weight += template.getWeight();
      slots++;

      totalPrice += Config.WEAR_PRICE;
      if (totalPrice > 2147483647L)
      {
        return;
      }

    }

    if (!player.getInventory().validateWeight(weight))
    {
      sendPacket(Static.WEIGHT_LIMIT_EXCEEDED);
      return;
    }

    if (!player.getInventory().validateCapacity(slots))
    {
      sendPacket(Static.SLOTS_FULL);
      return;
    }

    if ((totalPrice < 0L) || (!player.reduceAdena("Wear", (int)totalPrice, player.getLastFolkNPC(), false)))
    {
      sendPacket(Static.YOU_NOT_ENOUGH_ADENA);
      return;
    }

    InventoryUpdate playerIU = new InventoryUpdate();
    for (int i = 0; i < _count; i++)
    {
      int itemId = _items[i];

      if (!list.containsItemId(itemId))
      {
        return;
      }

      L2ItemInstance item = player.getInventory().addWearItem("Wear", itemId, player, merchant);

      player.getInventory().equipItemAndRecord(item);

      playerIU.addItem(item);
    }

    player.sendPacket(playerIU);

    StatusUpdate su = new StatusUpdate(player.getObjectId());
    su.addAttribute(14, player.getCurrentLoad());
    player.sendPacket(su);

    player.broadcastUserInfo();

    if (_removeWearItemsTask == null)
      _removeWearItemsTask = ThreadPoolManager.getInstance().scheduleGeneral(new RemoveWearItemsTask(), Config.WEAR_DELAY * 1000);
  }

  public String getType()
  {
    return "C.WearItem";
  }

  class RemoveWearItemsTask
    implements Runnable
  {
    RemoveWearItemsTask()
    {
    }

    public void run()
    {
      try
      {
        _activeChar.destroyWearedItems("Wear", null, true);
      }
      catch (Throwable e) {
        RequestWearItem._log.log(Level.SEVERE, "", e);
      }
    }
  }
}