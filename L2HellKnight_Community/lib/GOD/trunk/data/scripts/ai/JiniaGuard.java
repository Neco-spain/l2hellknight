package ai;

import l2rt.common.ThreadPoolManager;
import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.ai.CtrlIntention;
import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.Reflection;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.gameserver.network.serverpackets.ExStartScenePlayer;
import l2rt.util.Location;
import l2rt.util.Rnd;

/**
 * AI для гвардов в квесте на фрею
 * @author DarkShadow74 ^_^
 */
public class JiniaGuard extends Fighter
{
    boolean attack = false;
    boolean say = false;
	private L2NpcInstance target;
	private long is_leader = 0;
	private long _lastTarget;
	private static final int targets[] = { 22767, 18847 };
	
	public JiniaGuard(L2Character actor)
	{
		super(actor);
		AI_TASK_DELAY = 1000;
		AI_TASK_ACTIVE_DELAY = 1000;
	}

	@Override
	protected void onEvtSpawn()
	{
	    if(is_leader == 0)
	    	ThreadPoolManager.getInstance().scheduleGeneral(new Leader(), 1000);
	}

	@Override
	protected void onEvtDead(L2Character killer)
	{   
	
		L2NpcInstance actor = getActor();
	    final Reflection r = actor.getReflection();
		L2NpcInstance leaderNPC = L2ObjectsStorage.getAsNpc(is_leader);
		final String refName = r.getName();
		
		r.clearReflection(2, true);
		
		if(leaderNPC == null)
		{
			// Показываем финальный ролик при фейле серез секунду после очистки инстанса
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable(){
				@Override
				public void run()
				{
					for(L2Player pl : r.getPlayers())
						if(pl != null)
						{
							pl.showQuestMovie(ExStartScenePlayer.SCENE_SC_BOSS_FREYA_OPENING);
							QuestState qs = pl.getQuestState("_10285_MeetingSirra");
							if(qs != null && qs.isStarted() && qs.getCond() == 9)
						    	qs.setCond(10);
							if(r.getReturnLoc() != null)
	                     	    pl.teleToLocation(r.getReturnLoc(), 0);
							pl.setVar(refName, String.valueOf(System.currentTimeMillis() - 1380000));
						}
				}
			}, 1000);
		}
			
		super.onEvtDead(killer);
	}

	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{
		if(attacker.isPlayer())
	    	return;
			
		super.onEvtAttacked(attacker, damage);
	}

	@Override
	protected void onEvtAggression(L2Character attacker, int aggro)
	{
		if(attacker.isPlayer())
	    	return;
			
		super.onEvtAggression(attacker, aggro);
	}

	@Override
	public void checkAggression(L2Character target)
	{
		L2NpcInstance actor = getActor();
		L2Player player = target.getPlayer();
		if(actor == null || target == null)
			return;
		if(attack)
	    	return;
		if(!actor.isInRange(target, 800))
			return;
		if(is_leader != 0 && !say)
		{
			say = true;
			L2NpcInstance leaderNPC = L2ObjectsStorage.getAsNpc(is_leader);
			if(leaderNPC != null)
				Functions.npcSay(leaderNPC, player.getName() + ". May the protection of the gods be upon you!");
		}
		attack = true;
	}

	@Override
	protected boolean thinkActive()
	{
		L2NpcInstance actor = getActor();
		if(actor == null || actor.isDead())
			return true;
			
		if(attack)
		{
	    	if(System.currentTimeMillis() - _lastTarget > 1000)
			{
			    _lastTarget = System.currentTimeMillis();
		    	if(target == null)
			    	for(L2NpcInstance npc : L2ObjectsStorage.getAllByNpcId(targets[Rnd.get(targets.length)], true))
			    		if(npc.getReflection().getId() == actor.getReflection().getId())
						{
					    	npc.addDamageHate(actor, 0, 100);
							target = npc;
						}
				if(target != null)
				{
		    		setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
					target = null;
				}
			}
		}
		actor.setRunning(); // всегда бегают
		return false;
    }
	
	@Override
	protected boolean randomWalk()
	{
		return false;
	}
	
	private class Leader implements Runnable
	{
		public void run()
		{
			L2NpcInstance actor = getActor();
			Location sloc = actor.getSpawnedLoc();
			if(actor != null && sloc.x == 114861)
				is_leader = actor.getStoredId();
		}
	}
}
