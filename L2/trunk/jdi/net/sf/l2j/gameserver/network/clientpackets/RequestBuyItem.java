package net.sf.l2j.gameserver.network.clientpackets;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.TradeController;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2TradeList;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2CastleChamberlainInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2ClanHallManagerInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2FishermanInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MercManagerInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MerchantInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.util.Util;

public final class RequestBuyItem extends L2GameClientPacket
{
  private static final String _C__1F_REQUESTBUYITEM = "[C] 1F RequestBuyItem";
  private static Logger _log = Logger.getLogger(RequestBuyItem.class.getName());
  private int _listId;
  private int _count;
  private int[] _items;

  protected void readImpl()
  {
    _listId = readD();
    _count = readD();

    if ((_count * 2 < 0) || (_count * 8 > _buf.remaining()) || (_count > Config.MAX_ITEM_IN_PACKET)) _count = 0;

    _items = new int[_count * 2];
    for (int i = 0; i < _count; i++)
    {
      int itemId = readD(); _items[(i * 2 + 0)] = itemId;
      long cnt = readD();
      if ((cnt > 2147483647L) || (cnt < 0L))
      {
        _count = 0; _items = null;
        return;
      }
      _items[(i * 2 + 1)] = (int)cnt;
    }
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) return;

    if ((!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP) && (player.getKarma() > 0)) return;

    L2Object target = player.getTarget();
    if ((!player.isGM()) && ((target == null) || ((!(target instanceof L2MerchantInstance)) && (!(target instanceof L2FishermanInstance)) && (!(target instanceof L2MercManagerInstance)) && (!(target instanceof L2ClanHallManagerInstance)) && (!(target instanceof L2CastleChamberlainInstance))) || (!player.isInsideRadius(target, 150, false, false))))
    {
      return;
    }
    boolean ok = true;
    String htmlFolder = "";

    if (target != null)
    {
      if ((target instanceof L2MerchantInstance))
        htmlFolder = "merchant";
      else if ((target instanceof L2FishermanInstance))
        htmlFolder = "fisherman";
      else if ((target instanceof L2MercManagerInstance))
        ok = true;
      else if ((target instanceof L2ClanHallManagerInstance))
        ok = true;
      else if ((target instanceof L2CastleChamberlainInstance))
        ok = true;
      else
        ok = false;
    }
    else {
      ok = false;
    }
    L2NpcInstance merchant = null;

    if (ok) {
      merchant = (L2NpcInstance)target;
    } else if ((!ok) && (!player.isGM()))
    {
      player.sendMessage("Invalid Target: Seller must be targetted");
      return;
    }

    L2TradeList list = null;

    if (merchant != null)
    {
      List lists = TradeController.getInstance().getBuyListByNpcId(merchant.getNpcId());

      if (!player.isGM())
      {
        if (lists == null)
        {
          Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false BuyList list_id.", Config.DEFAULT_PUNISH);
          return;
        }
        for (L2TradeList tradeList : lists)
        {
          if (tradeList.getListId() == _listId)
          {
            list = tradeList;
          }
        }
      }
      else
      {
        list = TradeController.getInstance().getBuyList(_listId);
      }
    }
    else {
      list = TradeController.getInstance().getBuyList(_listId);
    }if (list == null)
    {
      Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false BuyList list_id.", Config.DEFAULT_PUNISH);
      return;
    }

    _listId = list.getListId();

    if (_listId > 1000000)
    {
      if ((merchant != null) && (merchant.getTemplate().npcId != _listId - 1000000))
      {
        sendPacket(new ActionFailed());
        return;
      }
    }
    if (_count < 1)
    {
      sendPacket(new ActionFailed());
      return;
    }
    double taxRate = 0.0D;
    if ((merchant != null) && (merchant.getIsInTown())) taxRate = merchant.getCastle().getTaxRate();
    long subTotal = 0L;
    int tax = 0;

