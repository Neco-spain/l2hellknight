package l2p.gameserver.skills.effects;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.stats.Env;

public final class EffectBlessNoblesse extends Effect
{
  public EffectBlessNoblesse(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public void onStart()
  {
    super.onStart();
    getEffected().setIsBlessedByNoblesse(true);
  }

  public void onExit()
  {
    super.onExit();
    getEffected().setIsBlessedByNoblesse(false);
  }

  public boolean onActionTime()
  {
    return false;
  }
}