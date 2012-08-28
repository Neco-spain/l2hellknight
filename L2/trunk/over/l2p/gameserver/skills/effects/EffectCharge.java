package l2p.gameserver.skills.effects;

import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.model.Player;
import l2p.gameserver.stats.Env;

public final class EffectCharge extends Effect
{
  public EffectCharge(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public void onStart()
  {
    super.onStart();

    if (getEffected().isPlayer())
    {
      Player player = (Player)getEffected();

      if (player.getIncreasedForce() >= calc())
        player.sendPacket(Msg.YOUR_FORCE_HAS_REACHED_MAXIMUM_CAPACITY_);
      else
        player.setIncreasedForce(player.getIncreasedForce() + 1);
    }
  }

  public boolean onActionTime()
  {
    return false;
  }
}