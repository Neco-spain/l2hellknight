package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.skills.Env;

final class EffectCoolStun extends L2Effect
{
  public EffectCoolStun(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.STUN;
  }

  public void onStart()
  {
    getEffected().setTarget(null);
    getEffected().startStunning();
    getEffected().startAbnormalEffect(524288);
  }

  public void onExit()
  {
    getEffected().stopAbnormalEffect(524288);
    getEffected().stopStunning(this);
  }

  public boolean onActionTime()
  {
    return false;
  }
}