package net.sf.l2j.gameserver.network.serverpackets;

public class ShowTownMap extends L2GameServerPacket
{
  private String _texture;
  private int _x;
  private int _y;

  public ShowTownMap(String texture, int x, int y)
  {
    _texture = texture;
    _x = x;
    _y = y;
  }

  protected final void writeImpl()
  {
    writeC(222);
    writeS(_texture);
    writeD(_x);
    writeD(_y);
  }
}