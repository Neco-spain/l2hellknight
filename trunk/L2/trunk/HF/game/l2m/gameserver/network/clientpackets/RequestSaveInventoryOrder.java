package l2m.gameserver.network.clientpackets;

import java.nio.ByteBuffer;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.items.PcInventory;
import l2m.gameserver.network.GameClient;

public class RequestSaveInventoryOrder extends L2GameClientPacket
{
  int[][] _items;

  protected void readImpl()
  {
    int size = readD();
    if (size > 125)
      size = 125;
    if ((size * 8 > _buf.remaining()) || (size < 1))
    {
      _items = ((int[][])null);
      return;
    }
    _items = new int[size][2];
    for (int i = 0; i < size; i++)
    {
      _items[i][0] = readD();
      _items[i][1] = readD();
    }
  }

  protected void runImpl()
  {
    if (_items == null)
      return;
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null)
      return;
    activeChar.getInventory().sort(_items);
  }
}