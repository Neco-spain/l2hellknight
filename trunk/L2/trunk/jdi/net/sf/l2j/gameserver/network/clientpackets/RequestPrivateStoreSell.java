package net.sf.l2j.gameserver.network.clientpackets;

import java.nio.ByteBuffer;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.ItemRequest;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.TradeList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.util.Util;

public final class RequestPrivateStoreSell extends L2GameClientPacket
{
  private static final String _C__96_REQUESTPRIVATESTORESELL = "[C] 96 RequestPrivateStoreSell";
  private static Logger _log = Logger.getLogger(RequestPrivateStoreSell.class.getName());
  private int _storePlayerId;
  private int _count;
  private int _price;
  private ItemRequest[] _items;

  protected void readImpl()
  {
    _storePlayerId = readD();
    _count = readD();

    if ((_count < 0) || (_count * 20 > _buf.remaining()) || (_count > Config.MAX_ITEM_IN_PACKET))
      _count = 0;
    _items = new ItemRequest[_count];

    long priceTotal = 0L;
    for (int i = 0; i < _count; i++)
    {
      int objectId = readD();
      int itemId = readD();
      readH();
      readH();
      long count = readD();
      int price = readD();

      if ((count > 2147483647L) || (count < 0L))
      {
        String msgErr = "[RequestPrivateStoreSell] player " + ((L2GameClient)getClient()).getActiveChar().getName() + " tried an overflow exploit, ban this player!";
        Util.handleIllegalPlayerAction(((L2GameClient)getClient()).getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
        _count = 0;
        _items = null;
        return;
      }
      _items[i] = new ItemRequest(objectId, itemId, (int)count, price);
      priceTotal += price * count;
    }

    if ((priceTotal < 0L) || (priceTotal > 2147483647L))
    {
      String msgErr = "[RequestPrivateStoreSell] player " + ((L2GameClient)getClient()).getActiveChar().getName() + " tried an overflow exploit, ban this player!";
      Util.handleIllegalPlayerAction(((L2GameClient)getClient()).getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
      _count = 0;
      _items = null;
      return;
    }

    _price = (int)priceTotal;
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) return;
    L2Object object = L2World.getInstance().findObject(_storePlayerId);
    if ((object == null) || (!(object instanceof L2PcInstance))) return;
    L2PcInstance storePlayer = (L2PcInstance)object;
    if (storePlayer.getPrivateStoreType() != 3) return;
    TradeList storeList = storePlayer.getBuyList();
    if (storeList == null) return;

    if ((Config.GM_DISABLE_TRANSACTION) && (player.getAccessLevel() >= Config.GM_TRANSACTION_MIN) && (player.getAccessLevel() <= Config.GM_TRANSACTION_MAX))
    {
      player.sendMessage("Transactions are disable for your Access Level");
      sendPacket(new ActionFailed());
      return;
    }

    if (storePlayer.getAdena() < _price)
    {
      sendPacket(new ActionFailed());
      storePlayer.sendMessage("You have not enough adena, canceling PrivateBuy.");
      storePlayer.setPrivateStoreType(0);
      storePlayer.broadcastUserInfo();
      return;
    }

    if (player.getInventory().getAdena() >= 2147483647)
    {
      player.sendPacket(new ActionFailed());
      return;
    }

    if (!storeList.PrivateStoreSell(player, _items, _price))
    {
      sendPacket(new ActionFailed());
      _log.warning("PrivateStore sell has failed due to invalid list or request. Player: " + player.getName() + ", Private store of: " + storePlayer.getName());
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
    return "[C] 96 RequestPrivateStoreSell";
  }
}