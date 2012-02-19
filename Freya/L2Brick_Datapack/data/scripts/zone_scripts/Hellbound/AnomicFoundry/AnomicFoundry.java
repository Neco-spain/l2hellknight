package zone_scripts.Hellbound.AnomicFoundry;

//import l2.brick.gameserver.Announcements;
import l2.brick.Config;
import l2.brick.gameserver.ai.CtrlIntention;
import l2.brick.gameserver.datatables.SpawnTable;
import l2.brick.gameserver.instancemanager.HellboundManager;
import l2.brick.gameserver.instancemanager.WalkingManager;
import l2.brick.gameserver.model.L2CharPosition;
import l2.brick.gameserver.model.L2Skill;
import l2.brick.gameserver.model.L2Spawn;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.L2Attackable;
import l2.brick.gameserver.model.actor.instance.L2MonsterInstance;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.network.clientpackets.Say2;
import l2.brick.gameserver.network.serverpackets.NpcSay;
import l2.brick.util.Rnd;

import java.util.Map;
import javolution.util.FastMap;

public class AnomicFoundry extends Quest
{
	private static int LABORER = 22396;
	private static int FOREMAN =  22397;
	private static int LESSER_EVIL = 22398;
	private static int GREATER_EVIL = 22399;
	
	//npcId, x, y, z, heading, max count
	private static int[][] SPAWNS =
	{
		{LESSER_EVIL, 27883, 248613, -3209, -13248, 5},
		{LESSER_EVIL, 26142, 246442, -3216, 7064, 5},
		{LESSER_EVIL, 27335, 246217, -3668, -7992, 5},
		{LESSER_EVIL, 28486, 245913, -3698, 0, 10},
		{GREATER_EVIL, 28684, 244118, -3700, -22560, 10},
	};
	
	private int respawnTime = 60000;
	private int respawnMin = 20000;
	private int respawnMax = 300000;

	private int[] _spawned = {0, 0, 0, 0, 0};
	private Map<Integer, Integer> _atkIndex = new FastMap<Integer, Integer>(); 

	public AnomicFoundry(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addAggroRangeEnterId(LABORER);
		addAttackId(LABORER);
		addKillId(LABORER);
		addKillId(LESSER_EVIL);
		addKillId(GREATER_EVIL);
		addSpawnId(LABORER);
		addSpawnId(LESSER_EVIL);
		addSpawnId(GREATER_EVIL);
		
		startQuestTimer("make_spawn_1", respawnTime, null, null);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("make_spawn_1"))
		{
			if (HellboundManager.getInstance().getLevel() >= 10)
			{
				int idx = Rnd.get(3);
				if (_spawned[idx] < SPAWNS[idx][5])
				{
					addSpawn(SPAWNS[idx][0], SPAWNS[idx][1], SPAWNS[idx][2], SPAWNS[idx][3], SPAWNS[idx][4], false, 0, false);
					respawnTime += 10000;
				}
				startQuestTimer("make_spawn_1", respawnTime, null, null);
			}
		}
		
		else if (event.equalsIgnoreCase("make_spawn_2"))
		{
			if (_spawned[4] < SPAWNS[4][5])
				addSpawn(SPAWNS[4][0], SPAWNS[4][1], SPAWNS[4][2], SPAWNS[4][3], SPAWNS[4][4], false, 0, false);
		}
		
		else if (event.equalsIgnoreCase("return_laborer"))
		{
			if (npc != null && !npc.isDead())
				((L2Attackable) npc).returnHome();
		}
		
		else if (event.equalsIgnoreCase("reset_respawn_time"))
		{
			respawnTime = 60000;
		}
		
		return null;
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		//Announcements.getInstance().announceToAll("Aggro Range triggered");
		if (Rnd.get(10000) < 2000)
			requestHelp(npc, player, 500);

