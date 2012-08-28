package l2m.gameserver.skills.effects;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.skills.Env;

public class EffectMPDamPercent extends Effect
{
  public EffectMPDamPercent(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public void onStart()
  {
    super.onStart();

    if (_effected.isDead()) {
      return;
    }
    double newMp = (100.0D - calc()) * _effected.getMaxMp() / 100.0D;
    newMp = Math.min(_effected.getCurrentMp(), Math.max(0.0D, newMp));
    _effected.setCurrentMp(newMp);
  }

  public boolean onActionTime()
  {
    return false;
  }
}