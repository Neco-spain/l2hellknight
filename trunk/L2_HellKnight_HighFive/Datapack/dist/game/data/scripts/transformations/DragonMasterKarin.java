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

/**
 * @author Tan
 */
public class DragonMasterKarin extends L2Transformation
{
	private static final int[] SKILLS =
	{
		5491, 619, 20003, 20004, 20005
	};
	
	public DragonMasterKarin()
	{
		// id, colRadius, colHeight
		super(20006, 8, 18.6);
	}
	
	@Override
	public void onTransform()
	{
		if ((getPlayer().getTransformationId() != 20006) || getPlayer().isCursedWeaponEquipped())
		{
			return;
		}
		
		transformedSkills();
	}
	
	public void transformedSkills()
	{
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		// Transform Dispel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Dragon Slash
		getPlayer().addSkill(SkillTable.getInstance().getInfo(20003, 1), false);
		// Dragon Dash
		getPlayer().addSkill(SkillTable.getInstance().getInfo(20004, 1), false);
		// Dragon Aura
		getPlayer().addSkill(SkillTable.getInstance().getInfo(20005, 1), false);
		
		getPlayer().setTransformAllowedSkills(SKILLS);
	}
	
	@Override
	public void onUntransform()
	{
		removeSkills();
	}
	
	public void removeSkills()
	{
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		// Transform Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Dragon Slash
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(20003, 1), false);
		// Dragon Dash
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(20004, 1), false);
		// Dragon Aura
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(20005, 1), false);
		
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
	
	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new DragonMasterKarin());
	}
}
