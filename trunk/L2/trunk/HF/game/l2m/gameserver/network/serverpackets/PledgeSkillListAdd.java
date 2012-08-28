package l2m.gameserver.serverpackets;

public class PledgeSkillListAdd extends L2GameServerPacket
{
  private int _skillId;
  private int _skillLevel;

  public PledgeSkillListAdd(int skillId, int skillLevel)
  {
    _skillId = skillId;
    _skillLevel = skillLevel;
  }

  protected final void writeImpl()
  {
    writeEx(59);
    writeD(_skillId);
    writeD(_skillLevel);
  }
}