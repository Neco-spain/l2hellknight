package l2m.gameserver.serverpackets;

public class PledgeExtendedInfo extends L2GameServerPacket
{
  protected final void writeImpl()
  {
    writeC(138);
  }
}