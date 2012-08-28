package l2p.gameserver.serverpackets;

public class ExEventMatchScore extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(16);
  }
}