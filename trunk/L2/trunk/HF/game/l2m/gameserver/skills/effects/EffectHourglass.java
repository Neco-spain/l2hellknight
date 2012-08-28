package l2m.gameserver.skills.effects;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.model.Player;
import l2m.gameserver.skills.Env;

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