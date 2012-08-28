package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.skills.Env;

final class EffectBetray extends L2Effect
{
  public EffectBetray(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.BETRAY;
  }

  public boolean onStart()
  {
    if ((getEffected() != null) && ((getEffector() instanceof L2PcInstance)) && ((getEffected() instanceof L2Summon)))
    {
      L2PcInstance targetOwner = null;
      targetOwner = ((L2Summon)getEffected()).getOwner();
      getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, targetOwner);
      targetOwner.setIsBetrayed(true);
      onActionTime();
      return true;
    }
    return false;
  }

  public void onExit()
  {
    if ((getEffected() != null) && ((getEffector() instanceof L2PcInstance)) && ((getEffected() instanceof L2Summon)))
    {
      L2PcInstance targetOwner = null;
      targetOwner = ((L2Summon)getEffected()).getOwner();
      targetOwner.setIsBetrayed(false);
      getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
    }
  }

  public boolean onActionTime()
  {
    L2PcInstance targetOwner = null;
    targetOwner = ((L2Summon)getEffected()).getOwner();
    targetOwner.setIsBetrayed(true);
    return false;
  }
}