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

public class Benom extends L2Transformation
{
	private static final int[] SKILLS = new int[] { 725, 726, 727, 5491, 619 };
	
	public Benom()
	{
		// id, colRadius, colHeight
		super(307, 20, 56);
	}
	
	@Override
	public void onTransform()
	{
		if (getPlayer().getTransformationId() != 307 || getPlayer().isCursedWeaponEquipped())
			return;
		
		transformedSkills();
	}
	
	public void transformedSkills()
	{
		// Benom Power Smash
		getPlayer().addSkill(SkillTable.getInstance().getInfo(725, 1), false);
		// Benom Sonic Storm
		getPlayer().addSkill(SkillTable.getInstance().getInfo(726, 1), false);
		// Benom Disillusion
		getPlayer().addSkill(SkillTable.getInstance().getInfo(727, 1), false);
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
		// Benom Power Smash
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(725, 1), false);
		// Benom Sonic Storm
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(726, 1), false);
		// Benom Disillusion
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(727, 1), false, false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		// Transform Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);
		
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
	
	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new Benom());
	}
}
