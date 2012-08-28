package l2m.gameserver.serverpackets;

public class SunSet extends L2GameServerPacket
{
  protected final void writeImpl()
  {
    writeC(19);
  }
}