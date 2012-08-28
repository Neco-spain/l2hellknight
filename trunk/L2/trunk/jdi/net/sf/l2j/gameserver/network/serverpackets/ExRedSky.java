package net.sf.l2j.gameserver.network.serverpackets;

public class ExRedSky extends L2GameServerPacket
{
  private static final String _S__FE_40_EXREDSKYPACKET = "[S] FE:40 ExRedSkyPacket";
  private int _duration;

  public ExRedSky(int duration)
  {
    _duration = duration;
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(64);
    writeD(_duration);
  }

  public String getType()
  {
    return "[S] FE:40 ExRedSkyPacket";
  }
}