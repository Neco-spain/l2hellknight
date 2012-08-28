package l2p.gameserver.serverpackets;

public class ExEventMatchGMTest extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(7);
  }
}