package l2p.gameserver.skills.effects;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.stats.Env;

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