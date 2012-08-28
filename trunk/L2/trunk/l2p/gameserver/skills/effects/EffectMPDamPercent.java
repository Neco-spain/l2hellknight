package l2p.gameserver.skills.effects;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.stats.Env;

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