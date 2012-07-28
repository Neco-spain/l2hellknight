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

import l2.brick.gameserver.ai.CtrlEvent;
import l2.brick.gameserver.model.L2Effect;
import l2.brick.gameserver.model.actor.L2Character;
import l2.brick.gameserver.model.actor.L2Playable;
import l2.brick.gameserver.model.actor.L2Summon;
import l2.brick.gameserver.model.actor.instance.L2EffectPointInstance;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.network.SystemMessageId;
import l2.brick.gameserver.skills.Env;
import l2.brick.gameserver.templates.effects.EffectTemplate;
import l2.brick.gameserver.templates.skills.L2EffectType;

/**
 * @author Forsaiken
 */
public class SignetAntiSummon extends L2Effect
{
	private L2EffectPointInstance _actor;
	
	public SignetAntiSummon(Env env, EffectTemplate template)
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
		return L2EffectType.SIGNET_GROUND;
	}
	
	/**
	 * 
	 * @see l2.brick.gameserver.model.L2Effect#onStart()
	 */
	@Override
	public boolean onStart()
	{
		_actor = (L2EffectPointInstance) getEffected();
		return true;
	}
	
	/**
	 * 
	 * @see l2.brick.gameserver.model.L2Effect#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		if (getCount() == getTotalCount() - 1)
			return true; // do nothing first time
		int mpConsume = getSkill().getMpConsume();
		
		L2PcInstance caster = (L2PcInstance) getEffector();
		
		for (L2Character cha : _actor.getKnownList().getKnownCharactersInRadius(getSkill().getSkillRadius()))
		{
			if (cha == null)
				continue;
			
			if (cha instanceof L2Playable)
			{
				if (caster.canAttackCharacter(cha))
				{
					L2PcInstance owner = null;
					if (cha instanceof L2Summon)
						owner = ((L2Summon) cha).getOwner();
					else
						owner = (L2PcInstance) cha;
					
					if (owner != null && owner.getPet() != null)
					{
						if (mpConsume > getEffector().getCurrentMp())
						{
							getEffector().sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
							return false;
						}
						
						getEffector().reduceCurrentMp(mpConsume);
						owner.getPet().unSummon(owner);
						owner.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, getEffector());
					}
				}
			}
		}
		return true;
	}
	
	/**
	 * 
	 * @see l2.brick.gameserver.model.L2Effect#onExit()
	 */
	@Override
	public void onExit()
	{
		if (_actor != null)
			_actor.deleteMe();
	}
}
