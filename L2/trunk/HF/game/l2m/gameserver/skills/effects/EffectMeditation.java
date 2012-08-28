package l2m.gameserver.skills.effects;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.skills.Env;

public final class EffectMeditation extends Effect
{
  public EffectMeditation(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public void onStart()
  {
    super.onStart();
    _effected.startParalyzed();
    _effected.setMeditated(true);
  }

  public void onExit()
  {
    super.onExit();
    _effected.stopParalyzed();
    _effected.setMeditated(false);
  }

  public boolean onActionTime()
  {
    return false;
  }
}