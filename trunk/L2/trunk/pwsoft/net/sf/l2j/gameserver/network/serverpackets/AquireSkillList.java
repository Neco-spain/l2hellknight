package net.sf.l2j.gameserver.network.serverpackets;

import java.util.List;
import javolution.util.FastList;

public class AquireSkillList extends L2GameServerPacket
{
  private List<Skill> _skills;
  private SkillType _fishingSkills;

  public AquireSkillList(SkillType type)
  {
    _skills = new FastList();
    _fishingSkills = type;
  }

  public void addSkill(int id, int nextLevel, int maxLevel, int spCost, int requirements)
  {
    _skills.add(new Skill(id, nextLevel, maxLevel, spCost, requirements));
  }

  protected final void writeImpl()
  {
    writeC(138);
    writeD(_fishingSkills.ordinal());
    writeD(_skills.size());

    for (Skill temp : _skills)
    {
      writeD(temp.id);
      writeD(temp.nextLevel);
      writeD(temp.maxLevel);
      writeD(temp.spCost);
      writeD(temp.requirements);
    }
  }

  public void gc()
  {
    _skills.clear();
    _skills = null;
  }

  private static class Skill
  {
    public int id;
    public int nextLevel;
    public int maxLevel;
    public int spCost;
    public int requirements;

    public Skill(int pId, int pNextLevel, int pMaxLevel, int pSpCost, int pRequirements)
    {
      id = pId;
      nextLevel = pNextLevel;
      maxLevel = pMaxLevel;
      spCost = pSpCost;
      requirements = pRequirements;
    }
  }

  public static enum SkillType
  {
    Usual, 
    Fishing, 
    Clan;
  }
}