package l2p.gameserver.serverpackets;

public class ExNavitAdventPointInfo extends L2GameServerPacket
{
  private int _points;

  public ExNavitAdventPointInfo(int points)
  {
    _points = points;
  }

  protected final void writeImpl()
  {
    writeEx(223);
    writeD(_points);
  }
}