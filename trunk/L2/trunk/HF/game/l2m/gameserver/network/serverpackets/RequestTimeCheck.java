package l2m.gameserver.serverpackets;

public class RequestTimeCheck extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeC(193);
  }
}