package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.skills.Env;

final class EffectSignetAntiSummon extends L2Effect
{
  public EffectSignetAntiSummon(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.SIGNET_GROUND;
  }

  public void onStart()
  {
    if (!getEffected().isPlayer()) {
      return;
    }
    L2PcInstance pc = (L2PcInstance)getEffected();
    pc.setNoSummon(true);
    if (pc.getPet() != null)
      pc.getPet().unSummon(pc);
  }

  public boolean onActionTime()
  {
    if (!getEffected().isPlayer()) {
      return false;
    }
    ((L2PcInstance)getEffected()).setNoSummon(false);
    return false;
  }

  public void onExit()
  {
  }
}