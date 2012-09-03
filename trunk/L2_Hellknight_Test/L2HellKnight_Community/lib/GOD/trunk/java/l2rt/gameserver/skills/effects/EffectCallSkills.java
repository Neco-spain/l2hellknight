package l2rt.gameserver.skills.effects;

import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Effect;
import l2rt.gameserver.model.L2Skill.AddedSkill;
import l2rt.gameserver.skills.Env;
import l2rt.util.GArray;

public class EffectCallSkills extends L2Effect
{
	public EffectCallSkills(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		for(AddedSkill as : getSkill().getAddedSkills())
		{
			GArray<L2Character> targets = new GArray<L2Character>();
			targets.add(getEffected());
			getEffector().callSkill(as.getSkill(), targets, false);
		}
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}