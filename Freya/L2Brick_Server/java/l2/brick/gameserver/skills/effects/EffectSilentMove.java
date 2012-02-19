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
package l2.brick.gameserver.skills.effects;

import l2.brick.gameserver.model.CharEffectList;
import l2.brick.gameserver.model.L2Effect;
import l2.brick.gameserver.network.SystemMessageId;
import l2.brick.gameserver.network.serverpackets.SystemMessage;
import l2.brick.gameserver.skills.Env;
import l2.brick.gameserver.templates.EffectTemplate;
import l2.brick.gameserver.templates.L2EffectType;
import l2.brick.gameserver.templates.L2SkillType;

public class EffectSilentMove extends L2Effect
{
	public EffectSilentMove(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	// Special constructor to steal this effect
	public EffectSilentMove(Env env, L2Effect effect)
	{
		super(env, effect);
	}
	
	/**
	 * 
	 * @see l2.brick.gameserver.model.L2Effect#effectCanBeStolen()
	 */
	@Override
	protected boolean effectCanBeStolen()
	{
		return true;
	}

	/**
	 * 
	 * @see l2.brick.gameserver.model.L2Effect#onStart()
	 */
	@Override
	public boolean onStart()
	{
		super.onStart();
		return true;
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
	 * @see l2.brick.gameserver.model.L2Effect#getEffectType()
	 */
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.SILENT_MOVE;
	}
	
	/**
	 * 
	 * @see l2.brick.gameserver.model.L2Effect#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		// Only cont skills shouldn't end
		if (getSkill().getSkillType() != L2SkillType.CONT)
			return false;
		
		if (getEffected().isDead())
			return false;
		
		double manaDam = calc();
		
		if (manaDam > getEffected().getCurrentMp())
		{
			getEffected().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP));
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
		return CharEffectList.EFFECT_FLAG_SILENT_MOVE;
	}
}
