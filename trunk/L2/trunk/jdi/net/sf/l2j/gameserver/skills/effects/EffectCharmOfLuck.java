package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.skills.Env;

public class EffectCharmOfLuck extends L2Effect
{
  public EffectCharmOfLuck(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.CHARM_OF_LUCK;
  }

  public boolean onStart()
  {
    if ((getEffected() instanceof L2PlayableInstance))
    {
      ((L2PlayableInstance)getEffected()).startCharmOfLuck();
      return true;
    }
    return false;
  }

  public void onExit()
  {
    if ((getEffected() instanceof L2PlayableInstance))
      ((L2PlayableInstance)getEffected()).stopCharmOfLuck(this);
  }

  public boolean onActionTime()
  {
    return false;
  }
}