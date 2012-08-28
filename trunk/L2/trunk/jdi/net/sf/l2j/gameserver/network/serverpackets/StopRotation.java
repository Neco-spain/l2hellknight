package net.sf.l2j.gameserver.network.serverpackets;

public class StopRotation extends L2GameServerPacket
{
  private static final String _S__78_STOPROTATION = "[S] 63 StopRotation";
  private int _charObjId;
  private int _degree;
  private int _speed;

  public StopRotation(int player, int degree, int speed)
  {
    _charObjId = player;
    _degree = degree;
    _speed = speed;
  }

  protected final void writeImpl()
  {
    writeC(99);
    writeD(_charObjId);
    writeD(_degree);
    writeD(_speed);
    writeC(0);
  }

  public String getType()
  {
    return "[S] 63 StopRotation";
  }
}