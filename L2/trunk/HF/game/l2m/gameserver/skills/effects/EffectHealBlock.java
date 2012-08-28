package l2m.gameserver.skills.effects;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.skills.Env;

public final class EffectHealBlock extends Effect
{
  public EffectHealBlock(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public boolean checkCondition()
  {
    if (_effected.isHealBlocked())
      return false;
    return super.checkCondition();
  }

  public void onStart()
  {
    super.onStart();
    _effected.startHealBlocked();
  }

  public void onExit()
  {
    super.onExit();
    _effected.stopHealBlocked();
  }

  public boolean onActionTime()
  {
    return false;
  }
}