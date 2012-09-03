package transformations;

import l2.hellknight.gameserver.datatables.SkillTable;
import l2.hellknight.gameserver.instancemanager.TransformationManager;
import l2.hellknight.gameserver.model.L2Transformation;

public class InquisitorShilienElder extends L2Transformation
{
	public InquisitorShilienElder()
	{
		// id
		super(318);
	}
	
	@Override
	public void onTransform()
	{
		if (getPlayer().getTransformationId() != 318 || getPlayer().isCursedWeaponEquipped())
			return;
		
		transformedSkills();
	}
	
	public void transformedSkills()
	{
		if (getPlayer().getLevel() > 40)
		{
			// Divine Punishment
			getPlayer().addSkill(SkillTable.getInstance().getInfo(1523, getPlayer().getLevel() - 40), false);
			// Divine Flash
			getPlayer().addSkill(SkillTable.getInstance().getInfo(1528, getPlayer().getLevel() - 40), false);
			// Holy Weapon
			getPlayer().addSkill(SkillTable.getInstance().getInfo(1043, 1), false);
			// Surrender to the Holy
			getPlayer().addSkill(SkillTable.getInstance().getInfo(1524, getPlayer().getLevel() - 40), false);
			// Divine Curse
			getPlayer().addSkill(SkillTable.getInstance().getInfo(1525, getPlayer().getLevel() - 40), false);
			getPlayer().setTransformAllowedSkills(new int[]{838,1523,1528,1524,1525,1430,1303,1059,1043});
		}
		else
			getPlayer().setTransformAllowedSkills(new int[]{838,1430,1303,1059});
		// Switch Stance
		getPlayer().addSkill(SkillTable.getInstance().getInfo(838, 1), false);
	}
	
	@Override
	public void onUntransform()
	{
		removeSkills();
	}
	
	public void removeSkills()
	{
		// Divine Punishment
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(1523, getPlayer().getLevel() - 40), false);
		// Divine Flash
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(1528, getPlayer().getLevel() - 40), false);
		// Holy Weapon
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(1043, 1), false, false);
		// Surrender to the Holy
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(1524, getPlayer().getLevel() - 40), false);
		// Divine Curse
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(1525, getPlayer().getLevel() - 40), false);
		// Switch Stance
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(838, 1), false);
		
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
	
	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new InquisitorShilienElder());
	}
}
