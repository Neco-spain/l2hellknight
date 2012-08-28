package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Character;
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

  public void onStart()
  {
    if (getEffected().isL2Playable())
      ((L2PlayableInstance)getEffected()).startNoblesseBlessing();
  }

  public void onExit()
  {
    if (getEffected().isL2Playable())
      ((L2PlayableInstance)getEffected()).stopNoblesseBlessing(this);
  }

  public boolean onActionTime()
  {
    return false;
  }
}