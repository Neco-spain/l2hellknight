package l2rt.gameserver.skills.effects;

import l2rt.gameserver.model.L2Effect;
import l2rt.gameserver.skills.Env;

public class EffectCPDamPercent extends L2Effect
{
	public EffectCPDamPercent(final Env env, final EffectTemplate template)
	{
		super(env, template);
		if(_effected.isDead())
			return;
		double newCp = (100 - calc()) * _effected.getMaxCp() / 100;
		newCp = Math.min(_effected.getCurrentCp(), Math.max(0, newCp));
		_effected.setCurrentCp(newCp);
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}