package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.skills.Env;

final class EffectFrintSong extends L2Effect
{
  public EffectFrintSong(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.PARALYZE;
  }

  public void onStart()
  {
    getEffected().startAbnormalEffect(262144);
    getEffected().setIsParalyzed(true);
  }

  public void onExit()
  {
    getEffected().stopAbnormalEffect(262144);
    getEffected().setIsParalyzed(false);
  }

  public boolean onActionTime()
  {
    return false;
  }
}