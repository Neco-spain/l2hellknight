package l2m.gameserver.skills.effects;

import l2m.gameserver.ai.PlayerAI;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.skills.Env;

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