		return super.onAggroRangeEnter(npc, player, isPet);
	}
	
	@Override
	public String onAttack (L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		int atkIndex = _atkIndex.containsKey(npc.getObjectId()) ? _atkIndex.get(npc.getObjectId()) : 0;
		if (atkIndex == 0)
		{
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getNpcId(), 1800109)); //Enemy invasion! Hurry up!
			cancelQuestTimer("return_laborer", npc, null);
			startQuestTimer("return_laborer", 60000, npc, null);
			
			if (respawnTime > respawnMin)
				respawnTime -= 5000;
			else if (respawnTime <= respawnMin && getQuestTimer("reset_respawn_time", null, null) == null)
				startQuestTimer("reset_respawn_time", 600000, null, null);
		}
		
		if (Rnd.get(10000) < 2000)
		{
			atkIndex++;
			_atkIndex.put(npc.getObjectId(), atkIndex);
			requestHelp(npc, attacker, 1000 * atkIndex);
			
			if (Rnd.get(10) < 1)
				npc.getAI().setIntention( CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition((npc.getX() + Rnd.get(-800, 800)), (npc.getY()+ Rnd.get(-800, 800)), npc.getZ(), npc.getHeading())); 
		}

		return super.onAttack(npc, attacker, damage, isPet, skill);
	}

	@Override
	public String onKill (L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (getSpawnGroup(npc) >= 0)
		{
			_spawned[getSpawnGroup(npc)]--;
			SpawnTable.getInstance().deleteSpawn(npc.getSpawn(), false);
		}
		
		else if (npc.getNpcId() == LABORER)
		{
			if (Rnd.get(10000) < 8000)
			{
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getNpcId(), 1800110)); //Process... shouldn't... be delayed... because of me...
			if (respawnTime < respawnMax)
				respawnTime += 10000;
			else if (respawnTime >= respawnMax && getQuestTimer("reset_respawn_time", null, null) == null)
				startQuestTimer("reset_respawn_time", 600000, null, null);
			}
			_atkIndex.remove(npc.getObjectId());
		}

		return super.onKill(npc, killer, isPet);
	}

	@Override
	public final String onSpawn(L2Npc npc)
	{
		if (!npc.isTeleporting())
		{
			SpawnTable.getInstance().addNewSpawn(npc.getSpawn(), false);
			if (getSpawnGroup(npc) >= 0)
				_spawned[getSpawnGroup(npc)]++;
			
			//Announcements.getInstance().announceToAll("Spawned Evil in group " + Integer.toString(getSpawnGroup(npc)) + ". Total spawned = " + Integer.toString(_spawned[getSpawnGroup(npc)]));
			
			if (npc.getNpcId() == LABORER)
				npc.setIsNoRndWalk(true);
		}
		
		if (getSpawnGroup(npc) >= 0 && getSpawnGroup(npc) <= 2)
		{
			if (!npc.isTeleporting())
				WalkingManager.getInstance().startMoving(npc, getRoute(npc));

			else
			{
				_spawned[getSpawnGroup(npc)]--;
				SpawnTable.getInstance().deleteSpawn(npc.getSpawn(), false);
				npc.scheduleDespawn(100);
				if (_spawned[3] < SPAWNS[3][5])
					addSpawn(SPAWNS[3][0], SPAWNS[3][1], SPAWNS[3][2], SPAWNS[3][3], SPAWNS[3][4], false, 0, false);
			}
		}
		
		else if (getSpawnGroup(npc) == 3)
		{
			if (!npc.isTeleporting())
				WalkingManager.getInstance().startMoving(npc, getRoute(npc));
			
			else
			{
				//Announcements.getInstance().announceToAll("Greater spawn is added");
				startQuestTimer("make_spawn_2", respawnTime * 2, null, null);
				_spawned[3]--;
				SpawnTable.getInstance().deleteSpawn(npc.getSpawn(), false);
				npc.scheduleDespawn(100);
			}		
		}
		
		else if (getSpawnGroup(npc) == 4 && !npc.isTeleporting())
			WalkingManager.getInstance().startMoving(npc, getRoute(npc));
		
		return super.onSpawn(npc);
	}
	
	private static int getSpawnGroup(L2Npc npc)
	{
		int ret = -1;
		int[] coords = new int[2];
		coords[0] = npc.getSpawn().getLocx();
		coords[1] = npc.getSpawn().getLocy();
		int npcId = npc.getNpcId();

		for (int i = 0; i < 5; i++)
		{
			if (SPAWNS[i][0] == npcId && SPAWNS[i][1] == coords[0] && SPAWNS[i][2] == coords[1])
			{
				ret = i;
				break;
			}     
		}
		
		return ret;	
	}
	
	private static int getRoute(L2Npc npc)
	{
		int ret = getSpawnGroup(npc);

		return ret >= 0 ? ret + 6 : -1;
	}
	
	private static void requestHelp(L2Npc requester, L2PcInstance agressor, int range)
	{
		for (L2Spawn npcSpawn : SpawnTable.getInstance().getSpawnTable())
		{
			if (npcSpawn.getNpcid() == FOREMAN || npcSpawn.getNpcid() == LESSER_EVIL || npcSpawn.getNpcid() == GREATER_EVIL)
			{
				L2MonsterInstance monster = (L2MonsterInstance) npcSpawn.getLastSpawn();
				
				if (monster != null && !monster.isDead() && monster.isInsideRadius(requester, range, true, false) && agressor != null && !agressor.isDead())
				{
					monster.addDamageHate(agressor, 0, 1000);
				}
			}
		}
	}

	public static void main(String[] args)
	{
		new AnomicFoundry(-1, AnomicFoundry.class.getSimpleName(), "zone_scripts/Hellbound");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Hellbound: Anomic Foundry");
	}
}