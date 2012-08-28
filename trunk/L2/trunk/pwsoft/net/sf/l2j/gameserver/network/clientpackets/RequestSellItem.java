package net.sf.l2j.gameserver.network.clientpackets;

import java.nio.ByteBuffer;
import javolution.text.TextBuilder;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2FishermanInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MercManagerInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MerchantInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.EnchantResult;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.Log;

public final class RequestSellItem extends L2GameClientPacket
{
  private int _listId;
  private int _count;
  private int[] _items;

  protected void readImpl()
  {
    _listId = readD();
    _count = readD();
    if ((_count <= 0) || (_count * 12 > _buf.remaining()) || (_count > Config.MAX_ITEM_IN_PACKET))
    {
      _count = 0; _items = null;
      return;
    }
    _items = new int[_count * 3];
    for (int i = 0; i < _count; i++)
    {
      int objectId = readD(); _items[(i * 3 + 0)] = objectId;
      int itemId = readD(); _items[(i * 3 + 1)] = itemId;
      long cnt = readD();
      if ((cnt > 2147483647L) || (cnt <= 0L))
      {
        _count = 0; _items = null;
        return;
      }
      _items[(i * 3 + 2)] = (int)cnt;
    }
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    if (System.currentTimeMillis() - player.gCPAW() < 100L)
    {
      player.sendActionFailed();
      return;
    }

    player.sCPAW();

    L2Object target = player.getTarget();
    if ((!player.isGM()) && ((target == null) || ((!(target instanceof L2MerchantInstance)) && (!(target instanceof L2MercManagerInstance))) || (!player.isInsideRadius(target, 150, false, false))))
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
      else
        ok = false;
    }
    else {
      ok = false;
    }
    L2NpcInstance merchant = null;

    if (!ok) {
      return;
    }
    merchant = (L2NpcInstance)target;
    if (!player.isInsideRadius(merchant, 120, false, false)) {
      return;
    }
    if (_listId > 1000000)
    {
      if (merchant.getTemplate().npcId != _listId - 1000000)
      {
        player.sendActionFailed();
        return;
      }
    }

    String date = "";
    TextBuilder tb = null;
    if (Config.LOG_ITEMS)
    {
      date = Log.getTime();
      tb = new TextBuilder();
    }

    long totalPrice = 0L;

    for (int i = 0; i < _count; i++)
    {
      int objectId = _items[(i * 3 + 0)];

      int itemId = _items[(i * 3 + 1)];
      int count = _items[(i * 3 + 2)];

      if ((count < 0) || (count > 2147483647))
      {
        sendPacket(Static.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
        return;
      }

      if (player.getActiveEnchantItem() != null)
      {
        player.setActiveEnchantItem(null);
        player.sendPacket(new EnchantResult(0, true));
        player.sendActionFailed();
        return;
      }

      if (player.getActiveWarehouse() != null)
      {
        player.cancelActiveWarehouse();
        player.sendActionFailed();
        return;
      }

      if (player.getActiveTradeList() != null)
      {
        player.cancelActiveTrade();
        player.sendActionFailed();
        return;
      }

      L2ItemInstance item = player.checkItemManipulation(objectId, count, "sell");
      if ((item == null) || (!item.getItem().isSellable()))
        continue;
      totalPrice += item.getReferencePrice() * count / 2;
      if (totalPrice > 2147483647L)
      {
        return;
      }

      player.getInventory().destroyItem("Sell", objectId, count, player, null);
      if ((!Config.LOG_ITEMS) || (item == null))
        continue;
      String act = "SELL " + item.getItemName() + "(" + count + ")(" + item.getObjectId() + ")(npc:" + merchant.getTemplate().npcId + ") #(player " + player.getName() + ", account: " + player.getAccountName() + ", ip: " + player.getIP() + ", hwid: " + player.getHWID() + ")";
      tb.append(date + act + "\n");
    }

    if ((Config.LOG_ITEMS) && (tb != null))
    {
      Log.item(tb.toString(), 5);
      tb.clear();
      tb = null;
    }

    player.addAdena("Sell", (int)totalPrice, merchant, false);
    player.broadcastUserInfo();

    String html = HtmCache.getInstance().getHtm("data/html/" + htmlFolder + "/" + merchant.getNpcId() + "-sold.htm");

    if (html != null)
    {
      NpcHtmlMessage soldMsg = NpcHtmlMessage.id(merchant.getObjectId());
      soldMsg.setHtml(html.replaceAll("%objectId%", String.valueOf(merchant.getObjectId())));
      player.sendPacket(soldMsg);
    }

    player.sendChanges();
    player.sendItems(true);
  }
}