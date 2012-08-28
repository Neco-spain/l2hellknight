package l2m.gameserver.serverpackets;

public class PledgeShowMemberListDeleteAll extends L2GameServerPacket
{
  public static final L2GameServerPacket STATIC = new PledgeShowMemberListDeleteAll();

  protected final void writeImpl()
  {
    writeC(136);
  }
}