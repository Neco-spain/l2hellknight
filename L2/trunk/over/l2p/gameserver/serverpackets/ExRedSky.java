package l2p.gameserver.serverpackets;

public class ExRedSky extends L2GameServerPacket
{
  private int _duration;

  public ExRedSky(int duration)
  {
    _duration = duration;
  }

  protected final void writeImpl()
  {
    writeEx(65);
    writeD(_duration);
  }
}