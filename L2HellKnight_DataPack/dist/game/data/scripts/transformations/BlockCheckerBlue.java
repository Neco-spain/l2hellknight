package transformations;

import l2.brick.gameserver.datatables.SkillTable;
import l2.brick.gameserver.instancemanager.TransformationManager;
import l2.brick.gameserver.model.L2Transformation;

public class BlockCheckerBlue extends L2Transformation
{
	private static final int[] SKILLS = {619};
	public BlockCheckerBlue()
	{
		// id, colRadius, colHeight
		super(127, 8, 20.3);
	}
	
	@Override
	public void onTransform()
	{
		if (getPlayer().getTransformationId() != 127 || getPlayer().isCursedWeaponEquipped())
			return;
		
		transformedSkills();
	}
	
	public void transformedSkills()
	{
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
		// Transform Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);
		
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
	
	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new BlockCheckerBlue());
	}
}
