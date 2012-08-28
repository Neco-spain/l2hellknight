package l2p.gameserver.model.base;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Skill;
import l2p.gameserver.stats.Env;
import l2p.gameserver.stats.Stats;

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