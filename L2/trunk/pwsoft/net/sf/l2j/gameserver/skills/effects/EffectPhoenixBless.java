package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.skills.Env;

final class EffectPhoenixBless extends L2Effect
{
  public EffectPhoenixBless(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.PHOENIX_BLESSING;
  }

  public void onStart()
  {
    if (getEffected().isL2Playable())
      ((L2PlayableInstance)getEffected()).startPhoenixBlessing();
  }

  public void onExit()
  {
    if (getEffected().isL2Playable())
      ((L2PlayableInstance)getEffected()).stopPhoenixBlessing(this);
  }

  public boolean onActionTime()
  {
    return false;
  }
}