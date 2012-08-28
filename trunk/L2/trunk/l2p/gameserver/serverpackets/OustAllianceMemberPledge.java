package l2p.gameserver.serverpackets;

public class OustAllianceMemberPledge extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeC(172);
  }
}