package l2m.gameserver.network.serverpackets;

public class ExCubeGameCloseUI extends L2GameServerPacket
{
  int _seconds;

  protected void writeImpl()
  {
    writeEx(151);
    writeD(-1);
  }
}