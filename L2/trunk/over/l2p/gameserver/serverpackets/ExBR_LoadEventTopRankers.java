package l2p.gameserver.serverpackets;

public class ExBR_LoadEventTopRankers extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(189);
  }
}