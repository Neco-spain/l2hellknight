package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.skills.Env;

public class EffectMalariaBuff extends L2Effect
{
  public EffectMalariaBuff(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.BUFF;
  }

  public void onStart()
  {
    getEffected().startAbnormalEffect(2);
  }

  public boolean onActionTime()
  {
    return false;
  }

  public void onExit()
  {
    getEffected().stopAbnormalEffect(2);
  }
}