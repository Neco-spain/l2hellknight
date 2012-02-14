package ai;

import static l2rt.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;

import java.util.concurrent.ScheduledFuture;

import l2rt.common.ThreadPoolManager;
import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.ai.DefaultAI;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.tables.SkillTable;
import l2rt.util.Location;
import l2rt.util.Rnd;

public class GuardOfTheDawn extends DefaultAI
{
	public Location[] points = null;
	public Location teleport = null;
	private final String[] txt = { "Intruder! Protect the Priests of Dawn!",
			"How dare you intrude with that transformation! Get lost!" };

	private int current_point = -1;

	ScheduledFuture<?> teleportTask;

	public GuardOfTheDawn(L2Character actor)
	{
		super(actor);
		AI_TASK_DELAY = 1000;
		AI_TASK_ACTIVE_DELAY = 1000;
	}

	@Override
	public boolean isGlobalAI()
	{
		return true;
	}

	@Override
	public void checkAggression(L2Character target)
	{
		if(teleportTask != null || target == null || !target.isPlayer())
			return;
		L2NpcInstance actor = getActor();
		if(actor == null || actor.isDead())
			return;
		L2Player player = (L2Player) target;
		if(!((player.isInvisible() || player.isSilentMoving()) && actor.getNpcId() == 27351) && actor.isInRange(player, actor.getNpcId() == 27351 ? 400 : 150) && GeoEngine.canSeeTarget(actor, player, false))
		{
			Functions.npcSay(actor, txt[Rnd.get(txt.length)]);
			teleportTask = ThreadPoolManager.getInstance().scheduleAi(new TeleportTask(player), 3000, true);
			actor.doCast(SkillTable.getInstance().getInfo(5978, 1), player, false);
		}
	}

	@Override
	protected boolean thinkActive()
	{
		L2NpcInstance actor = getActor();
		if(actor == null || actor.isDead())
			return true;

		if(teleportTask != null)
			return true;

		if(_def_think)
		{
			doTask();
			return true;
		}

		if(points == null)
			return true;

		current_point++;

		if(current_point >= points.length)
			current_point = 0;

		addTaskMove(points[current_point], false);
		doTask();
		return true;
	}

	private class TeleportTask implements Runnable
	{
		private L2Player _player;

		public TeleportTask(L2Player player)
		{
			_player = player;
		}

		public void run()
		{
			if(_player != null)
				_player.teleToLocation(teleport);
			_player = null;
			teleportTask = null;
			setIntention(AI_INTENTION_ACTIVE);
			L2NpcInstance actor = getActor();
			if(actor != null)
				actor.stopAttackStanceTask();
		}
	}

	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{}

	@Override
	protected void onEvtAggression(L2Character target, int aggro)
	{}
}