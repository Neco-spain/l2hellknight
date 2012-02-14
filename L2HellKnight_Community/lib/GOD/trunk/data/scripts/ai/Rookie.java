package ai;

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

public class Rookie extends Fighter
{
	private boolean targeta = false;
	public Rookie(L2Character actor)
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
	protected void onEvtSpawn()
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
			return;
		for(L2NpcInstance target : L2World.getAroundNpc(actor, 100, 200))
		{
			if (target != null && target.getNpcId() == 33023) {
				target.addDamageHate(actor, 0, 100);
				setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
				targeta = true;
			}
		}
		super.onEvtSpawn();
	}
}