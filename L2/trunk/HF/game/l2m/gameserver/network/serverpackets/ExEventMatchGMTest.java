package l2m.gameserver.network.serverpackets;

public class ExEventMatchGMTest extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(7);
  }
}