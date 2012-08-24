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
package l2.hellknight.gameserver.model.skills.l2skills;

import l2.hellknight.gameserver.model.L2Object;
import l2.hellknight.gameserver.model.StatsSet;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.effects.L2Effect;
import l2.hellknight.gameserver.model.items.instance.L2ItemInstance;
import l2.hellknight.gameserver.model.skills.L2Skill;
import l2.hellknight.gameserver.model.stats.Env;
import l2.hellknight.gameserver.model.stats.Formulas;

public class L2SkillElemental extends L2Skill {
	
	private final int[] _seeds;
	private final boolean _seedAny;
	
	public L2SkillElemental(StatsSet set) {
		super(set);
		
		_seeds = new int[3];
		_seeds[0] = set.getInteger("seed1",0);
		_seeds[1] = set.getInteger("seed2",0);
		_seeds[2] = set.getInteger("seed3",0);
		
		if (set.getInteger("seed_any",0)==1)
			_seedAny = true;
		else
			_seedAny = false;
	}
	
	@Override
	public void useSkill(L2Character activeChar, L2Object[] targets) {
		if (activeChar.isAlikeDead())
			return;
		
		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		
		if (activeChar.isPlayer())
		{
			if (weaponInst == null)
			{
				activeChar.sendMessage("You must equip your weapon before casting a spell.");
				return;
			}
		}
		
		for (L2Character target: (L2Character[]) targets)
		{
			if (target.isAlikeDead())
				continue;
			
			boolean charged = true;
			if (!_seedAny){
				for (int i=0;i<_seeds.length;i++){
					if (_seeds[i]!=0){
						L2Effect e = target.getFirstEffect(_seeds[i]);
						if (e==null || !e.getInUse()){
							charged = false;
							break;
						}
					}
				}
			}
			else {
				charged = false;
				for (int i=0;i<_seeds.length;i++){
					if (_seeds[i]!=0){
						L2Effect e = target.getFirstEffect(_seeds[i]);
						if (e!=null && e.getInUse()){
							charged = true;
							break;
						}
					}
				}
			}
			if (!charged)
			{
				activeChar.sendMessage("Target is not charged by elements.");
				continue;
			}
			
			boolean mcrit = Formulas.calcMCrit(activeChar.getMCriticalHit(target, this));
			byte shld = Formulas.calcShldUse(activeChar, target, this);
			
			int damage = (int)Formulas.calcMagicDam(
					activeChar, target, this, shld, activeChar.isSpiritshotCharged(this), activeChar.isBlessedSpiritshotCharged(this), mcrit);
			
			if (damage > 0)
			{
				target.reduceCurrentHp(damage, activeChar, this);
				
				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}
				
				activeChar.sendDamageMessage(target, damage, false, false, false);
				
			}
			
			// activate attacked effects, if any
			target.stopSkillEffects(getId());
			getEffects(activeChar, target, new Env(shld, activeChar.isSpiritshotCharged(this), false, activeChar.isBlessedSpiritshotCharged(this)));
		}
		
		activeChar.spsUncharge(this);
	}
}
