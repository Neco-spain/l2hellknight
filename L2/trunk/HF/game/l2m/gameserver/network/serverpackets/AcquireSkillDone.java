package l2m.gameserver.network.serverpackets;

public class AcquireSkillDone extends L2GameServerPacket
{
  public static final L2GameServerPacket STATIC = new AcquireSkillDone();

  protected void writeImpl()
  {
    writeC(148);
  }
}