package ai;

import l2rt.gameserver.ai.CtrlIntention;
import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.QuestEventType;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.util.GArray;

/**
 * Квестовый NPC, атакующий мобов. Игнорирует игроков.
 * @author Diamond
 */
public class AttackMobNotPlayerFighter extends Fighter
{
	public AttackMobNotPlayerFighter(L2Character actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{
		L2NpcInstance actor = getActor();
		if(attacker == null || actor == null)
			return;

		L2Player player = attacker.getPlayer();
		if(player != null)
		{
			GArray<QuestState> quests = player.getQuestsForEvent(actor, QuestEventType.MOBGOTATTACKED);
			if(quests != null)
				for(QuestState qs : quests)
					qs.getQuest().notifyAttack(actor, qs);
		}

		onEvtAggression(attacker, damage);
	}

	@Override
	protected void onEvtAggression(L2Character attacker, int aggro)
	{
		L2NpcInstance actor = getActor();
		if(attacker == null || actor == null)
			return;

		actor.setAttackTimeout(getMaxAttackTimeout() + System.currentTimeMillis());
		setGlobalAggro(0);

		if(!actor.isRunning())
			startRunningTask(1000);

		if(getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
	}
}