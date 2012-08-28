package l2p.gameserver.skills.effects;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.stats.Env;

public final class EffectParalyze extends Effect
{
  public EffectParalyze(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public boolean checkCondition()
  {
    if (_effected.isParalyzeImmune())
      return false;
    return super.checkCondition();
  }

  public void onStart()
  {
    super.onStart();
    _effected.startParalyzed();
    _effected.abortAttack(true, true);
    _effected.abortCast(true, true);
  }

  public void onExit()
  {
    super.onExit();
    _effected.stopParalyzed();
  }

  public boolean onActionTime()
  {
    return false;
  }
}