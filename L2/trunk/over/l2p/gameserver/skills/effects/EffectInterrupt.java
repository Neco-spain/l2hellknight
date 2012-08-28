package l2p.gameserver.skills.effects;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.stats.Env;

public class EffectInterrupt extends Effect
{
  public EffectInterrupt(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public void onStart()
  {
    super.onStart();
    if (!getEffected().isRaid())
      getEffected().abortCast(false, true);
  }

  public boolean onActionTime()
  {
    return false;
  }
}