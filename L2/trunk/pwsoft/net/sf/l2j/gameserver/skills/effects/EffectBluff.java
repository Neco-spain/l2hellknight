package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.network.serverpackets.BeginRotation;
import net.sf.l2j.gameserver.network.serverpackets.StopRotation;
import net.sf.l2j.gameserver.skills.Env;

final class EffectBluff extends L2Effect
{
  public EffectBluff(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.BLUFF;
  }

  public void onStart()
  {
    if (getEffected().isRaid()) {
      return;
    }
    getEffected().broadcastPacket(new BeginRotation(getEffected(), getEffected().getHeading(), 1, 65535));
    getEffected().broadcastPacket(new StopRotation(getEffected(), getEffector().getHeading(), 65535));
    getEffected().setHeading(getEffector().getHeading());
    getEffected().startStunning();
  }

  public void onExit()
  {
    getEffected().stopStunning(this);
  }

  public boolean onActionTime()
  {
    onExit();
    return false;
  }
}