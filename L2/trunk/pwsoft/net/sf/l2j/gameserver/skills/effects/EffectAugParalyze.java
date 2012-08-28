package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.skills.Env;

final class EffectAugParalyze extends L2Effect
{
  public EffectAugParalyze(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.PARALYZE;
  }

  public void onStart()
  {
    getEffector().broadcastPacket(new MagicSkillUser(getEffector(), getEffected(), 5144, 1, 0, 0));
    getEffected().startAbnormalEffect(1024);
    getEffected().setIsParalyzed(true);
    getEffected().setIsInvul(true);
  }

  public void onExit()
  {
    getEffected().stopAbnormalEffect(1024);
    getEffected().setIsParalyzed(false);
    getEffected().setIsInvul(false);
  }

  public boolean onActionTime()
  {
    getEffected().setIsInvul(false);
    return false;
  }
}