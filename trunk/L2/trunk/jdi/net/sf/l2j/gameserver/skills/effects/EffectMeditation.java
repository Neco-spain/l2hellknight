package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.skills.Env;

final class EffectMeditation extends L2Effect
{
  public EffectMeditation(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.MEDITATION;
  }

  public boolean onStart()
  {
    getEffected().startMeditation();
    return true;
  }

  public void onExit()
  {
    getEffected().stopMeditation(this);
  }

  public boolean onActionTime()
  {
    getEffected().stopMeditation(this);
    return false;
  }
}