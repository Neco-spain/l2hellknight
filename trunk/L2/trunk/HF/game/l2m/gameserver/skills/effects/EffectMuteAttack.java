package l2m.gameserver.skills.effects;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.skills.Env;

public class EffectMuteAttack extends Effect
{
  public EffectMuteAttack(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public void onStart()
  {
    super.onStart();

    if (!_effected.startAMuted())
    {
      _effected.abortCast(true, true);
      _effected.abortAttack(true, true);
    }
  }

  public void onExit()
  {
    super.onExit();
    _effected.stopAMuted();
  }

  public boolean onActionTime()
  {
    return false;
  }
}