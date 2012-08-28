package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.skills.Env;

public class EffectInvincible extends L2Effect
{
  public EffectInvincible(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.INVINCIBLE;
  }

  public boolean onStart()
  {
    getEffected().setIsInvul(true);
    return true;
  }

  public boolean onActionTime()
  {
    getEffected().setIsInvul(false);
    return false;
  }

  public void onExit()
  {
    getEffected().setIsInvul(false);
  }
}