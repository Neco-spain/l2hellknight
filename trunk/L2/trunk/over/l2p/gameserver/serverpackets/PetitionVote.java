package l2p.gameserver.serverpackets;

public class PetitionVote extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeC(252);
  }
}