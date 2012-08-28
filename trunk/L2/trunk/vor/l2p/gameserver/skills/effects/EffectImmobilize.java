package l2p.gameserver.skills.effects;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.stats.Env;

public final class EffectImmobilize extends Effect
{
  public EffectImmobilize(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public void onStart()
  {
    super.onStart();
    _effected.startImmobilized();
    _effected.stopMove();
  }

  public void onExit()
  {
    super.onExit();
    _effected.stopImmobilized();
  }

  public boolean onActionTime()
  {
    return false;
  }
}