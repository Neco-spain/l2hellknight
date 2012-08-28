package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.skills.Env;

public class EffectCharmOfCourage extends L2Effect
{
  public EffectCharmOfCourage(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.CHARMOFCOURAGE;
  }

  public boolean onStart()
  {
    if ((getEffected() instanceof L2PcInstance))
    {
      ((L2PcInstance)getEffected()).setCharmOfCourage(true);
      return true;
    }
    return false;
  }

  public void onExit()
  {
    if ((getEffected() instanceof L2PcInstance))
      ((L2PcInstance)getEffected()).setCharmOfCourage(false);
  }

  public boolean onActionTime()
  {
    return false;
  }
}