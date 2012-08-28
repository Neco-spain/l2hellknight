package net.sf.l2j.gameserver.network.serverpackets;

import javolution.util.FastTable;

public class SkillList extends L2GameServerPacket
{
  private FastTable<Skill> _skills;

  public SkillList()
  {
    _skills = new FastTable();
  }

  public void addSkill(int id, int level, boolean passive)
  {
    _skills.add(new Skill(id, level, passive));
  }

  protected final void writeImpl()
  {
    writeC(88);
    writeD(_skills.size());

    int i = 0; for (int n = _skills.size(); i < n; i++)
    {
      Skill s = (Skill)_skills.get(i);
      writeD(s.passive ? 1 : 0);
      writeD(s.level);
      writeD(s.id);
      writeC(0);
    }
  }

  public void gc()
  {
    _skills.clear();
    _skills = null;
  }

  static class Skill
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