package ai;

import l2rt.common.ThreadPoolManager;
import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.ai.CtrlEvent;
import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2ObjectTasks.NotifyFactionTask;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.util.Rnd;

/**
 * AI для ищущих помощи при HP < 50%
 *
 * @author Diamond
 */
public class WatchmanMonster extends Fighter
{
	private long _attacker = 0, _lastSearch = 0;
	private boolean isSearching = false;
	static final String[] flood = { "I'll be back", "You are stronger than expected" };
	static final String[] flood2 = { "Help me!", "Alarm! We are under attack!" };

	public WatchmanMonster(L2Character actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(final L2Character attacker, int damage)
	{
		final L2NpcInstance actor = getActor();
		if(actor == null)
			return;
		if(attacker != null && !actor.getFactionId().isEmpty() && actor.getCurrentHpPercents() < 50 && _lastSearch < System.currentTimeMillis() - 15000)
		{
			_lastSearch = System.currentTimeMillis();
			_attacker = attacker.getStoredId();

			if(findHelp())
				return;
			Functions.npcSay(actor, "Anyone, help me!");
		}
		super.onEvtAttacked(attacker, damage);
	}

	private boolean findHelp()
	{
		isSearching = false;
		final L2NpcInstance actor = getActor();
		L2Character attacker = L2ObjectsStorage.getAsCharacter(_attacker);
		if(actor == null || attacker == null)
			return false;

		for(final L2NpcInstance npc : actor.getAroundNpc(1000, 150))
			if(!actor.isDead() && npc.getFactionId().equals(actor.getFactionId()) && !npc.isInCombat() && actor.buildPathTo(npc.getX(), npc.getY(), npc.getZ(), 20, true, false))
			{
				clearTasks();
				isSearching = true;
				addTaskMove(npc.getLoc(), true);
				Functions.npcSay(actor, flood[Rnd.get(flood.length)]);
				return true;
			}
		return false;
	}

	@Override
	protected void onEvtDead(L2Character killer)
	{
		_lastSearch = 0;
		_attacker = 0;
		isSearching = false;
		super.onEvtDead(killer);
	}

	@Override
	protected void onEvtArrived()
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
			return;
		if(isSearching)
		{
			L2Character attacker = L2ObjectsStorage.getAsCharacter(_attacker);
			if(attacker != null)
			{
				Functions.npcSay(actor, flood2[Rnd.get(flood2.length)]);
				actor.callFriends(attacker, 100);
				ThreadPoolManager.getInstance().executeAi(new NotifyFactionTask(actor, attacker, 100), false);
			}
			isSearching = false;
			notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 100);
		}
		else
			super.onEvtArrived();
	}

	@Override
	protected void onEvtAggression(L2Character target, int aggro)
	{
		if(!isSearching)
			super.onEvtAggression(target, aggro);
	}
}