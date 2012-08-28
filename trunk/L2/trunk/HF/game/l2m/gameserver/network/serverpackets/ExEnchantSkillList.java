package l2m.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

public class ExEnchantSkillList extends L2GameServerPacket
{
  private final List<Skill> _skills;
  private final EnchantSkillType _type;

  public void addSkill(int id, int level)
  {
    _skills.add(new Skill(id, level));
  }

  public ExEnchantSkillList(EnchantSkillType type)
  {
    _type = type;
    _skills = new ArrayList();
  }

  protected final void writeImpl()
  {
    writeEx(41);

    writeD(_type.ordinal());
    writeD(_skills.size());
    for (Skill sk : _skills)
    {
      writeD(sk.id);
      writeD(sk.level);
    }
  }

  class Skill
  {
    public int id;
    public int level;

    Skill(int id, int nextLevel)
    {
      this.id = id;
      level = nextLevel;
    }
  }

  public static enum EnchantSkillType
  {
    NORMAL, 
    SAFE, 
    UNTRAIN, 
    CHANGE_ROUTE;
  }
}