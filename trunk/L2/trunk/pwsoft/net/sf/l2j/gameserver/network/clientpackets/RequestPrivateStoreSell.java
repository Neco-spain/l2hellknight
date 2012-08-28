package net.sf.l2j.gameserver.network.clientpackets;

import java.nio.ByteBuffer;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.ItemRequest;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.TradeList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class RequestPrivateStoreSell extends L2GameClientPacket
{
  private static final Logger _log = Logger.getLogger(RequestPrivateStoreSell.class.getName());
  private int _storePlayerId;
  private int _count;
  private int _price;
  private ItemRequest[] _items;

  protected void readImpl()
  {
    _storePlayerId = readD();
    _count = readD();

    if ((_count < 0) || (_count * 20 > _buf.remaining()) || (_count > Config.MAX_ITEM_IN_PACKET)) {
      _count = 0;
    }
    _items = new ItemRequest[_count];

    long priceTotal = 0L;
    for (int i = 0; i < _count; i++) {
      int objectId = readD();
      int itemId = readD();
      readH();
      readH();
      long count = readD();
      int price = readD();

      if ((count > 2147483647L) || (count < 0L))
      {
        _count = 0;
        _items = null;
        return;
      }
      _items[i] = new ItemRequest(objectId, itemId, (int)count, price);
      priceTotal += price * count;
    }

    if ((priceTotal < 0L) || (priceTotal > 2147483647L))
    {
      _count = 0;
      _items = null;
      return;
    }

    _price = (int)priceTotal;
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }

    if (player.isParalyzed()) {
      return;
    }

    L2PcInstance storePlayer = L2World.getInstance().getPlayer(_storePlayerId);
    if (storePlayer == null) {
      return;
    }
    if (storePlayer.getPrivateStoreType() != 3) {
      return;
    }
    TradeList storeList = storePlayer.getBuyList();
    if (storeList == null) {
      return;
    }

    if (!player.isInsideRadius(storePlayer, 120, false, false)) {
      return;
    }

    if (storePlayer.getAdena() < _price) {
      storePlayer.sendActionFailed();
      storePlayer.sendMessage("You have not enough adena, canceling PrivateBuy.");
      storePlayer.setPrivateStoreType(0);
      storePlayer.broadcastUserInfo();
      return;
    }

    if (!storeList.PrivateStoreSell(player, _items, _price))
    {
      _log.warning("PrivateStore sell has failed due to invalid list or request. Player: " + player.getName() + ", Private store of: " + storePlayer.getName());
      player.kick();
      return;
    }

    storePlayer.saveTradeList();
    if (storeList.getItemCount() == 0) {
      if (storePlayer.isInOfflineMode()) {
        storePlayer.kick();
        return;
      }
      storePlayer.setPrivateStoreType(0);
      storePlayer.broadcastUserInfo();
    }
  }
}