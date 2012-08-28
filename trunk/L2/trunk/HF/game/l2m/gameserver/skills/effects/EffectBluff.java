package l2m.gameserver.skills.effects;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.network.serverpackets.FinishRotating;
import l2m.gameserver.network.serverpackets.L2GameServerPacket;
import l2m.gameserver.network.serverpackets.StartRotating;
import l2m.gameserver.skills.Env;

public final class EffectBluff extends Effect
{
  public EffectBluff(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public boolean checkCondition()
  {
    if ((getEffected().isNpc()) && (!getEffected().isMonster()))
      return false;
    return super.checkCondition();
  }

  public void onStart()
  {
    getEffected().broadcastPacket(new L2GameServerPacket[] { new StartRotating(getEffected(), getEffected().getHeading(), 1, 65535) });
    getEffected().broadcastPacket(new L2GameServerPacket[] { new FinishRotating(getEffected(), getEffector().getHeading(), 65535) });
    getEffected().setHeading(getEffector().getHeading());
  }

  public boolean isHidden()
  {
    return true;
  }

  public boolean onActionTime()
  {
    return false;
  }
}