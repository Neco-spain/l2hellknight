package l2m.gameserver.skills.effects;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.skills.Env;

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