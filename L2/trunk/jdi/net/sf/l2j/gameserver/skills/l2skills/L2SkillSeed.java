package net.sf.l2j.gameserver.skills.l2skills;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.skills.effects.EffectSeed;
import net.sf.l2j.gameserver.templates.StatsSet;

public class L2SkillSeed extends L2Skill
{
  public L2SkillSeed(StatsSet set)
  {
    super(set);
  }

  public void useSkill(L2Character caster, L2Object[] targets)
  {
    if (caster.isAlikeDead()) {
      return;
    }

    for (int i = 0; i < targets.length; i++) {
      L2Character target = (L2Character)targets[i];
      if ((target.isAlikeDead()) && (getTargetType() != L2Skill.SkillTargetType.TARGET_CORPSE_MOB)) {
        continue;
      }
      EffectSeed oldEffect = (EffectSeed)target.getFirstEffect(getId());
      if (oldEffect == null)
        getEffects(caster, target);
      else oldEffect.increasePower();

      L2Effect[] effects = target.getAllEffects();
      for (int j = 0; j < effects.length; j++)
        if (effects[j].getEffectType() == L2Effect.EffectType.SEED)
          effects[j].rescheduleEffect();
    }
  }
}