package l2m.gameserver.skills.effects;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.skills.Env;

public class EffectInterrupt extends Effect
{
  public EffectInterrupt(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public void onStart()
  {
    super.onStart();
    if (!getEffected().isRaid())
      getEffected().abortCast(false, true);
  }

  public boolean onActionTime()
  {
    return false;
  }
}