/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.effecthandlers;

import l2.brick.gameserver.ai.CtrlIntention;
import l2.brick.gameserver.model.CharEffectList;
import l2.brick.gameserver.model.L2Effect;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.network.SystemMessageId;
import l2.brick.gameserver.skills.Env;
import l2.brick.gameserver.templates.effects.EffectTemplate;
import l2.brick.gameserver.templates.skills.L2EffectType;
import l2.brick.gameserver.templates.skills.L2SkillType;

public class ChameleonRest extends L2Effect
{
	public ChameleonRest(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	/**
	 * 
	 * @see l2.brick.gameserver.model.L2Effect#getEffectType()
	 */
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.RELAXING;
	}
	
	/**
	 * 
	 * @see l2.brick.gameserver.model.L2Effect#onStart()
	 */
	@Override
	public boolean onStart()
	{
		if (getEffected() instanceof L2PcInstance)
		{
			((L2PcInstance) getEffected()).sitDown(false);
		}
		else
			getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_REST);
		return super.onStart();
	}
	
	/**
	 * 
	 * @see l2.brick.gameserver.model.L2Effect#onExit()
	 */
	@Override
	public void onExit()
	{
		super.onExit();
	}
	
	/**
	 * 
	 * @see l2.brick.gameserver.model.L2Effect#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		if (getEffected().isDead())
			return false;
		
		// Only cont skills shouldn't end
		if (getSkill().getSkillType() != L2SkillType.CONT)
			return false;
		
		if (getEffected() instanceof L2PcInstance)
		{
			if (!((L2PcInstance) getEffected()).isSitting())
				return false;
		}
		
		double manaDam = calc();
		
		if (manaDam > getEffected().getCurrentMp())
		{
			getEffected().sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
			return false;
		}
		
		getEffected().reduceCurrentMp(manaDam);
		return true;
	}
	
	/* (non-Javadoc)
	 * @see l2.brick.gameserver.model.L2Effect#getEffectFlags()
	 */
	@Override
	public int getEffectFlags()
	{
		return (CharEffectList.EFFECT_FLAG_SILENT_MOVE | CharEffectList.EFFECT_FLAG_RELAXING);
	}
}
