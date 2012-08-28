package l2p.gameserver.skills.effects;

import l2p.gameserver.ai.PlayerAI;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.stats.Env;

public class EffectAggression extends Effect
{
  public EffectAggression(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public void onStart()
  {
    super.onStart();
    if ((_effected.isPlayer()) && (_effected != _effector))
      ((PlayerAI)_effected.getAI()).lockTarget(_effector);
  }

  public void onExit()
  {
    super.onExit();
    if ((_effected.isPlayer()) && (_effected != _effector))
      ((PlayerAI)_effected.getAI()).lockTarget(null);
  }

  public boolean onActionTime()
  {
    return false;
  }
}