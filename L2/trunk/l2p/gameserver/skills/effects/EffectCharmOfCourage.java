package l2p.gameserver.skills.effects;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.model.Player;
import l2p.gameserver.stats.Env;

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