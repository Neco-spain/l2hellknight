package net.sf.l2j.gameserver.network.serverpackets;

public class BeginRotation extends L2GameServerPacket
{
  private static final String _S__77_BEGINROTATION = "[S] 62 BeginRotation";
  private int _charObjId;
  private int _degree;
  private int _side;
  private int _speed;

  public BeginRotation(int player, int degree, int side, int speed)
  {
    _charObjId = player;
    _degree = degree;
    _side = side;
    _speed = speed;
  }

  protected final void writeImpl()
  {
    writeC(98);
    writeD(_charObjId);
    writeD(_degree);
    writeD(_side);
    writeD(_speed);
  }

  public String getType()
  {
    return "[S] 62 BeginRotation";
  }
}