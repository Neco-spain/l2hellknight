package net.sf.l2j.gameserver.network.clientpackets;

import java.nio.ByteBuffer;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.ItemRequest;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.TradeList;
import net.sf.l2j.gameserver.model.TradeList.TradeItem;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class RequestPrivateStoreBuy extends L2GameClientPacket
{
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
    if (player == null) {
      return;
    }
    if (player.isParalyzed()) {
      return;
    }
    L2Object object = L2World.getInstance().findObject(_storePlayerId);
    if ((object == null) || (!object.isPlayer())) return;

    L2PcInstance storePlayer = object.getPlayer();
    if ((storePlayer.getPrivateStoreType() != 1) && (storePlayer.getPrivateStoreType() != 8)) return;

    TradeList storeList = storePlayer.getSellList();
    if (storeList == null) return;

    if (!player.isInsideRadius(storePlayer, 120, false, false)) {
      return;
    }

    long priceTotal = 0L;
    for (ItemRequest ir : _items)
    {
      if ((ir.getCount() > 2147483647) || (ir.getCount() < 0))
      {
        return;
      }
      TradeList.TradeItem sellersItem = storeList.getItem(ir.getObjectId());
      if (sellersItem == null)
      {
        return;
      }
      if (ir.getPrice() != sellersItem.getPrice())
      {
        return;
      }
      priceTotal += ir.getPrice() * ir.getCount();
    }

    if ((priceTotal < 0L) || (priceTotal > 2147483647L))
    {
      return;
    }

    if (player.getAdena() < priceTotal)
    {
      sendPacket(Static.YOU_NOT_ENOUGH_ADENA);
      player.sendActionFailed();
      return;
    }

    if (storePlayer.getPrivateStoreType() == 8)
    {
      if (storeList.getItemCount() > _count)
      {
        return;
      }
    }

    if (!storeList.PrivateStoreBuy(player, _items, (int)priceTotal))
    {
      player.sendActionFailed();
      _log.warning("PrivateStore buy has failed due to invalid list or request. Player: " + player.getName() + ", Private store of: " + storePlayer.getName());
      storePlayer.logout();
      return;
    }

    player.sendChanges();
    storePlayer.saveTradeList();
    if (storeList.getItemCount() == 0)
    {
      if (storePlayer.isInOfflineMode())
      {
        storePlayer.kick();
        return;
      }
      storePlayer.setPrivateStoreType(0);
      storePlayer.broadcastUserInfo();
    }
    storePlayer.sendChanges();
    player.sendActionFailed();
  }
}