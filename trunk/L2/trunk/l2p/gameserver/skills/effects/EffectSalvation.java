package l2p.gameserver.skills.effects;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.stats.Env;

public final class EffectSalvation extends Effect
{
  public EffectSalvation(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public boolean checkCondition()
  {
    return (getEffected().isPlayer()) && (super.checkCondition());
  }

  public void onStart()
  {
    getEffected().setIsSalvation(true);
  }

  public void onExit()
  {
    super.onExit();
    getEffected().setIsSalvation(false);
  }

  public boolean onActionTime()
  {
    return false;
  }
}