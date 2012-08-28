package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Env;

public final class EffectBlockBuff extends L2Effect
{
	public EffectBlockBuff(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public EffectType getEffectType()
	{
		return EffectType.BLOCK_BUFF;
	}
	
	@Override
	public boolean onActionTime()
	{
		// Only cont skills shouldn't end
		if(getSkill().getSkillType() != SkillType.CONT)
			return false;

		double manaDam = calc();

		if(manaDam > getEffected().getCurrentMp())
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
			getEffected().sendPacket(sm);
			return false;
		}

		getEffected().reduceCurrentMp(manaDam);
		return true;
	}
	
	@Override
	public boolean onStart()
	{
		getEffected().setIsBuffBlocked(true);
		return true;
	}
	
	@Override
	public void onExit()
	{
		getEffected().setIsBuffBlocked(false);
	}
}