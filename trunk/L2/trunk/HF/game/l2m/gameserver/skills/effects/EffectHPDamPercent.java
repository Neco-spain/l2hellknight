package l2m.gameserver.skills.effects;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.skills.Env;

public class EffectHPDamPercent extends Effect
{
  public EffectHPDamPercent(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public void onStart()
  {
    super.onStart();

    if (_effected.isDead()) {
      return;
    }
    double newHp = (100.0D - calc()) * _effected.getMaxHp() / 100.0D;
    newHp = Math.min(_effected.getCurrentHp(), Math.max(0.0D, newHp));
    _effected.setCurrentHp(newHp, false);
  }

  public boolean onActionTime()
  {
    return false;
  }
}