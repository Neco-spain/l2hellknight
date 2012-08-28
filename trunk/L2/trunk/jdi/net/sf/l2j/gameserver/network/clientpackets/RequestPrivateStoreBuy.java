package net.sf.l2j.gameserver.network.clientpackets;

import java.nio.ByteBuffer;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.ItemRequest;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.TradeList;
import net.sf.l2j.gameserver.model.TradeList.TradeItem;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.Util;

public final class RequestPrivateStoreBuy extends L2GameClientPacket
{
  private static final String _C__79_REQUESTPRIVATESTOREBUY = "[C] 79 RequestPrivateStoreBuy";
  private static Logger _log = Logger.getLogger(RequestPrivateStoreBuy.class.getName());
  private int _storePlayerId;
  private int _count;
  private ItemRequest[] _items;

  protected void readImpl()
  {
    _storePlayerId = readD();
    _count = readD();

    if ((_count < 0) || (_count * 12 > _buf.remaining()) || (_count > Config.MAX_ITEM_IN_PACKET))
    {
      _count = 0;
    }
    _items = new ItemRequest[_count];

    for (int i = 0; i < _count; i++)
    {
      int objectId = readD();
      long count = readD();
      if (count > 2147483647L) count = 2147483647L;
      int price = readD();

      _items[i] = new ItemRequest(objectId, (int)count, price);
    }
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) return;

    L2Object object = L2World.getInstance().findObject(_storePlayerId);
    if ((object == null) || (!(object instanceof L2PcInstance))) return;

    L2PcInstance storePlayer = (L2PcInstance)object;
    if ((storePlayer.getPrivateStoreType() != 1) && (storePlayer.getPrivateStoreType() != 8)) return;

    TradeList storeList = storePlayer.getSellList();
    if (storeList == null) return;

    if ((Config.GM_DISABLE_TRANSACTION) && (player.getAccessLevel() >= Config.GM_TRANSACTION_MIN) && (player.getAccessLevel() <= Config.GM_TRANSACTION_MAX))
    {
      player.sendMessage("Transactions are disable for your Access Level");
      sendPacket(new ActionFailed());
      return;
    }

    long priceTotal = 0L;
    for (ItemRequest ir : _items)
    {
      if ((ir.getCount() > 2147483647) || (ir.getCount() < 0))
      {
        String msgErr = "[RequestPrivateStoreBuy] player " + ((L2GameClient)getClient()).getActiveChar().getName() + " tried an overflow exploit, ban this player!";
        Util.handleIllegalPlayerAction(((L2GameClient)getClient()).getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
        return;
      }
      TradeList.TradeItem sellersItem = storeList.getItem(ir.getObjectId());
      if (sellersItem == null)
      {
        String msgErr = "[RequestPrivateStoreBuy] player " + ((L2GameClient)getClient()).getActiveChar().getName() + " tried to buy an item not sold in a private store (buy), ban this player!";
        Util.handleIllegalPlayerAction(((L2GameClient)getClient()).getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
        return;
      }
      if (ir.getPrice() != sellersItem.getPrice())
      {
        String msgErr = "[RequestPrivateStoreBuy] player " + ((L2GameClient)getClient()).getActiveChar().getName() + " tried to change the seller's price in a private store (buy), ban this player!";
        Util.handleIllegalPlayerAction(((L2GameClient)getClient()).getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
        return;
      }
      priceTotal += ir.getPrice() * ir.getCount();
    }

    if ((priceTotal < 0L) || (priceTotal > 2147483647L))
    {
      String msgErr = "[RequestPrivateStoreBuy] player " + ((L2GameClient)getClient()).getActiveChar().getName() + " tried an overflow exploit, ban this player!";
      Util.handleIllegalPlayerAction(((L2GameClient)getClient()).getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
      return;
    }

    if (player.getAdena() < priceTotal)
    {
      sendPacket(new SystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
      sendPacket(new ActionFailed());
      return;
    }

    if (storePlayer.getPrivateStoreType() == 8)
    {
      if (storeList.getItemCount() > _count)
      {
        String msgErr = "[RequestPrivateStoreBuy] player " + ((L2GameClient)getClient()).getActiveChar().getName() + " tried to buy less items then sold by package-sell, ban this player for bot-usage!";
        Util.handleIllegalPlayerAction(((L2GameClient)getClient()).getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
        return;
      }
    }

    if (!storeList.PrivateStoreBuy(player, _items, (int)priceTotal))
    {
      sendPacket(new ActionFailed());
      _log.warning("PrivateStore buy has failed due to invalid list or request. Player: " + player.getName() + ", Private store of: " + storePlayer.getName());
      return;
    }

    if (storeList.getItemCount() == 0)
    {
      storePlayer.setPrivateStoreType(0);
      storePlayer.broadcastUserInfo();
    }
  }

  public String getType()
  {
    return "[C] 79 RequestPrivateStoreBuy";
  }
}