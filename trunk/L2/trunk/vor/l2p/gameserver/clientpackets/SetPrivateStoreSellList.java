package l2p.gameserver.clientpackets;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.model.items.TradeItem;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.PrivateStoreManageListSell;
import l2p.gameserver.serverpackets.PrivateStoreMsgSell;
import l2p.gameserver.utils.TradeHelper;
import org.apache.commons.lang3.ArrayUtils;

public class SetPrivateStoreSellList extends L2GameClientPacket
{
  private int _count;
  private boolean _package;
  private int[] _items;
  private long[] _itemQ;
  private long[] _itemP;

  protected void readImpl()
  {
    _package = (readD() == 1);
    _count = readD();

    if ((_count * 20 > _buf.remaining()) || (_count > 32767) || (_count < 1))
    {
      _count = 0;
      return;
    }

    _items = new int[_count];
    _itemQ = new long[_count];
    _itemP = new long[_count];

    for (int i = 0; i < _count; i++)
    {
      _items[i] = readD();
      _itemQ[i] = readQ();
      _itemP[i] = readQ();
      if ((_itemQ[i] >= 1L) && (_itemP[i] >= 0L) && (ArrayUtils.indexOf(_items, _items[i]) >= i))
        continue;
      _count = 0;
      break;
    }
  }

  protected void runImpl()
  {
    Player seller = ((GameClient)getClient()).getActiveChar();
    if ((seller == null) || (_count == 0)) {
      return;
    }
    if (!TradeHelper.checksIfCanOpenStore(seller, _package ? 8 : 1))
    {
      seller.sendActionFailed();
      return;
    }

    List sellList = new CopyOnWriteArrayList();

    seller.getInventory().writeLock();
    try
    {
      for (int i = 0; i < _count; i++)
      {
        int objectId = _items[i];
        long count = _itemQ[i];
        long price = _itemP[i];

        ItemInstance item = seller.getInventory().getItemByObjectId(objectId);

        if ((item == null) || (item.getCount() < count) || (!item.canBeTraded(seller)) || (item.getItemId() == 57)) {
          continue;
        }
        TradeItem temp = new TradeItem(item);
        temp.setCount(count);
        temp.setOwnersPrice(price);

        sellList.add(temp);
      }
    }
    finally
    {
      seller.getInventory().writeUnlock();
    }

    if (sellList.size() > seller.getTradeLimit())
    {
      seller.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
      seller.sendPacket(new PrivateStoreManageListSell(seller, _package));
      return;
    }

    if (!sellList.isEmpty())
    {
      seller.setSellList(_package, sellList);
      seller.saveTradeList();
      seller.setPrivateStoreType(_package ? 8 : 1);
      seller.broadcastPacket(new L2GameServerPacket[] { new PrivateStoreMsgSell(seller) });
      seller.sitDown(null);
      seller.broadcastCharInfo();
    }

    seller.sendActionFailed();
  }
}