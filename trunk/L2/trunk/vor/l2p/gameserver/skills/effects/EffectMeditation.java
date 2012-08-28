package l2p.gameserver.skills.effects;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.stats.Env;

public final class EffectMeditation extends Effect
{
  public EffectMeditation(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public void onStart()
  {
    super.onStart();
    _effected.startParalyzed();
    _effected.setMeditated(true);
  }

  public void onExit()
  {
    super.onExit();
    _effected.stopParalyzed();
    _effected.setMeditated(false);
  }

  public boolean onActionTime()
  {
    return false;
  }
}