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

import l2.hellknight.gameserver.ai.CtrlEvent;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.L2Summon;
import l2.hellknight.gameserver.model.actor.instance.L2EffectPointInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.effects.EffectTemplate;
import l2.hellknight.gameserver.model.effects.L2Effect;
import l2.hellknight.gameserver.model.effects.L2EffectType;
import l2.hellknight.gameserver.model.stats.Env;
import l2.hellknight.gameserver.network.SystemMessageId;

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
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.SIGNET_GROUND;
	}
	
	@Override
	public boolean onStart()
	{
		_actor = (L2EffectPointInstance) getEffected();
		return true;
	}
	
	@Override
	public boolean onActionTime()
	{
		if (getCount() == getTotalCount() - 1)
			return true; // do nothing first time
		int mpConsume = getSkill().getMpConsume();
		
		L2PcInstance caster = getEffector().getActingPlayer();
		
		for (L2Character cha : _actor.getKnownList().getKnownCharactersInRadius(getSkill().getSkillRadius()))
		{
			if (cha == null)
				continue;
			
			if (cha.isPlayable())
			{
				if (caster.canAttackCharacter(cha))
				{
					L2PcInstance owner = null;
					if (cha.isSummon())
						owner = ((L2Summon) cha).getOwner();
					else
						owner = cha.getActingPlayer();
					
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
	
	@Override
	public void onExit()
	{
		if (_actor != null)
			_actor.deleteMe();
	}
}
