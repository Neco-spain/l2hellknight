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

public class Kamael extends L2Transformation
{
	private static final int[] SKILLS = { 539, 540, 1471, 1472, 5491, 619 };
	
	public Kamael()
	{
		// id, duration (secs), colRadius, colHeight
		super(251, 9, 38);
	}
	
	@Override
	public void onTransform()
	{
		if (getPlayer().getTransformationId() != 251 || getPlayer().isCursedWeaponEquipped())
			return;
		
		transformedSkills();
	}
	
	public void transformedSkills()
	{
		// Nail Attack
		getPlayer().addSkill(SkillTable.getInstance().getInfo(539, 1), false);
		// Wing Assault
		getPlayer().addSkill(SkillTable.getInstance().getInfo(540, 1), false);
		// Soul Sucking
		getPlayer().addSkill(SkillTable.getInstance().getInfo(1471, 1), false);
		// Death Beam
		getPlayer().addSkill(SkillTable.getInstance().getInfo(1472, 1), false);
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
		// Nail Attack
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(539, 1), false);
		// Wing Assault
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(540, 1), false);
		// Soul Sucking
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(1471, 1), false);
		// Death Beam
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(1472, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		// Transform Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);
		
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
	
	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new Kamael());
	}
}
