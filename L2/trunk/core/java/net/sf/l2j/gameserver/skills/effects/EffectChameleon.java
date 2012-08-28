package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Env;


class EffectChameleon extends L2Effect
{
	public EffectChameleon(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public EffectType getEffectType()
	{
		return EffectType.CHAMELEON;
	}

	@Override
	public boolean onStart()
	{

        if(getEffected() instanceof L2PcInstance)
        {
        	((L2PcInstance)getEffected()).setSilentMoving(true);
			((L2PcInstance)getEffected()).setRelax(true);
        	((L2PcInstance)getEffected()).sitDown();
        }
        else
        	getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_REST);
		return super.onStart();
	}

	@Override
	public void onExit()
	{
        ((L2PcInstance)getEffected()).setSilentMoving(false);
		((L2PcInstance)getEffected()).setRelax(false);
		super.onExit();
	}

	@Override
	public boolean onActionTime()
	{
        boolean retval = true;
		if(getEffected().isDead())
            retval = false;

		if(getEffected() instanceof L2PcInstance)
		{
			if(!((L2PcInstance)getEffected()).isSitting())
				retval = false;
		}

		double manaDam = calc();

		if(manaDam > getEffected().getCurrentMp())
		{
			if(getSkill().isToggle())
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
				getEffected().sendPacket(sm);
				if (getEffected() instanceof L2PcInstance)
					((L2PcInstance)getEffected()).standUp();
                retval = false;
			}
		}

        if (!retval)
		{
            ((L2PcInstance)getEffected()).setSilentMoving(retval);
			((L2PcInstance)getEffected()).setRelax(retval);
		}
        else
            getEffected().reduceCurrentMp(manaDam);

        return retval;
	}
}
