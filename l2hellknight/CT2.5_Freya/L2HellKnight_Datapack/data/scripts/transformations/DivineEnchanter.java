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

public class DivineEnchanter extends L2Transformation
{
	private static final int[] SKILLS = { 704, 705, 706, 707, 708, 709, 5779, 619 };
	
	public DivineEnchanter()
	{
		// id, colRadius, colHeight
		super(257, 8, 18.25);
	}
	
	@Override
	public void onTransform()
	{
		if (getPlayer().getTransformationId() != 257 || getPlayer().isCursedWeaponEquipped())
			return;
		
		transformedSkills();
	}
	
	public void transformedSkills()
	{
		// Divine Enchanter Water Spirit
		getPlayer().addSkill(SkillTable.getInstance().getInfo(704, 1), false);
		// Divine Enchanter Fire Spirit
		getPlayer().addSkill(SkillTable.getInstance().getInfo(705, 1), false);
		// Divine Enchanter Wind Spirit
		getPlayer().addSkill(SkillTable.getInstance().getInfo(706, 1), false);
		// Divine Enchanter Hero Spirit
		getPlayer().addSkill(SkillTable.getInstance().getInfo(707, 1), false);
		// Divine Enchanter Mass Binding
		getPlayer().addSkill(SkillTable.getInstance().getInfo(708, 1), false);
		// Sacrifice Enchanter
		getPlayer().addSkill(SkillTable.getInstance().getInfo(709, 1), false);
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
		// Divine Enchanter Water Spirit
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(704, 1), false, false);
		// Divine Enchanter Fire Spirit
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(705, 1), false, false);
		// Divine Enchanter Wind Spirit
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(706, 1), false, false);
		// Divine Enchanter Hero Spirit
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(707, 1), false, false);
		// Divine Enchanter Mass Binding
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(708, 1), false, false);
		// Sacrifice Enchanter
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(709, 1), false, false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		// Transform Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);
		
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
	
	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new DivineEnchanter());
	}
}
