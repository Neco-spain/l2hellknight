package l2m.gameserver.network.serverpackets;

public class ExEventMatchScore extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(16);
  }
}