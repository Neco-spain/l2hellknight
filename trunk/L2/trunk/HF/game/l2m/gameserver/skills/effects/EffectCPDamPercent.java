package l2m.gameserver.skills.effects;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.skills.Env;

public class EffectCPDamPercent extends Effect
{
  public EffectCPDamPercent(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public void onStart()
  {
    super.onStart();

    if (_effected.isDead()) {
      return;
    }
    double newCp = (100.0D - calc()) * _effected.getMaxCp() / 100.0D;
    newCp = Math.min(_effected.getCurrentCp(), Math.max(0.0D, newCp));
    _effected.setCurrentCp(newCp);
  }

  public boolean onActionTime()
  {
    return false;
  }
}