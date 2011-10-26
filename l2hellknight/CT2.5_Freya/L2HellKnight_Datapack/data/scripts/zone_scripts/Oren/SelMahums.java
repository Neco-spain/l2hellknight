package zone_scripts.Oren;

import l2.hellknight.Config;
import l2.hellknight.gameserver.ThreadPoolManager;
import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.datatables.SpawnTable;
import l2.hellknight.gameserver.instancemanager.ZoneManager;
import l2.hellknight.gameserver.model.L2CharPosition;
import l2.hellknight.gameserver.model.L2Spawn;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2MonsterInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.zone.L2ZoneType;
import l2.hellknight.gameserver.network.clientpackets.Say2;
import l2.hellknight.gameserver.network.serverpackets.NpcSay;
import l2.hellknight.gameserver.network.serverpackets.SocialAction;
import l2.hellknight.gameserver.util.Util;
import l2.hellknight.util.Rnd;

import gnu.trove.TIntHashSet;

import l2.hellknight.bflmpsvz.a.L2AttackableAIScript;

import java.util.List;
import javolution.util.FastList;

public class SelMahums extends L2AttackableAIScript
{
	private static final int[] MAHUM_CHIEFS = { 22775, 22776, 22778 };
	private static final int[] MAHUM_SOLDIERS = { 22780, 22782, 22783, 22784, 22785 };
	private static final int[] CHIEF_SOCIAL_ACTIONS = { 1, 4, 5, 7 };
	private static final int[] SOLDIER_SOCIAL_ACTIONS = { 1, 5, 6, 7 };
	private static final String[] CHIEF_FSTRINGS = { "Who is mucking with my recruits!?!", "You are entering a world of hurt!" };
	private static final String[] SOLDIER_FSTRINGS = { "They done killed da Sarge... Run!!", "Don't Panic... Okay, Panic!" };
	private static List<L2Spawn> _spawns = new FastList<L2Spawn>();
	private static TIntHashSet _scheduledReturnTasks = new TIntHashSet();

	public SelMahums (int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		for (int i : MAHUM_CHIEFS)
			{
				addAttackId(i);
				addKillId(i);
				addSpawnId(i);
			}

		for (int i : MAHUM_SOLDIERS)
			addSpawnId(i);
	
    for (L2Spawn npcSpawn : SpawnTable.getInstance().getSpawnTable())
    {
      if (Util.contains(MAHUM_CHIEFS, npcSpawn.getNpcid()) || Util.contains(MAHUM_SOLDIERS, npcSpawn.getNpcid()))
          onSpawn(npcSpawn.getLastSpawn());
    }
	}

	@Override
	public String onAdvEvent (String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("do_social_action"))
		{
			if (npc != null && !npc.isDead()) 
			{
				if (!npc.isBusy() && npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE && 
						npc.getX() == npc.getSpawn().getLocx() && npc.getY() == npc.getSpawn().getLocy()) 
				{
					int idx = Rnd.get(6);
					if (idx <= CHIEF_SOCIAL_ACTIONS.length - 1)
					{
						npc.broadcastPacket(new SocialAction(npc, CHIEF_SOCIAL_ACTIONS[idx]));

						L2ZoneType zone = getZone(npc);
					
						if (zone != null )
						for (L2Character ch : zone.getCharactersInsideArray())
						{
							if (ch != null && !ch.isDead() && ch instanceof L2MonsterInstance && !((L2MonsterInstance) ch).isBusy() && 
									Util.contains(MAHUM_SOLDIERS, ((L2MonsterInstance) ch).getNpcId()) && ch.getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE && 
									ch.getX() == ((L2MonsterInstance) ch).getSpawn().getLocx() && ch.getY() == ((L2MonsterInstance) ch).getSpawn().getLocy())
								ch.broadcastPacket(new SocialAction(ch, SOLDIER_SOCIAL_ACTIONS[idx]));
						}
					}
				}	

				startQuestTimer("do_social_action", 15000, npc, null);
			}
		}
		
		else if (event.equalsIgnoreCase("reset_busy_state"))
		{
			if (npc != null)
			{
				npc.setBusy(false);
				npc.disableCoreAI(false);
			}
   	}
   	
