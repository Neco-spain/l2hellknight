package l2m.gameserver.skills.effects;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.skills.Env;

public final class EffectDebuffImmunity extends Effect
{
  public EffectDebuffImmunity(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public void onStart()
  {
    super.onStart();
    getEffected().startDebuffImmunity();
  }

  public void onExit()
  {
    super.onExit();
    getEffected().stopDebuffImmunity();
  }

  public boolean onActionTime()
  {
    return false;
  }
}