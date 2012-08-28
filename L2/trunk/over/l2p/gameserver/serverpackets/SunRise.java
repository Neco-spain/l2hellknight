package l2p.gameserver.serverpackets;

public class SunRise extends L2GameServerPacket
{
  protected final void writeImpl()
  {
    writeC(18);
  }
}