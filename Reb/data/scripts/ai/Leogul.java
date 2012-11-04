package ai;

import l2r.gameserver.ai.CtrlEvent;
import l2r.gameserver.ai.Fighter;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.scripts.Functions;

public class Leogul extends Fighter
{
	public Leogul(NpcInstance actor)
	{
		super(actor);
		AI_TASK_ATTACK_DELAY = 1000;
		AI_TASK_ACTIVE_DELAY = 1000;
	}

	@Override
	public boolean checkAggression(Creature killer)
	{
		if(super.checkAggression(killer))
		{
			Functions.npcSayCustomMessage(getActor(), "scripts.ai.Leogul");
		
			for(NpcInstance npc : getActor().getAroundNpc(800, 128))
				if(npc.isMonster() && npc.getNpcId() >= 22660 && npc.getNpcId() <= 22677)
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, 5000);
			return true;
		}
		return false;
	}
}