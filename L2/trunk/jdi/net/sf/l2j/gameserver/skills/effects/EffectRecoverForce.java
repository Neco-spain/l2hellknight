package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.EtcStatusUpdate;
import net.sf.l2j.gameserver.skills.Env;

public class EffectRecoverForce extends L2Effect
{
  public EffectRecoverForce(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public EffectRecoverForce(Env env, EffectTemplate template, byte kol)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.BUFF;
  }

  public boolean onActionTime()
  {
    if ((getEffected() instanceof L2PcInstance))
    {
      EffectCharge effect = (EffectCharge)getEffected().getFirstEffect(L2Effect.EffectType.CHARGE);
      if ((effect != null) && (effect.getLevel() < 7))
      {
        effect.addNumCharges(1);
        getEffected().sendPacket(new EtcStatusUpdate((L2PcInstance)getEffected()));
      }
    }
    return false;
  }
}