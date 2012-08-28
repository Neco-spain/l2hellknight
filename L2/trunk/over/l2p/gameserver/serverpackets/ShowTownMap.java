package l2p.gameserver.serverpackets;

public class ShowTownMap extends L2GameServerPacket
{
  String _texture;
  int _x;
  int _y;

  public ShowTownMap(String texture, int x, int y)
  {
    _texture = texture;
    _x = x;
    _y = y;
  }

  protected final void writeImpl()
  {
    writeC(234);
    writeS(_texture);
    writeD(_x);
    writeD(_y);
  }
}