package l2p.gameserver.skills.effects;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.stats.Env;

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