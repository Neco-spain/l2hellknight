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

import l2.hellknight.gameserver.model.L2Effect;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.instance.L2EffectPointInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.skills.Env;
import l2.hellknight.gameserver.templates.effects.EffectTemplate;
import l2.hellknight.gameserver.templates.skills.L2EffectType;

/**
 * @authors Forsaiken, Sami
 */
public class EffectSignetNoise extends L2Effect
{
	private L2EffectPointInstance _actor;
	
	public EffectSignetNoise(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	/**
	 * 
	 * @see l2.hellknight.gameserver.model.L2Effect#getEffectType()
	 */
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.SIGNET_GROUND;
	}
	
	/**
	 * 
	 * @see l2.hellknight.gameserver.model.L2Effect#onStart()
	 */
	@Override
	public boolean onStart()
	{
		_actor = (L2EffectPointInstance) getEffected();
		return true;
	}
	
	/**
	 * 
	 * @see l2.hellknight.gameserver.model.L2Effect#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		if (getCount() == getTotalCount() - 1)
			return true; // do nothing first time
		
		L2PcInstance caster = (L2PcInstance) getEffector();
		
		for (L2Character target : _actor.getKnownList().getKnownCharactersInRadius(getSkill().getSkillRadius()))
		{
			if (target == null || target == caster)
				continue;
			
			if (caster.canAttackCharacter(target))
			{
				L2Effect[] effects = target.getAllEffects();
				if (effects != null)
				{
					for (L2Effect effect : effects)
					{
						if (effect.getSkill().isDance())
							effect.exit();
					}
				}
			}
		}
		return true;
	}
	
	/**
	 * 
	 * @see l2.hellknight.gameserver.model.L2Effect#onExit()
	 */
	@Override
	public void onExit()
	{
		if (_actor != null)
			_actor.deleteMe();
	}
}
