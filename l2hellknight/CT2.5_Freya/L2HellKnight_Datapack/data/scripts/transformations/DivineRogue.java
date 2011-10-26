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

package transformations;

import l2.hellknight.gameserver.datatables.SkillTable;
import l2.hellknight.gameserver.instancemanager.TransformationManager;
import l2.hellknight.gameserver.model.L2Transformation;

public class DivineRogue extends L2Transformation
{
	private static final int[] SKILLS = { 686, 687, 688, 689, 690, 691, 797, 5491, 619 };
	
	public DivineRogue()
	{
		// id, colRadius, colHeight
		super(254, 10, 28);
	}
	
	@Override
	public void onTransform()
	{
		if (getPlayer().getTransformationId() != 254 || getPlayer().isCursedWeaponEquipped())
			return;
		
		transformedSkills();
	}
	
	public void transformedSkills()
	{
		// Divine Rogue Stun Shot
		getPlayer().addSkill(SkillTable.getInstance().getInfo(686, 1), false);
		// Divine Rogue Double Shot
		getPlayer().addSkill(SkillTable.getInstance().getInfo(687, 1), false);
		// Divine Rogue Bleed Attack
		getPlayer().addSkill(SkillTable.getInstance().getInfo(688, 1), false);
		// Divine Rogue Deadly Blow
		getPlayer().addSkill(SkillTable.getInstance().getInfo(689, 1), false);
		// Divine Rogue Agility
		getPlayer().addSkill(SkillTable.getInstance().getInfo(690, 1), false);
		// Sacrifice Rogue
		getPlayer().addSkill(SkillTable.getInstance().getInfo(691, 1), false);
		// Divine Rogue Piercing Attack
		getPlayer().addSkill(SkillTable.getInstance().getInfo(797, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		// Transform Dispel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(619, 1), false);
		
		getPlayer().setTransformAllowedSkills(SKILLS);
	}
	
	@Override
	public void onUntransform()
	{
		removeSkills();
	}
	
	public void removeSkills()
	{
		// Divine Rogue Stun Shot
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(686, 1), false);
		// Divine Rogue Double Shot
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(687, 1), false);
		// Divine Rogue Bleed Attack
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(688, 1), false);
		// Divine Rogue Deadly Blow
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(689, 1), false);
		// Divine Rogue Agility
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(690, 1), false, false);
		// Sacrifice Rogue
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(691, 1), false, false);
		// Divine Rogue Piercing Attack
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(797, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		// Transform Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);
		
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
	
	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new DivineRogue());
	}
}
