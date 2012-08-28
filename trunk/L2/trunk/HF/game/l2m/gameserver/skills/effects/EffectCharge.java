package l2m.gameserver.skills.effects;

import l2m.gameserver.cache.Msg;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.model.Player;
import l2m.gameserver.skills.Env;

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