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

public class Rabbit extends L2Transformation
{
	private static final int[] SKILLS =
	{
		629, 630, 5491, 619
	};
	
	public Rabbit()
	{
		// id, colRadius, colHeight
		super(105, 5, 4.5);
	}
	
	@Override
	public void onTransform()
	{
		if ((getPlayer().getTransformationId() != 105) || getPlayer().isCursedWeaponEquipped())
		{
			return;
		}
		
		transformedSkills();
	}
	
	public void transformedSkills()
	{
		// Rabbit Magic Eye
		getPlayer().addSkill(SkillTable.getInstance().getInfo(629, 1), false);
		// Rabbit Tornado
		getPlayer().addSkill(SkillTable.getInstance().getInfo(630, 1), false);
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
		// Rabbit Magic Eye
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(629, 1), false);
		// Rabbit Tornado
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(630, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		// Transform Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);
		
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
	
	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new Rabbit());
	}
}
