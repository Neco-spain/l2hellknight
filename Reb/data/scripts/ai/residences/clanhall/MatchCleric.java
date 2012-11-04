package ai.residences.clanhall;

import l2r.gameserver.model.Skill;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.tables.SkillTable;

/**
 * @author VISTALL
 * @date 16:38/22.04.2011
 */
public class MatchCleric extends MatchFighter
{
	public static final Skill HEAL = SkillTable.getInstance().getInfo(4056, 6);

	public MatchCleric(NpcInstance actor)
	{
		super(actor);
	}

	public void heal()
	{
		NpcInstance actor = getActor();
		addTaskCast(actor, HEAL);
		doTask();
	}
}
