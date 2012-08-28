package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.skills.Env;

final class EffectStun extends L2Effect
{
  public EffectStun(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.STUN;
  }

  public boolean onStart()
  {
    getEffected().startStunning();
    return true;
  }

  public void onExit()
  {
    getEffected().stopStunning(this);
  }

  public boolean onActionTime()
  {
    return false;
  }
}