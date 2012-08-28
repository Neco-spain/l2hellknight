package l2m.gameserver.network.serverpackets;

public class Dice extends L2GameServerPacket
{
  private int _playerId;
  private int _itemId;
  private int _number;
  private int _x;
  private int _y;
  private int _z;

  public Dice(int playerId, int itemId, int number, int x, int y, int z)
  {
    _playerId = playerId;
    _itemId = itemId;
    _number = number;
    _x = x;
    _y = y;
    _z = z;
  }

  protected final void writeImpl()
  {
    writeC(218);
    writeD(_playerId);
    writeD(_itemId);
    writeD(_number);
    writeD(_x);
    writeD(_y);
    writeD(_z);
  }
}