package l2p.gameserver.skills.effects;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.model.Player;
import l2p.gameserver.stats.Env;

public final class EffectHourglass extends Effect
{
  public EffectHourglass(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public void onStart()
  {
    super.onStart();
    if (_effected.isPlayer())
      _effected.getPlayer().startHourglassEffect();
  }

  public void onExit()
  {
    super.onExit();
    if (_effected.isPlayer())
      _effected.getPlayer().stopHourglassEffect();
  }

  public boolean onActionTime()
  {
    return false;
  }
}