package l2r.gameserver.skills.skillclasses;

import java.util.List;

import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Skill;
import l2r.gameserver.templates.StatsSet;

public class Effect extends Skill
{
	public Effect(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
			if(target != null)
				getEffects(activeChar, target, false, false);
	}
}