    long slots = 0L;
    long weight = 0L;
    for (int i = 0; i < _count; i++)
    {
      int itemId = _items[(i * 2 + 0)];
      int count = _items[(i * 2 + 1)];
      int price = -1;

      if (!list.containsItemId(itemId))
      {
        Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false BuyList list_id.", Config.DEFAULT_PUNISH);
        return;
      }

      L2Item template = ItemTable.getInstance().getTemplate(itemId);

      if (template != null) {
        if ((count > 2147483647) || ((!template.isStackable()) && (count > 1)))
        {
          Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase invalid quantity of items at the same time.", Config.DEFAULT_PUNISH);
          SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
          sendPacket(sm);
          sm = null;

          return;
        }

        if (_listId < 1000000)
        {
          price = list.getPriceForItemId(itemId);
          if ((itemId >= 3960) && (itemId <= 4026)) price = (int)(price * Config.RATE_SIEGE_GUARDS_PRICE);

        }

        if (price < 0)
        {
          _log.warning("ERROR, no price found .. wrong buylist ??");
          sendPacket(new ActionFailed());
          return;
        }

        if ((price == 0) && (!player.isGM()) && (Config.ONLY_GM_ITEMS_FREE))
        {
          player.sendMessage("Ohh Cheat dont work? You have a problem now!");
          Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried buy item for 0 adena.", Config.DEFAULT_PUNISH);
          return;
        }

        subTotal += count * price;
        tax = (int)(subTotal * taxRate);
        if (subTotal + tax > 2147483647L)
        {
          Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + 2147483647 + " adena worth of goods.", Config.DEFAULT_PUNISH);
          return;
        }

        weight += count * template.getWeight();
        if (!template.isStackable()) { slots += count; } else {
          if (player.getInventory().getItemByItemId(itemId) != null) continue; slots += 1L;
        }
      }
    }
    if ((weight > 2147483647L) || (weight < 0L) || (!player.getInventory().validateWeight((int)weight)))
    {
      sendPacket(new SystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED));
      return;
    }

    if ((slots > 2147483647L) || (slots < 0L) || (!player.getInventory().validateCapacity((int)slots)))
    {
      sendPacket(new SystemMessage(SystemMessageId.SLOTS_FULL));
      return;
    }

    if ((subTotal < 0L) || (!player.reduceAdena("Buy", (int)(subTotal + tax), player.getLastFolkNPC(), false)))
    {
      sendPacket(new SystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
      return;
    }

    if ((merchant != null) && (merchant.getIsInTown()) && (merchant.getCastle().getOwnerId() > 0)) {
      merchant.getCastle().addToTreasury(tax);
    }

    for (int i = 0; i < _count; i++)
    {
      int itemId = _items[(i * 2 + 0)];
      int count = _items[(i * 2 + 1)];
      if (count < 0) count = 0;

      if (!list.containsItemId(itemId))
      {
        Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false BuyList list_id.", Config.DEFAULT_PUNISH);
        return;
      }

      if (list.countDecrease(itemId))
      {
        if (!list.decreaseCount(itemId, count)) {
          return;
        }

      }

      player.getInventory().addItem("Buy", itemId, count, player, merchant);
    }

    if (merchant != null)
    {
      String html = HtmCache.getInstance().getHtm("data/html/" + htmlFolder + "/" + merchant.getNpcId() + "-bought.htm");

      if (html != null)
      {
        NpcHtmlMessage boughtMsg = new NpcHtmlMessage(merchant.getObjectId());
        boughtMsg.setHtml(html.replaceAll("%objectId%", String.valueOf(merchant.getObjectId())));
        player.sendPacket(boughtMsg);
      }
    }

    StatusUpdate su = new StatusUpdate(player.getObjectId());
    su.addAttribute(14, player.getCurrentLoad());
    player.sendPacket(su);
    player.sendPacket(new ItemList(player, true));
  }

  public String getType()
  {
    return "[C] 1F RequestBuyItem";
  }
}