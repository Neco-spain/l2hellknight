package l2rt.gameserver.skills.effects;

import l2rt.gameserver.model.L2Effect;
import l2rt.gameserver.skills.Env;

public final class EffectStun extends L2Effect
{
	public EffectStun(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		_effected.startStunning();
	}

	@Override
	public void onExit()
	{
		super.onExit();
		_effected.stopStunning();
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}