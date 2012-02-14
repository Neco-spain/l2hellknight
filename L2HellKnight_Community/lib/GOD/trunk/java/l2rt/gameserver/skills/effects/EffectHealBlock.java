package l2rt.gameserver.skills.effects;

import l2rt.gameserver.model.L2Effect;
import l2rt.gameserver.skills.Env;

public final class EffectHealBlock extends L2Effect
{
	public EffectHealBlock(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public boolean checkCondition()
	{
		if(_effected.isHealBlocked(true))
			return false;
		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		_effected.setHealBlocked(true);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		_effected.setHealBlocked(false);
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}