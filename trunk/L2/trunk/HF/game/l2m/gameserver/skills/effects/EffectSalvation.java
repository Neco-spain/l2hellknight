package l2m.gameserver.skills.effects;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.skills.Env;

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