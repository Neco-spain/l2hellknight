package l2rt.gameserver.skills.effects;

import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Effect;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.skills.Env;
import l2rt.gameserver.skills.Stats;

public final class EffectFakeDeath extends L2Effect
{
	public EffectFakeDeath(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		getEffected().startFakeDeath();
	}

	@Override
	public void onExit()
	{
		super.onExit();
		// 5 секунд после FakeDeath на персонажа не агрятся мобы
		getEffected().setNonAggroTime(System.currentTimeMillis() + 5000);
		getEffected().stopFakeDeath();
	}

	@Override
	public boolean onActionTime()
	{
		if(getEffected().isDead())
			return false;

		double manaDam = calc();
		if(getSkill().isMagic())
			manaDam = _effected.calcStat(Stats.MP_MAGIC_SKILL_CONSUME, manaDam, null, getSkill());
		else
			manaDam = _effected.calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, manaDam, null, getSkill());

		if(manaDam > getEffected().getCurrentMp())
			if(getSkill().isToggle())
			{
				getEffected().sendPacket(Msg.NOT_ENOUGH_MP);
				getEffected().sendPacket(new SystemMessage(SystemMessage.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(getSkill().getId(), getSkill().getDisplayLevel()));
				return false;
			}

		getEffected().reduceCurrentMp(manaDam, null);
		return true;
	}
}