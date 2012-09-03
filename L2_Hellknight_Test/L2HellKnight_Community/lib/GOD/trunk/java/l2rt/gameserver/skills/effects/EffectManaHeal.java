package l2rt.gameserver.skills.effects;

import l2rt.gameserver.model.L2Effect;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.skills.Env;
import l2rt.gameserver.skills.Stats;

public class EffectManaHeal extends L2Effect
{
	public EffectManaHeal(Env env, EffectTemplate template)
	{
		super(env, template);
		if(_effected.isDead() || _effected.isHealBlocked(true))
			return;
		double mp = calc();
		double newMp = Math.min(mp * 1.7, _effected.calcStat(Stats.MANAHEAL_EFFECTIVNESS, mp, _effector, getSkill()));
		double addToMp = Math.max(0, Math.min(newMp, _effected.calcStat(Stats.MP_LIMIT, null, null) * _effected.getMaxMp() / 100. - _effected.getCurrentMp()));
		_effected.sendPacket(new SystemMessage(SystemMessage.S1_MPS_HAVE_BEEN_RESTORED).addNumber(Math.round(addToMp)));
		if(addToMp > 0)
			_effected.setCurrentMp(addToMp + _effected.getCurrentMp());
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}