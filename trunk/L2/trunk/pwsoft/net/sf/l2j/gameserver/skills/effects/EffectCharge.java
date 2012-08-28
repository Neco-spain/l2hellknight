package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.skills.Env;

public class EffectCharge extends L2Effect
{
  public int numCharges;

  public EffectCharge(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.CHARGE;
  }

  public boolean onActionTime()
  {
    return true;
  }

  public int getLevel() {
    return numCharges;
  }
  public void addNumCharges(int i) { numCharges += i;
  }
}