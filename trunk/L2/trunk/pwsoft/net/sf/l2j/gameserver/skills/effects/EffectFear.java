package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.skills.Env;

final class EffectFear extends L2Effect
{
  public static final int FEAR_RANGE = 500;

  public EffectFear(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.FEAR;
  }

  public void onStart()
  {
    if (!getEffected().isAfraid()) {
      getEffected().startFear();
      onActionTime();
    }
  }

  public void onExit()
  {
    getEffected().stopFear(this);
  }

  public boolean onActionTime()
  {
    if ((getEffected().isPlayer()) && 
      (getEffected().getPlayer().isInOlympiadMode())) {
      getEffected().getPlayer().startFear();
      return true;
    }

    getEffected().rndWalk();
    return true;
  }
}