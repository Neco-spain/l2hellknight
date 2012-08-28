package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.skills.Env;

final class EffectNoblesseBless extends L2Effect
{
  public EffectNoblesseBless(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.NOBLESSE_BLESSING;
  }

  public boolean onStart()
  {
    if ((getEffected() instanceof L2PlayableInstance))
    {
      ((L2PlayableInstance)getEffected()).startNoblesseBlessing();
      return true;
    }
    return false;
  }

  public void onExit()
  {
    if ((getEffected() instanceof L2PlayableInstance))
      ((L2PlayableInstance)getEffected()).stopNoblesseBlessing(this);
  }

  public boolean onActionTime()
  {
    return false;
  }
}