package l2p.gameserver.serverpackets;

public class ExResponseFreeServer extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(119);
  }
}