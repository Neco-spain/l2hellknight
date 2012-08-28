package l2p.gameserver.serverpackets;

public class PledgeExtendedInfo extends L2GameServerPacket
{
  protected final void writeImpl()
  {
    writeC(138);
  }
}