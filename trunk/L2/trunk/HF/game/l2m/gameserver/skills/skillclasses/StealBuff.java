package l2m.gameserver.skills.skillclasses;

import java.util.Collections;
import java.util.List;
import l2p.commons.util.Rnd;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.model.EffectList;
import l2m.gameserver.model.Skill;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.network.serverpackets.components.SystemMsg;
import l2m.gameserver.skills.EffectType;
import l2m.gameserver.skills.effects.EffectTemplate;
import l2m.gameserver.skills.Env;
import l2m.gameserver.skills.Stats;
import l2m.gameserver.templates.StatsSet;
import l2m.gameserver.utils.EffectsComparator;

public class StealBuff extends Skill
{
  private final int _stealCount;

  public StealBuff(StatsSet set)
  {
    super(set);
    _stealCount = set.getInteger("stealCount", 1);
  }

  public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
  {
    if ((target == null) || (!target.isPlayer()))
    {
      activeChar.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
      return false;
    }

    return super.checkCondition(activeChar, target, forceUse, dontMove, first);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    for (Creature target : targets) {
      if (target != null)
      {
        int stealCount;
        int counter;
        if (calcStealChance(target, activeChar))
        {
          stealCount = Rnd.get(1, _stealCount);
          counter = 0;
          if (!target.isPlayer())
            continue;
          List effectsList = target.getEffectList().getAllEffects();
          Collections.sort(effectsList, EffectsComparator.getInstance());
          Collections.reverse(effectsList);
          for (Effect e : effectsList)
          {
            if (counter >= stealCount)
              break;
            if (canSteal(e))
            {
              Effect stolenEffect = cloneEffect(activeChar, e);
              if (stolenEffect != null)
                activeChar.getEffectList().addEffect(stolenEffect);
              e.exit();
              counter++;
            }

          }

        }
        else
        {
          activeChar.sendPacket(new SystemMessage(139).addString(target.getName()).addSkillName(getId(), getLevel()));
          continue;
        }
        getEffects(activeChar, target, getActivateRate() > 0, false);
      }
    }
    if (isSSPossible())
      activeChar.unChargeShots(isMagic());
  }

  private boolean canSteal(Effect e)
  {
    return (e != null) && (e.isInUse()) && (e.isCancelable()) && (!e.getSkill().isToggle()) && (!e.getSkill().isPassive()) && (!e.getSkill().isOffensive()) && (e.getEffectType() != EffectType.Vitality) && (!e.getTemplate()._applyOnCaster);
  }

  private boolean calcStealChance(Creature effected, Creature effector)
  {
    double cancel_res_multiplier = effected.calcStat(Stats.CANCEL_RESIST, 1.0D, null, null);
    int dml = effector.getLevel() - effected.getLevel();
    double prelimChance = (dml + 50) * (1.0D - cancel_res_multiplier * 0.01D);
    return Rnd.chance(prelimChance);
  }

  private Effect cloneEffect(Creature cha, Effect eff)
  {
    Skill skill = eff.getSkill();

    for (EffectTemplate et : skill.getEffectTemplates())
    {
      Effect effect = et.getEffect(new Env(cha, cha, skill));
      if (effect == null)
        continue;
      effect.setCount(eff.getCount());
      effect.setPeriod(eff.getCount() == 1 ? eff.getPeriod() - eff.getTime() : eff.getPeriod());
      return effect;
    }

    return null;
  }
}