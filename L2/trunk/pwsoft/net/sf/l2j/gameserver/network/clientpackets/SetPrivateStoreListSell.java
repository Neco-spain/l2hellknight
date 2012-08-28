package net.sf.l2j.gameserver.network.clientpackets;

import java.nio.ByteBuffer;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.TradeList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.PrivateStoreManageListSell;
import net.sf.l2j.gameserver.network.serverpackets.PrivateStoreMsgSell;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class SetPrivateStoreListSell extends L2GameClientPacket
{
  private int _count;
  private boolean _packageSale;
  private int[] _items;

  protected void readImpl()
  {
    _packageSale = (readD() == 1);
    _count = readD();
    if ((_count <= 0) || (_count * 12 > _buf.remaining()) || (_count > Config.MAX_ITEM_IN_PACKET))
    {
      _count = 0;
      _items = null;
      return;
    }
    _items = new int[_count * 3];
    for (int x = 0; x < _count; x++)
    {
      int objectId = readD();
      _items[(x * 3 + 0)] = objectId;
      long cnt = readD();
      if ((cnt > 2147483647L) || (cnt < 0L))
      {
        _count = 0;
        _items = null;
        return;
      }
      _items[(x * 3 + 1)] = (int)cnt;
      int price = readD();
      _items[(x * 3 + 2)] = price;
    }
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) return;

    TradeList tradeList = player.getSellList();
    tradeList.clear();
    tradeList.setPackaged(_packageSale);

    for (int i = 0; i < _count; i++)
    {
      int objectId = _items[(i * 3 + 0)];
      int count = _items[(i * 3 + 1)];
      int price = _items[(i * 3 + 2)];

      if (price <= 0)
      {
        _count = 0;
        _items = null;
        return;
      }

      tradeList.addItem(objectId, count, price);
    }

    if ((_count <= 0) || (player.getChannel() > 1))
    {
      player.setPrivateStoreType(0);
      player.broadcastUserInfo();
      return;
    }

    if (_count > player.getPrivateSellStoreLimit())
    {
      player.sendPacket(new PrivateStoreManageListSell(player));
      player.sendPacket(SystemMessage.id(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
      return;
    }

    if (!player.canTrade())
    {
      player.sendActionFailed();
      return;
    }

    player.sitDown();
    if (_packageSale)
      player.setPrivateStoreType(8);
    else
      player.setPrivateStoreType(1);
    player.saveTradeList();
    player.setTarget(null);

    player.broadcastUserInfo();
    player.broadcastPacket(new PrivateStoreMsgSell(player));
  }
}