package l2p.gameserver.skills.effects;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.stats.Env;

public final class EffectSleep extends Effect
{
  public EffectSleep(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public void onStart()
  {
    super.onStart();
    _effected.startSleeping();
    _effected.abortAttack(true, true);
    _effected.abortCast(true, true);
    _effected.stopMove();
  }

  public void onExit()
  {
    super.onExit();
    _effected.stopSleeping();
  }

  public boolean onActionTime()
  {
    return false;
  }
}