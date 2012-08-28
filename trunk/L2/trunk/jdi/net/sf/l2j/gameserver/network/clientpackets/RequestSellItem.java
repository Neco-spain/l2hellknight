package net.sf.l2j.gameserver.network.clientpackets;

import java.nio.ByteBuffer;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2FishermanInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MercManagerInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MerchantInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
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

public final class RequestSellItem extends L2GameClientPacket
{
  private static final String _C__1E_REQUESTSELLITEM = "[C] 1E RequestSellItem";
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
    if (player == null) return;

    if ((!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP) && (player.getKarma() > 0)) return;

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

    if (ok) {
      merchant = (L2NpcInstance)target;
    }
    if (_listId > 1000000)
    {
      if (merchant.getTemplate().npcId != _listId - 1000000)
      {
        sendPacket(new ActionFailed());
        return;
      }
    }

    long totalPrice = 0L;

    for (int i = 0; i < _count; i++)
    {
      int objectId = _items[(i * 3 + 0)];

      int itemId = _items[(i * 3 + 1)];
      int count = _items[(i * 3 + 2)];

      if ((count < 0) || (count > 2147483647))
      {
        Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + 2147483647 + " items at the same time.", Config.DEFAULT_PUNISH);
        SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
        sendPacket(sm);
        sm = null;
        return;
      }

      L2ItemInstance item = player.checkItemManipulation(objectId, count, "sell");
      if ((item == null) || (!item.getItem().isSellable()))
        continue;
      totalPrice += item.getReferencePrice() * count / 2;
      if (totalPrice > 2147483647L)
      {
        Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + 2147483647 + " adena worth of goods.", Config.DEFAULT_PUNISH);
        return;
      }

      item = player.getInventory().destroyItem("Sell", objectId, count, player, null);
    }

    player.addAdena("Sell", (int)totalPrice, merchant, false);

    String html = HtmCache.getInstance().getHtm("data/html/" + htmlFolder + "/" + merchant.getNpcId() + "-sold.htm");

    if (html != null)
    {
      NpcHtmlMessage soldMsg = new NpcHtmlMessage(merchant.getObjectId());
      soldMsg.setHtml(html.replaceAll("%objectId%", String.valueOf(merchant.getObjectId())));
      player.sendPacket(soldMsg);
    }

    StatusUpdate su = new StatusUpdate(player.getObjectId());
    su.addAttribute(14, player.getCurrentLoad());
    player.sendPacket(su);
    player.sendPacket(new ItemList(player, true));
  }

  public String getType()
  {
    return "[C] 1E RequestSellItem";
  }
}