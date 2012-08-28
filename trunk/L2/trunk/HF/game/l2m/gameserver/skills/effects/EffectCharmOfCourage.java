package l2m.gameserver.skills.effects;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.model.Player;
import l2m.gameserver.skills.Env;

public class EffectCharmOfCourage extends Effect
{
  public EffectCharmOfCourage(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public void onStart()
  {
    super.onStart();
    if (_effected.isPlayer())
      _effected.getPlayer().setCharmOfCourage(true);
  }

  public void onExit()
  {
    super.onExit();
    _effected.getPlayer().setCharmOfCourage(false);
  }

  public boolean onActionTime()
  {
    return false;
  }
}