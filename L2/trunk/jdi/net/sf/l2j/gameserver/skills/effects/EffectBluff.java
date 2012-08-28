package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.actor.instance.L2FolkInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SiegeSummonInstance;
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

  public boolean onStart()
  {
    if ((getEffected() instanceof L2FolkInstance)) return false;

    if (((getEffected() instanceof L2NpcInstance)) && (((L2NpcInstance)getEffected()).getNpcId() == 35062))
      return false;
    if ((getEffected() instanceof L2SiegeSummonInstance))
      return false;
    getEffected().broadcastPacket(new BeginRotation(getEffected().getObjectId(), getEffected().getHeading(), 1, 65535));
    getEffected().broadcastPacket(new StopRotation(getEffected().getObjectId(), getEffector().getHeading(), 65535));
    getEffected().setHeading(getEffector().getHeading());
    onActionTime();
    return true;
  }

  public boolean onActionTime()
  {
    return false;
  }
}