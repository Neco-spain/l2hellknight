package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.skills.Env;

public class EffectProtectionBlessing extends L2Effect
{
  public EffectProtectionBlessing(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.PROTECTION_BLESSING;
  }

  public boolean onStart()
  {
    if ((getEffected() instanceof L2PlayableInstance))
    {
      ((L2PlayableInstance)getEffected()).startProtectionBlessing();
      return true;
    }
    return false;
  }

  public void onExit()
  {
    ((L2PlayableInstance)getEffected()).stopProtectionBlessing(this);
  }

  public boolean onActionTime()
  {
    return false;
  }
}