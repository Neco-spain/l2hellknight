package net.sf.l2j.gameserver.datatables;

import javolution.util.FastTable;
import net.sf.l2j.gameserver.model.L2Skill;

public class HeroSkillTable
{
  private static HeroSkillTable _instance;
  private static FastTable<L2Skill> _hS;

  public static HeroSkillTable getInstance()
  {
    return _instance;
  }

  public static void init()
  {
    _instance = new HeroSkillTable();
    load();
  }

  private static void load()
  {
    _hS = new FastTable();
    SkillTable sk = SkillTable.getInstance();
    _hS.add(sk.getInfo(395, 1));
    _hS.add(sk.getInfo(396, 1));
    _hS.add(sk.getInfo(1374, 1));
    _hS.add(sk.getInfo(1375, 1));
    _hS.add(sk.getInfo(1376, 1));
  }

  public static FastTable<L2Skill> getHeroSkills()
  {
    return _hS;
  }

  public static boolean isHeroSkill(int skillid)
  {
    switch (skillid)
    {
    case 395:
    case 396:
    case 1374:
    case 1375:
    case 1376:
      return true;
    }
    return false;
  }
}