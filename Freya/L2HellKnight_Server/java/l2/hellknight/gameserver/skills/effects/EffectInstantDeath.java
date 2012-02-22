package l2.hellknight.gameserver.skills.effects;

import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.model.L2Effect;
import l2.hellknight.gameserver.skills.Env;
import l2.hellknight.gameserver.templates.EffectTemplate;
import l2.hellknight.gameserver.templates.L2EffectType;

public class EffectInstantDeath extends L2Effect
{
	public EffectInstantDeath(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	/**
	 * 
	 * @see l2.hellknight.gameserver.model.L2Effect#getEffectType()
	 */
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.INSTANT_DEATH;
	}
	
	/**
	 * 
	 * @see l2.hellknight.gameserver.model.L2Effect#onStart()
	 */
	@Override
	public boolean onStart()
	{
		getEffected().setTarget(null);
		getEffected().abortAttack();
		getEffected().abortCast();
		getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, getEffector());
		getEffected().doDie(null);
		return true;
	}
	
	/**
	 * 
	 * @see l2.hellknight.gameserver.model.L2Effect#onExit()
	 */
	@Override
	public void onExit()
	{
		// nothing
	}
	
	/**
	 * 
	 * @see l2.hellknight.gameserver.model.L2Effect#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		// nothing
		return false;
	}
}