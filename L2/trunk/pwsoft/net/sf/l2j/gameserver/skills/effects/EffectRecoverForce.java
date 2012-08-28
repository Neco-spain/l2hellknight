package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.skills.Env;

public class EffectRecoverForce extends L2Effect
{
  public EffectRecoverForce(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.SIGNET_EFFECT;
  }

  public void onStart()
  {
    if (getEffected().getCharges() < 5)
      getEffected().increaseCharges();
    else
      getEffected().sendPacket(Static.FORCE_MAXLEVEL_REACHED);
  }

  public boolean onActionTime()
  {
    getEffected().stopSkillEffects(getId());
    return true;
  }
}