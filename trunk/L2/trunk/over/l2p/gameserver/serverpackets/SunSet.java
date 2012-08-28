package l2p.gameserver.serverpackets;

public class SunSet extends L2GameServerPacket
{
  protected final void writeImpl()
  {
    writeC(19);
  }
}