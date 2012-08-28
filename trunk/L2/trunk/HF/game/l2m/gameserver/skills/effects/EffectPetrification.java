package l2m.gameserver.skills.effects;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.skills.Env;

public final class EffectPetrification extends Effect
{
  public EffectPetrification(Env env, EffectTemplate template)
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
    _effected.startDebuffImmunity();
    _effected.startBuffImmunity();
    _effected.startDamageBlocked();
    _effected.abortAttack(true, true);
    _effected.abortCast(true, true);
  }

  public void onExit()
  {
    super.onExit();
    _effected.stopParalyzed();
    _effected.stopDebuffImmunity();
    _effected.stopBuffImmunity();
    _effected.stopDamageBlocked();
  }

  public boolean onActionTime()
  {
    return false;
  }
}