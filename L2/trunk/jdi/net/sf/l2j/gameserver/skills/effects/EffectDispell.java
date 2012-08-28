package net.sf.l2j.gameserver.skills.effects;

import java.io.PrintStream;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.util.Rnd;

public class EffectDispell extends L2Effect
{
  public EffectDispell(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.DISPELL_YOU;
  }

  public boolean onStart()
  {
    L2Effect[] effects = getEffected().getAllEffects();
    double totalCancelVuln = 1.0D;
    for (L2Effect e : effects)
    {
      switch (e.getSkill().getId())
      {
      case 396:
        return true;
      case 110:
        totalCancelVuln *= 0.9D;
        break;
      case 111:
        totalCancelVuln *= 0.9D;
        break;
      case 287:
        totalCancelVuln *= 0.9D;
        break;
      case 341:
        totalCancelVuln *= 0.7D;
        break;
      case 368:
        totalCancelVuln *= 0.9D;
        break;
      case 395:
        totalCancelVuln *= 0.9D;
        break;
      case 1338:
        totalCancelVuln *= 1.3D;
        break;
      case 1354:
        totalCancelVuln *= 0.7D;
        break;
      case 1362:
        totalCancelVuln *= 0.7D;
        break;
      case 1415:
        totalCancelVuln *= 0.7D;
        break;
      case 5125:
        totalCancelVuln *= 0.8D;
        break;
      case 5145:
        totalCancelVuln *= 0.1D;
      }
    }

    int maxfive = 5;
    int DeffTime = Config.SKILL_DURATION_TIME;
    for (L2Effect e : effects)
    {
      switch (1.$SwitchMap$net$sf$l2j$gameserver$model$L2Effect$EffectType[e.getEffectType().ordinal()])
      {
      case 1:
      case 2:
        break;
      default:
        if ((e.getSkill().getId() == 4082) || (e.getSkill().getId() == 4215) || (e.getSkill().getId() == 4515) || (e.getSkill().getId() == 110) || (e.getSkill().getId() == 111) || (e.getSkill().getId() == 1323) || (e.getSkill().getId() == 1325))
        {
          continue;
        }
        if (e.getSkill().getSkillType() != L2Skill.SkillType.BUFF) {
          e.exit();
        }
        else {
          double prelim_chance = 2 * (80 - e.getSkill().getMagicLevel()) + 10 + e.getPeriod() / (120 * DeffTime);
          prelim_chance *= totalCancelVuln;
          if (prelim_chance < 5.0D) prelim_chance = 5.0D;
          if (prelim_chance > 95.0D) prelim_chance = 95.0D;

          if (Rnd.get(100) >= prelim_chance)
            continue;
          if (Config.DEBUG)
            System.out.println("skill: " + e.getSkill().getName() + " lvl: " + e.getSkill().getMagicLevel() + " time " + e.getPeriod() / DeffTime + " prelim_chance: " + prelim_chance + " totalCancelVuln: " + totalCancelVuln);
          e.exit();
          maxfive--;
          if (maxfive == 0) {
            break label649;
          }
        }
      }
    }
    label649: return true;
  }

  public void onExit()
  {
  }

  public boolean onActionTime()
  {
    return false;
  }
}