package l2m.gameserver.skills.effects;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.skills.Env;

public class EffectMuteAll extends Effect
{
  public EffectMuteAll(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public void onStart()
  {
    super.onStart();
    _effected.startMuted();
    _effected.startPMuted();
    _effected.abortCast(true, true);
  }

  public void onExit()
  {
    super.onExit();
    _effected.stopMuted();
    _effected.stopPMuted();
  }

  public boolean onActionTime()
  {
    return false;
  }
}