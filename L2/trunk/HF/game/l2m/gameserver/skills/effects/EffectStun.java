package l2m.gameserver.skills.effects;

import l2p.commons.util.Rnd;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.skills.Env;

public final class EffectStun extends Effect
{
  public EffectStun(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public boolean checkCondition()
  {
    return Rnd.chance(_template.chance(100));
  }

  public void onStart()
  {
    super.onStart();
    _effected.startStunning();
    _effected.abortAttack(true, true);
    _effected.abortCast(true, true);
    _effected.stopMove();
  }

  public void onExit()
  {
    super.onExit();
    _effected.stopStunning();
  }

  public boolean onActionTime()
  {
    return false;
  }
}