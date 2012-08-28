package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.skills.Env;

public class EffectTargetMe extends L2Effect
{
  public EffectTargetMe(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.TARGET_ME;
  }

  public void onStart()
  {
    getEffected().setTarget(getEffector());
    getEffected().sendPacket(new MyTargetSelected(getEffector().getObjectId(), 0));
    getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getEffector());
  }

  public void onExit()
  {
  }

  public boolean onActionTime()
  {
    return false;
  }
}