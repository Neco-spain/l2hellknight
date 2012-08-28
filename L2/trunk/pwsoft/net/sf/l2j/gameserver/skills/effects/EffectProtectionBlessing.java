package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Character;
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

  public void onStart()
  {
    if (getEffected().isL2Playable())
      ((L2PlayableInstance)getEffected()).startProtectionBlessing();
  }

  public void onExit()
  {
    if (getEffected().isL2Playable())
      ((L2PlayableInstance)getEffected()).stopProtectionBlessing(this);
  }

  public boolean onActionTime()
  {
    return false;
  }
}