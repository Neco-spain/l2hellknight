package l2r.gameserver.skills.effects;

import l2r.gameserver.model.Effect;
import l2r.gameserver.stats.Env;

public class EffectMPDamPercent extends Effect
{
	public EffectMPDamPercent(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();

		if(_effected.isDead())
			return;

		double newMp = (100. - calc()) * _effected.getMaxMp() / 100.;
		newMp = Math.min(_effected.getCurrentMp(), Math.max(0, newMp));
		_effected.setCurrentMp(newMp);
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}