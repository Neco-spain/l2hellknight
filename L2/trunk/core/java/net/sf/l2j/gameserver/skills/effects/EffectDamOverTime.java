package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Env;

class EffectDamOverTime extends L2Effect
{
	public EffectDamOverTime(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public EffectType getEffectType()
	{
		return EffectType.DMG_OVER_TIME;
	}

	@Override
	public boolean onActionTime()
	{
		if (getEffected().isDead())
			return false;

		
		double damage = calc();
		
		if (damage >= getEffected().getCurrentHp())
		{
			if (getSkill().isToggle())
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_HP);
				getEffected().sendPacket(sm);
				return false;
			}
            if (getSkill().getId() != 4082) damage = getEffected().getCurrentHp() - 1;
		}

        boolean awake = !(getEffected() instanceof L2Attackable)
        					&& !(getSkill().getTargetType() == SkillTargetType.TARGET_SELF
        							&& getSkill().isToggle());


        getEffected().reduceCurrentHp(damage, getEffector(),awake);

		return true;
	}
}
