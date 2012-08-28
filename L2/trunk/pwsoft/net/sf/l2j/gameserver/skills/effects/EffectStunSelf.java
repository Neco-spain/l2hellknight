package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.skills.Env;

public class EffectStunSelf extends L2Effect
{
  public EffectStunSelf(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.STUN_SELF;
  }

  public void onStart()
  {
    getEffector().startStunning();
  }

  public void onExit()
  {
    getEffector().stopStunning(this);
  }

  public boolean onActionTime()
  {
    return false;
  }
}