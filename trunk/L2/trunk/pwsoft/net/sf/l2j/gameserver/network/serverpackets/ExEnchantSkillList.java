package net.sf.l2j.gameserver.network.serverpackets;

import java.util.List;
import javolution.util.FastList;

public class ExEnchantSkillList extends L2GameServerPacket
{
  private List<Skill> _skills;

  public void addSkill(int id, int level, int sp, int exp)
  {
    _skills.add(new Skill(id, level, sp, exp));
  }

  public ExEnchantSkillList()
  {
    _skills = new FastList();
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(23);

    writeD(_skills.size());
    for (Skill sk : _skills)
    {
      writeD(sk.id);
      writeD(sk.nextLevel);
      writeD(sk.sp);
      writeQ(sk.exp);
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
    public int nextLevel;
    public int sp;
    public int exp;

    Skill(int pId, int pNextLevel, int pSp, int pExp)
    {
      id = pId;
      nextLevel = pNextLevel;
      sp = pSp;
      exp = pExp;
    }
  }
}