package l2rt.gameserver.skills.effects;

import l2rt.gameserver.model.L2Effect;
import l2rt.gameserver.skills.Env;

public class EffectCharmOfCourage extends L2Effect
{
	public EffectCharmOfCourage(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(_effected.isPlayer())
			_effected.getPlayer().setCharmOfCourage(true);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		_effected.getPlayer().setCharmOfCourage(false);
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}