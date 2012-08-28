package l2p.gameserver.serverpackets;

public class ExPVPMatchUserDie extends L2GameServerPacket
{
  private int _blueKills;
  private int _redKills;

  protected final void writeImpl()
  {
    writeEx(127);
    writeD(_blueKills);
    writeD(_redKills);
  }
}