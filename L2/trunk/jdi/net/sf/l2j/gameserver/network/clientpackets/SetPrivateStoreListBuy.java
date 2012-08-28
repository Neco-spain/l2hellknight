package net.sf.l2j.gameserver.network.clientpackets;

import java.io.PrintStream;
import java.nio.ByteBuffer;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.TradeList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.PrivateStoreManageListBuy;
import net.sf.l2j.gameserver.network.serverpackets.PrivateStoreMsgBuy;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class SetPrivateStoreListBuy extends L2GameClientPacket
{
  private static final String _C__91_SETPRIVATESTORELISTBUY = "[C] 91 SetPrivateStoreListBuy";
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
      int enchant = readH();
      _items[(x * 3 + 1)] = enchant;
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

    if ((Config.GM_DISABLE_TRANSACTION) && (player.getAccessLevel() >= Config.GM_TRANSACTION_MIN) && (player.getAccessLevel() <= Config.GM_TRANSACTION_MAX))
    {
      player.sendMessage("Transactions are disable for your Access Level");
      return;
    }

    if ((Config.USE_TRADE_ZONE) && (!player.isInsideZone(32768)))
    {
      player.sendMessage("\u0412\u044B \u043D\u0430\u0445\u043E\u0434\u0438\u0442\u0435\u0441\u044C \u0432 \u0437\u043E\u043D\u0435 \u0433\u0434\u0435 \u0437\u0430\u043F\u0440\u0435\u0449\u0435\u043D\u043E \u0442\u043E\u0440\u0433\u043E\u0432\u0430\u0442\u044C.");
      return;
    }

    TradeList tradeList = player.getBuyList();
    tradeList.clear();

    int cost = 0;
    for (int i = 0; i < _count; i++)
    {
      int itemId = _items[(i * 3 + 0)];
      int enchant = _items[(i * 3 + 1)];
      int count = _items[(i * 3 + 2)];
      int price = _items[(i * 3 + 3)];

      if (player.getInventory().getItemByItemId(itemId) == null)
      {
        player.setPrivateStoreType(0);
        System.out.println("JES: Player " + player.getName() + " tried to trade exploit.");
        player.broadcastUserInfo();
        return;
      }
      tradeList.addItemByItemId(itemId, count, price, enchant);
      cost += count * price;
    }

    if (_count <= 0)
    {
      player.setPrivateStoreType(0);
      player.broadcastUserInfo();
      return;
    }

    if (_count > player.GetPrivateBuyStoreLimit())
    {
      player.sendPacket(new PrivateStoreManageListBuy(player));
      player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
      return;
    }

    if ((cost > player.getAdena()) || (cost <= 0))
    {
      player.sendPacket(new PrivateStoreManageListBuy(player));
      player.sendPacket(new SystemMessage(SystemMessageId.THE_PURCHASE_PRICE_IS_HIGHER_THAN_MONEY));
      return;
    }

    player.sitDown();
    player.setPrivateStoreType(3);
    player.broadcastUserInfo();
    player.broadcastPacket(new PrivateStoreMsgBuy(player));
  }

  public String getType()
  {
    return "[C] 91 SetPrivateStoreListBuy";
  }
}