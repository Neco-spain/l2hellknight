package l2rt.gameserver.skills.effects;

import l2rt.gameserver.model.L2Effect;
import l2rt.gameserver.skills.Env;

public final class EffectImobileBuff extends L2Effect
{
	public EffectImobileBuff(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		_effected.setImobilised(true);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		_effected.setImobilised(false);
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
