package l2m.gameserver.model.base;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Skill;
import l2m.gameserver.skills.Env;
import l2m.gameserver.skills.Stats;

public enum SkillTrait
{
  NONE, 
  BLEED, 

  BOSS, 
  DEATH, 
  DERANGEMENT, 

  ETC, 
  GUST, 
  HOLD, 

  PARALYZE, 

  PHYSICAL_BLOCKADE, 
  POISON, 

  SHOCK, 

  SLEEP, 

  VALAKAS;

  public double calcVuln(Env env)
  {
    return 0.0D;
  }

  public double calcProf(Env env)
  {
    return 0.0D;
  }

  public static double calcEnchantMod(Env env)
  {
    int enchantLevel = skill.getDisplayLevel();
    if (enchantLevel <= 100)
      return 0.0D;
    enchantLevel %= 100;
    return enchantLevel;
  }
}