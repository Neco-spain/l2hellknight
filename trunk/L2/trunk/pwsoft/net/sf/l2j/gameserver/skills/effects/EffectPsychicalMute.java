package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.skills.Env;

public class EffectPsychicalMute extends L2Effect
{
  public EffectPsychicalMute(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.PSYCHICAL_MUTE;
  }

  public void onStart()
  {
    getEffected().startPsychicalMuted();
  }

  public boolean onActionTime()
  {
    getEffected().stopPsychicalMuted(this);
    return false;
  }

  public void onExit()
  {
    getEffected().stopPsychicalMuted(this);
  }
}