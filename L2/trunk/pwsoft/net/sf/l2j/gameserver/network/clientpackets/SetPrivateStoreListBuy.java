package net.sf.l2j.gameserver.network.clientpackets;

import java.nio.ByteBuffer;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.TradeList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.PrivateStoreManageListBuy;
import net.sf.l2j.gameserver.network.serverpackets.PrivateStoreMsgBuy;

public final class SetPrivateStoreListBuy extends L2GameClientPacket
{
  private int _count;
  private int[] _items;

  protected void readImpl()
  {
    _count = readD();
    if ((_count <= 0) || (_count * 12 > _buf.remaining()) || (_count > Config.MAX_ITEM_IN_PACKET))
    {
      _count = 0;
      _items = null;
      return;
    }
    _items = new int[_count * 4];
    for (int x = 0; x < _count; x++)
    {
      int itemId = readD();
      _items[(x * 3 + 0)] = itemId;
      int enchLvl = readH();
      _items[(x * 3 + 1)] = enchLvl;
      readH();
      long cnt = readD();
      if ((cnt > 2147483647L) || (cnt < 0L))
      {
        _count = 0; _items = null;
        return;
      }
      _items[(x * 3 + 2)] = (int)cnt;
      int price = readD();
      _items[(x * 3 + 3)] = price;
    }
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) return;

    TradeList tradeList = player.getBuyList();
    tradeList.clear();

    int cost = 0;
    for (int i = 0; i < _count; i++)
    {
      int itemId = _items[(i * 3 + 0)];
      int enchLvl = _items[(i * 3 + 1)];
      int count = _items[(i * 3 + 2)];
      int price = _items[(i * 3 + 3)];

      tradeList.addItemByItemId(itemId, count, price, enchLvl);
      cost += count * price;
    }

    if ((_count <= 0) || (player.getChannel() > 1))
    {
      player.setPrivateStoreType(0);
      player.broadcastUserInfo();
      return;
    }

    if (_count > player.getPrivateBuyStoreLimit())
    {
      player.sendPacket(new PrivateStoreManageListBuy(player));
      player.sendPacket(Static.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
      return;
    }

    if ((cost > player.getAdena()) || (cost <= 0))
    {
      player.sendPacket(new PrivateStoreManageListBuy(player));
      player.sendPacket(Static.THE_PURCHASE_PRICE_IS_HIGHER_THAN_MONEY);
      return;
    }

    if (!player.canTrade())
    {
      player.sendActionFailed();
      return;
    }

    player.sitDown();
    player.setPrivateStoreType(3);
    player.saveTradeList();
    player.setTarget(null);

    player.broadcastUserInfo();
    player.broadcastPacket(new PrivateStoreMsgBuy(player));
  }
}