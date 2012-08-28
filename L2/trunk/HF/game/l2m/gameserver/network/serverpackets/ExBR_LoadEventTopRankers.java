package l2m.gameserver.network.serverpackets;

public class ExBR_LoadEventTopRankers extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(189);
  }
}