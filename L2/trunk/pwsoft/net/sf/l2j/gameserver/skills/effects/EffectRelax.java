package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Env;

class EffectRelax extends L2Effect
{
  public EffectRelax(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.RELAXING;
  }

  public void onStart()
  {
    if (getEffected().isPlayer())
    {
      setRelax(true);
      ((L2PcInstance)getEffected()).sitDown();
    }
    else {
      getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_REST);
    }super.onStart();
  }

  public void onExit()
  {
    setRelax(false);
    super.onExit();
  }

  public boolean onActionTime()
  {
    boolean retval = true;
    if (getEffected().isDead()) {
      retval = false;
    }
    if (getEffected().isPlayer())
    {
      if (!((L2PcInstance)getEffected()).isSitting()) {
        retval = false;
      }
    }
    if ((getEffected().getCurrentHp() + 1.0D > getEffected().getMaxHp()) && 
      (getSkill().isToggle()))
    {
      getEffected().sendPacket(SystemMessage.id(SystemMessageId.S1_S2).addString("Fully rested. Effect of " + getSkill().getName() + " has been removed."));

      retval = false;
    }

    double manaDam = calc();

    if (manaDam > getEffected().getCurrentMp())
    {
      if (getSkill().isToggle())
      {
        getEffected().sendPacket(Static.SKILL_REMOVED_DUE_LACK_MP);

        retval = false;
      }
    }

    if (!retval)
      setRelax(retval);
    else {
      getEffected().reduceCurrentMp(manaDam);
    }
    return retval;
  }

  private void setRelax(boolean val)
  {
    if (getEffected().isPlayer())
      ((L2PcInstance)getEffected()).setRelax(val);
  }
}