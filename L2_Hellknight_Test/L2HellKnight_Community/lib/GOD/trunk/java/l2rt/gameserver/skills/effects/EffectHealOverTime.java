package l2rt.gameserver.skills.effects;

import l2rt.gameserver.model.L2Effect;
import l2rt.gameserver.network.serverpackets.ExRegenMax;
import l2rt.gameserver.skills.Env;
import l2rt.gameserver.skills.Stats;

public class EffectHealOverTime extends L2Effect
{
	public EffectHealOverTime(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();

		if(getEffected().isPlayer() && getCount() > 0 && getPeriod() > 0)
			getEffected().sendPacket(new ExRegenMax(calc(), (int) (getCount() * getPeriod() / 1000), Math.round(getPeriod() / 1000)));

		/**
			switch(getSkill().getId().intValue())
			{
				case 2031: // Lesser Healing Potion
					getEffected().sendPacket(new ExRegenMax(ExRegenMax.POTION_HEALING_LESSER));
					break;
				case 2032: // Healing Potion
					getEffected().sendPacket(new ExRegenMax(ExRegenMax.POTION_HEALING_MEDIUM));
					break;
				case 2037: // Greater Healing Potion
					getEffected().sendPacket(new ExRegenMax(ExRegenMax.POTION_HEALING_GREATER));
					break;
			}
		*/
	}

	@Override
	public boolean onActionTime()
	{
		if(_effected.isDead() || _effected.isHealBlocked(true))
			return false;

		double newHp = calc() * _effected.calcStat(Stats.HEAL_EFFECTIVNESS, 100, _effector, getSkill()) / 100;
		double addToHp = Math.max(0, Math.min(newHp, _effected.calcStat(Stats.HP_LIMIT, null, null) * _effected.getMaxHp() / 100. - _effected.getCurrentHp()));

		if(addToHp > 0)
			getEffected().setCurrentHp(_effected.getCurrentHp() + addToHp, false);
		return true;
	}
}