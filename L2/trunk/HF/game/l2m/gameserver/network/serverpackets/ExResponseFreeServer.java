package l2m.gameserver.network.serverpackets;

public class ExResponseFreeServer extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(119);
  }
}