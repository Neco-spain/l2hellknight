package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.skills.Env;

final class EffectImobileBuff extends L2Effect
{
  public EffectImobileBuff(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.BUFF;
  }

  public void onStart()
  {
    getEffector().setIsImobilised(true);
  }

  public void onExit()
  {
    getEffector().setIsImobilised(false);
  }

  public boolean onActionTime()
  {
    return false;
  }
}