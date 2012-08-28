package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.skills.Env;

public class EffectPetrification extends L2Effect
{
  public EffectPetrification(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.PETRIFICATION;
  }

  public void onStart()
  {
    getEffected().startAbnormalEffect(2048);
    getEffected().setIsParalyzed(true);
    getEffected().setIsInvul(true);
  }

  public void onExit()
  {
    getEffected().stopAbnormalEffect(2048);
    getEffected().setIsParalyzed(false);
    getEffected().setIsInvul(false);
  }

  public boolean onActionTime()
  {
    return false;
  }
}