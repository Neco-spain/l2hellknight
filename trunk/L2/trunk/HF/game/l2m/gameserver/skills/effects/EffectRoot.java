package l2m.gameserver.skills.effects;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.skills.Env;

public final class EffectRoot extends Effect
{
  public EffectRoot(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public void onStart()
  {
    super.onStart();
    _effected.startRooted();
    _effected.stopMove();
  }

  public void onExit()
  {
    super.onExit();
    _effected.stopRooted();
  }

  public boolean onActionTime()
  {
    return false;
  }
}