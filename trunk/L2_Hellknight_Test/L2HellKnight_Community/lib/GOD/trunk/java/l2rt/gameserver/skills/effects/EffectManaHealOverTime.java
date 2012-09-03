package l2rt.gameserver.skills.effects;

import l2rt.gameserver.model.L2Effect;
import l2rt.gameserver.skills.Env;
import l2rt.gameserver.skills.Stats;

public class EffectManaHealOverTime extends L2Effect
{
	public EffectManaHealOverTime(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean onActionTime()
	{
		if(_effected.isDead() || _effected.isHealBlocked(true))
			return false;
		double addToMp = Math.max(0, Math.min(calc(), _effected.calcStat(Stats.MP_LIMIT, null, null) * _effected.getMaxMp() / 100. - _effected.getCurrentMp()));
		if(addToMp > 0)
			_effected.setCurrentMp(_effected.getCurrentMp() + addToMp);
		return true;
	}
}