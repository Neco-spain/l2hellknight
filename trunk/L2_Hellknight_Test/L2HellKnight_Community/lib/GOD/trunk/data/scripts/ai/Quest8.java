package ai;

import static l2rt.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;
import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.ai.CtrlIntention;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2World;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.network.serverpackets.MagicSkillUse;
import l2rt.gameserver.network.serverpackets.MyTargetSelected;
import l2rt.gameserver.tables.SkillTable;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.util.Location;
import l2rt.util.GArray;
import l2rt.util.Rnd;

public class Quest8 extends Fighter
{
	private boolean targeta = false;
	public Quest8(L2Character actor)
	{
		super(actor);
		this.AI_TASK_DELAY = 1000;
		this.AI_TASK_ACTIVE_DELAY = 1000;
	}

	@Override
	public boolean isGlobalAI()
	{
		return true;
	}
	
	@Override
	protected boolean thinkActive()
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
			return true;
		actor.setRunning();
		if (getIntention() != AI_INTENTION_ATTACK) {
			for(L2Player target : L2World.getAroundPlayers(actor, 1500, 1000))
			{
				if (target != null) {
					target.addDamageHate(actor, 0, 100);
					setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
					targeta = true;
				}
			}
		}
		return true;
	}
}