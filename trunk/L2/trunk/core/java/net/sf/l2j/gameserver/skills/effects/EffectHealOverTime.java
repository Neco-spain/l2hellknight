//L2DDT
package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.network.serverpackets.ExRegMax;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.skills.Env;


class EffectHealOverTime extends L2Effect
{
	public EffectHealOverTime(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public EffectType getEffectType()
	{
		return EffectType.HEAL_OVER_TIME;
	}

	@Override
	public boolean onStart()
	{
		getEffected().sendPacket(new ExRegMax(calc(), getTotalCount() * getPeriod(), getPeriod()));
		return true;
	}

	@Override
	public boolean onActionTime()
	{
		if(getEffected().isDead())
			return false;

		if(getEffected() instanceof L2DoorInstance)
			return false;

		double hp = getEffected().getCurrentHp();
		double maxhp = getEffected().getMaxHp();
		hp += calc();
		if(hp > maxhp)
		{
			hp = maxhp;
		}
		getEffected().setCurrentHp(hp);
		StatusUpdate suhp = new StatusUpdate(getEffected().getObjectId());
		suhp.addAttribute(StatusUpdate.CUR_HP, (int)hp);
		getEffected().sendPacket(suhp);
		return true;
	}
}
