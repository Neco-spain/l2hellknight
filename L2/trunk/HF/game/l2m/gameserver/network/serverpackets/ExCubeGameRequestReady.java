package l2m.gameserver.network.serverpackets;

public class ExCubeGameRequestReady extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(151);
    writeD(4);
  }
}