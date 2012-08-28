package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.effects.EffectForce;

public final class ConditionForceBuff extends Condition
{
  private static final short BATTLE_FORCE = 5104;
  private static final short SPELL_FORCE = 5105;
  private final byte[] _forces;

  public ConditionForceBuff(byte[] forces)
  {
    _forces = forces;
  }

  public boolean testImpl(Env env)
  {
    if (_forces[0] > 0)
    {
      L2Effect force = env.cha.getFirstEffect(5104);
      if ((force == null) || (((EffectForce)force).forces < _forces[0])) {
        return false;
      }
    }
    if (_forces[1] > 0)
    {
      L2Effect force = env.cha.getFirstEffect(5105);
      if ((force == null) || (((EffectForce)force).forces < _forces[1])) {
        return false;
      }
    }
    return true;
  }
}