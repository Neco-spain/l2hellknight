package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.skills.Env;

public class EffectMute extends L2Effect
{
  public EffectMute(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.MUTE;
  }

  public boolean onStart()
  {
    getEffected().startMuted();
    return true;
  }

  public boolean onActionTime()
  {
    getEffected().stopMuted(this);
    return false;
  }

  public void onExit()
  {
    getEffected().stopMuted(this);
  }
}