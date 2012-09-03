package l2rt.gameserver.skills.effects;

import l2rt.gameserver.model.L2Effect;
import l2rt.gameserver.skills.Env;

public final class EffectPetrification extends L2Effect
{
	public EffectPetrification(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean checkCondition()
	{
		if(_effected.isParalyzeImmune())
			return false;
		if(_effected.isParalyzed() || _effected.isInvul())
			return false;
		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		_effected.setParalyzed(true);
		_effected.setIsInvul(true);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		_effected.setParalyzed(false);
		_effected.setIsInvul(false);
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}