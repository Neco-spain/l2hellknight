package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.skills.Env;

final class EffectSilentMove extends L2Effect
{
  public EffectSilentMove(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public void onStart()
  {
    super.onStart();
    if (getEffected().isPlayer())
    {
      if (getSkill().getId() == 296)
      {
        getEffected().getPlayer().setRelax(true);
        getEffected().getPlayer().sitDown();
      }

      getEffected().getPlayer().setSilentMoving(true);
    }
  }

  public void onExit()
  {
    super.onExit();
    if (getEffected().isPlayer())
    {
      if (getSkill().getId() == 296)
      {
        getEffected().getPlayer().setRelax(false);
        getEffected().getPlayer().sitDown();
      }

      getEffected().getPlayer().setSilentMoving(false);
    }
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.SILENT_MOVE;
  }

  public boolean onActionTime()
  {
    if (getSkill().getSkillType() != L2Skill.SkillType.CONT) {
      return false;
    }
    if (getEffected().isDead()) {
      return false;
    }
    double manaDam = calc();

    if (manaDam > getEffected().getCurrentMp())
    {
      getEffected().sendPacket(Static.SKILL_REMOVED_DUE_LACK_MP);
      return false;
    }

    if ((getSkill().getId() == 296) && (!getEffected().isSitting())) {
      return false;
    }
    getEffected().reduceCurrentMp(manaDam);
    return true;
  }
}