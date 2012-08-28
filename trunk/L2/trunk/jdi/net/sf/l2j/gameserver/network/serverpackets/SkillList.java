package net.sf.l2j.gameserver.network.serverpackets;

import java.util.Vector;

public class SkillList extends L2GameServerPacket
{
  private static final String _S__6D_SKILLLIST = "[S] 58 SkillList";
  private Vector<Skill> _skills;

  public SkillList()
  {
    _skills = new Vector();
  }

  public void addSkill(int id, int level, boolean passive)
  {
    _skills.add(new Skill(id, level, passive));
  }

  protected final void writeImpl()
  {
    writeC(88);
    writeD(_skills.size());

    for (int i = 0; i < _skills.size(); i++)
    {
      Skill temp = (Skill)_skills.get(i);
      writeD(temp.passive ? 1 : 0);
      writeD(temp.level);
      writeD(temp.id);
      writeC(0);
    }
  }

  public String getType()
  {
    return "[S] 58 SkillList";
  }

  class Skill
  {
    public int id;
    public int level;
    public boolean passive;

    Skill(int pId, int pLevel, boolean pPassive)
    {
      id = pId;
      level = pLevel;
      passive = pPassive;
    }
  }
}