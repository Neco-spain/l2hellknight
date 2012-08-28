package l2m.gameserver.skills.effects;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.skills.Env;

public final class EffectAgathionRes extends Effect
{
  public EffectAgathionRes(Env env, EffectTemplate template)
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