		return null;
	}

	@Override
	public String onAttack (L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (!npc.isDead() && !npc.isBusy())
		{
			if (Rnd.get(10) < 1)
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getNpcId(), CHIEF_FSTRINGS[Rnd.get(2)]));

			npc.setBusy(true);
			startQuestTimer("reset_busy_state", 60000, npc, null);
		}

		return super.onAttack(npc,attacker,damage,isPet);
	}

	@Override
	public String onKill (L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		L2ZoneType leaderZone = getZone(npc);
		
		if (leaderZone != null)	
		{
			for (L2Spawn sp : _spawns)
			{
				L2MonsterInstance soldier = (L2MonsterInstance) sp.getLastSpawn();
				if (soldier != null && !soldier.isDead())
				{
					L2ZoneType soldierZone = getZone(soldier);
					if (soldierZone != null && leaderZone.getId() == soldierZone.getId())
					{
						if (Rnd.get(4) < 1)
							soldier.broadcastPacket(new NpcSay(soldier.getObjectId(), Say2.ALL, soldier.getNpcId(), SOLDIER_FSTRINGS[Rnd.get(2)]));
				
						soldier.setBusy(true);
						soldier.setIsRunning(true);
						soldier.clearAggroList();
						soldier.disableCoreAI(true);
						soldier.getAI().setIntention( CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition((soldier.getX() + Rnd.get(-800, 800)), (soldier.getY()+ Rnd.get(-800, 800)), soldier.getZ(), soldier.getHeading()));
						startQuestTimer("reset_busy_state", 5000, soldier, null);
					}
				}
			}
			if (!_scheduledReturnTasks.contains(leaderZone.getId()))
			{
				_scheduledReturnTasks.add(leaderZone.getId());
				ThreadPoolManager.getInstance().scheduleGeneral(new ReturnTask(leaderZone.getId()), 120000);
			}
		}

		return super.onKill(npc,killer,isPet);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if (!npc.isTeleporting())
		{
			if (Util.contains(MAHUM_CHIEFS, npc.getNpcId()))
				startQuestTimer("do_social_action", 15000, npc, null);
			
			npc.disableCoreAI(false);
			npc.setBusy(false);
			npc.setIsNoRndWalk(true);
			npc.setRandomAnimationEnabled(false);
			_spawns.add(npc.getSpawn());
		}

		return super.onSpawn(npc);
	}
	
	private L2ZoneType getZone(L2Npc npc)
	{
		L2ZoneType zone = null;
					
		try
		{
			L2Spawn spawn = npc.getSpawn();
			zone = ZoneManager.getInstance().getZones(spawn.getLocx(), spawn.getLocy(), spawn.getLocz()).get(0);
		}

		catch(NullPointerException e)
		{
		}

		catch(IndexOutOfBoundsException e)
		{
		}
		
		return zone;
	}
	 	
	private class ReturnTask implements Runnable
	{
		private final int _zoneId;
		private boolean _runAgain;

		public ReturnTask(int zoneId)
		{
			_zoneId = zoneId;
			_runAgain = false;
		}

		@Override
		public void run()
		{
		 	for (L2Spawn sp: _spawns)
		 	{
				L2MonsterInstance monster = (L2MonsterInstance) sp.getLastSpawn();
				
				if (monster != null && !monster.isDead())
				{
					L2ZoneType zone = getZone(monster);
					if (zone != null && zone.getId() == _zoneId)
					{
						if (monster.getX() != sp.getLocx() && monster.getY() != sp.getLocy())
						{
							if (monster.getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE || monster.getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE) 
							{
								monster.setHeading(sp.getHeading());
								monster.teleToLocation(sp.getLocx(), sp.getLocy(), sp.getLocz());
							}
							else
								_runAgain = true;
						}
					}
				}   
			}
			if (_runAgain)
				ThreadPoolManager.getInstance().scheduleGeneral(new ReturnTask(_zoneId), 120000);
			else
				_scheduledReturnTasks.remove(_zoneId);
		}
	}
	
	
	public static void main(String[] args)
	{
		new SelMahums(-1,"sel_mahums","ai");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Oren: Sel Mahums");
	}
}
