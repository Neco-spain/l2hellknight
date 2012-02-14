package ai;

import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.ai.CtrlEvent;
import l2rt.gameserver.ai.L2CharacterAI;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.util.GArray;

public class FieldMachine extends L2CharacterAI
{
	private long _lastAction;

	public FieldMachine(L2Character actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{
		L2NpcInstance actor = (L2NpcInstance) getActor();
		if(actor == null || attacker == null || attacker.getPlayer() == null)
			return;

		// Ругаемся не чаще, чем раз в 15 секунд
		if(System.currentTimeMillis() - _lastAction > 15000)
		{
			_lastAction = System.currentTimeMillis();
			Functions.npcSayCustomMessage(actor, "scripts.ai.FieldMachine." + actor.getNpcId());
			GArray<L2NpcInstance> around = actor.getAroundNpc(1500, 300);
			if(around != null && !around.isEmpty())
				for(L2NpcInstance npc : around)
					if(npc.isMonster() && npc.getNpcId() >= 22656 && npc.getNpcId() <= 22659)
						npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 5000);
		}
	}
}