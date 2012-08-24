/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ai.group_template;

import ai.group_template.L2AttackableAIScript;

import l2.hellknight.gameserver.GameTimeController;
import l2.hellknight.gameserver.ThreadPoolManager;
import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.datatables.SkillTable;
import l2.hellknight.gameserver.datatables.SpawnTable;
import l2.hellknight.gameserver.instancemanager.WalkingManager;
import l2.hellknight.gameserver.instancemanager.ZoneManager;
import l2.hellknight.gameserver.model.L2CharPosition;
import l2.hellknight.gameserver.model.L2Object;
import l2.hellknight.gameserver.model.skills.L2Skill;
import l2.hellknight.gameserver.model.L2Spawn;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2MonsterInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.zone.L2ZoneType;
import l2.hellknight.gameserver.network.NpcStringId;
import l2.hellknight.gameserver.network.clientpackets.Say2;
import l2.hellknight.gameserver.network.serverpackets.NpcSay;
import l2.hellknight.gameserver.network.serverpackets.SocialAction;
import l2.hellknight.gameserver.util.Util;
import l2.hellknight.util.Rnd;

import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.map.hash.TIntLongHashMap;
import java.util.List;
import javolution.util.FastList;

/**
 * Sel Mahum Training Ground AI.
 * @author GKR
 */

/** Some assumes
 * Fire Mahums (aka Private Warriors)
 *  i_ai0 - 0 means walking around, 1 - means moving / moved to fire. For this approach i_ai0 == 0 means !isNoRndWalk(), i_ai1 == 1 means isNoRndWalk().
 *  i_ai3 - 0 means walking around, 1 - means moving / moved to eat. For this approach i_ai3 == 0 means !isBusy(), i_ai1 == 1 isBusy().
 */

public class SelMahums extends L2AttackableAIScript
{

	//Sel Mahum Drill Sergeant, Sel Mahum Training Officer, Sel Mahum Drill Sergeant respectively
	private static final int[] MAHUM_CHIEFS = { 22775, 22776, 22778 };

	//Sel Mahum Recruit, Sel Mahum Recruit, Sel Mahum Soldier, Sel Mahum Recruit, Sel Mahum Soldier respectively 
	private static final int[] MAHUM_SOLDIERS = { 22780, 22782, 22783, 22784, 22785 };
	
	// Sel Mahum Squad Leaders
	private static final int[] FIRE_MAHUMS = { 22786, 22787, 22788 };
	
	private static final int CHEF = 18908;
	private static final int FIRE = 18927;
	private static final int STOVE = 18933;
	
	private static final int[] CHIEF_SOCIAL_ACTIONS = { 1, 4, 5, 7 };
	private static final int[] SOLDIER_SOCIAL_ACTIONS = { 1, 5, 6, 7 };
	
	//private static final int OHS_Weapon = 15280;
	//private static final int THS_Weapon = 15281;

	/**
	 * 1801112 - Who is mucking with my recruits!?!
	 * 1801113 - You are entering a world of hurt!
	 */	 	 	
	//I get crash of client, if use "int" constructor, so I use "String" constructor here
	//private static final int[] CHIEF_FSTRINGS = { 1801112, 1801113 };
	//private static final String[] CHIEF_FSTRINGS = { "Who is mucking with my recruits!?!", "You are entering a world of hurt!" };
	private static final NpcStringId[] CHIEF_FSTRINGS = 
	{
		NpcStringId.HOW_DARE_YOU_ATTACK_MY_RECRUITS,
		NpcStringId.WHO_IS_DISRUPTING_THE_ORDER
	};

	/**
	 * 1801114 - They done killed da Sarge... Run!!
	 * 1801115 - Don't Panic... Okay, Panic!
	 */	 	 	
	//I get crash of client, if use "int" constructor, so I use "String" constructor here
	//private static final int[] SOLDIER_FSTRINGS = { 1801114, 1801115 };
	//private static final String[] SOLDIER_FSTRINGS = { "They done killed da Sarge... Run!!", "Don't Panic... Okay, Panic!" };
	private static final NpcStringId[] SOLDIER_FSTRINGS = 
	{
		NpcStringId.THE_DRILLMASTER_IS_DEAD,
		NpcStringId.LINE_UP_THE_RANKS
	};

	private static final NpcStringId[] CHEF_FSTRINGS = 
	{
		NpcStringId.I_BROUGHT_THE_FOOD,
		NpcStringId.COME_AND_EAT
	};

	private static List<L2Spawn> _spawns = new FastList<L2Spawn>(); //all Mahum's spawns are stored here
	private static TIntHashSet _scheduledReturnTasks = new TIntHashSet(); //Used to track scheduled Return Tasks

	// Holder for Chef's info
	private TIntLongHashMap _firstAttackTime = new TIntLongHashMap();

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

		for (int i : FIRE_MAHUMS)
		{
			addMoveFinishedId(i);
			addSpawnId(i);
		}
		
		addSpawnId(CHEF);
		addAttackId(CHEF);
		addKillId(CHEF);
		addNodeArrivedId(CHEF);
		addSpellFinishedId(CHEF);
		addSpawnId(FIRE);
		addSkillSeeId(STOVE);
		addSpawnId(STOVE);

		//Send event to monsters, that was spawned through SpawnTable at server start (it is impossible to track first spawn)
    for (L2Spawn npcSpawn : SpawnTable.getInstance().getSpawnTable())
    {
      if (Util.contains(MAHUM_CHIEFS, npcSpawn.getNpcid()) || Util.contains(MAHUM_SOLDIERS, npcSpawn.getNpcid()))
      {
        onSpawn(npcSpawn.getLastSpawn());
        _spawns.add(npcSpawn);
      }
      else if (npcSpawn.getNpcid() == CHEF || npcSpawn.getNpcid() == FIRE || npcSpawn.getNpcid() == STOVE)
      {
      	onSpawn(npcSpawn.getLastSpawn());
      }
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
						npc.broadcastPacket(new SocialAction(npc.getObjectId(), CHIEF_SOCIAL_ACTIONS[idx]));

						L2ZoneType zone = getZone(npc, "sel_mahum_training_grounds", false);
					
						if (zone != null )
						{
							for (L2Character ch : zone.getCharactersInside())
							{
								if (ch != null && !ch.isDead() && ch instanceof L2MonsterInstance && !((L2MonsterInstance) ch).isBusy() && 
										Util.contains(MAHUM_SOLDIERS, ((L2MonsterInstance) ch).getNpcId()) && ch.getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE && 
										ch.getX() == ((L2MonsterInstance) ch).getSpawn().getLocx() && ch.getY() == ((L2MonsterInstance) ch).getSpawn().getLocy())
									ch.broadcastPacket(new SocialAction(ch.getObjectId(), SOLDIER_SOCIAL_ACTIONS[idx]));
							}
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
   	
   	else if (event.equalsIgnoreCase("fire"))
   	{
			startQuestTimer("fire", 30000 + Rnd.get(5000), npc, null);
			npc.setDisplayEffect(2);
			boolean fireBurns;
			
			if (Rnd.get(GameTimeController.getInstance().isNowNight() ? 2 : 4) < 1)
			{
				fireBurns = true;
				npc.setBusyMessage("burn");
				npc.setDisplayEffect(1);
			}
			else
			{
				fireBurns = false;
				npc.setBusyMessage("");
				npc.setDisplayEffect(2);
			}
			
			L2ZoneType zone = getZone(npc, "sel_mahum_fire", false);
			
			if (zone != null)
			{
				for (L2Character ch : zone.getCharactersInside())
				{
					if (ch instanceof L2MonsterInstance)
					{ 
						L2MonsterInstance monster = (L2MonsterInstance) ch;
						if (Util.contains(FIRE_MAHUMS, monster.getNpcId()) && monster.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
						{
							if (fireBurns) // Fire burns (@SCE_CAMPFIRE_START)
							{
								if (!monster.isNoRndWalk())
								{
									monster.setIsNoRndWalk(true); // i_ai0 = 1
									monster.setIsRunning(false);
									moveToRandomPoint(zone, monster, npc, 100, 200);

								}
							}
							else // Fire goes out (@SCE_CAMPFIRE_END)
							{
								monster.setIsNoRndWalk(false);
								monster.setBusy(false);
								//monster.setRHandId(THS_Weapon);
								startQuestTimer("return_from_fire", 3000, monster, null);
							}
						}
					}
					else if (ch instanceof L2Npc && ((L2Npc) ch).getNpcId() == STOVE) // Fire goes out (@SCE_CAMPFIRE_DUMMY)
					{
						if (!fireBurns)
						{
							ch.deleteMe();
						}
					}
				}
			}
		}
		
		else if (event.equalsIgnoreCase("return_from_fire"))
		{
			if (npc != null && npc instanceof L2MonsterInstance)
				((L2MonsterInstance)npc).returnHome();
		}
		
		else if (event.equalsIgnoreCase("fire_arrived"))
		{
			// myself.i_quest0 = 1;
			npc.setIsRunning(false);
			npc.setTarget(npc);

			if (npc.isBusy()) // Eating - i_ai3 = 1
			{
				npc.doCast(SkillTable.getInstance().getInfo(6332, 1));
				npc.setDisplayEffect(1);
			}
			else	// Sleeping
			{
				npc.doCast(SkillTable.getInstance().getInfo(6331, 1));
				//SkillTable.getInstance().getInfo(6331, 1).getEffectsSelf(npc);
				npc.setDisplayEffect(2);
			}

			startQuestTimer("remove_effects", 300000, npc, null);
		}
		
		// @SCE_DINNER_EAT
		else if (event.equalsIgnoreCase("notify_dinner"))
		{
				L2ZoneType zone = getZone(npc, "sel_mahum_fire", false);
				
				if (zone != null)
				{
					for (L2Character ch : zone.getCharactersInside())
					{
						if (ch instanceof L2MonsterInstance)
						{
							L2MonsterInstance monster = (L2MonsterInstance) ch;
							if (Util.contains(FIRE_MAHUMS, monster.getNpcId()) && npc.isInsideRadius(monster, 600, true, true) && 
									!monster.isBusy() && monster.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
							{
								if (monster.isNoRndWalk()) // i_ai0 == 1
								{
									//monster.setRHandId(THS_Weapon);
								}
								monster.setIsNoRndWalk(true); // Moving to fire - i_ai0 = 1
								monster.setBusy(true); // Eating - i_ai3 = 1
								monster.setIsRunning(true);
								
								NpcSay ns;
								if (Rnd.get(3) < 1)
									ns = new NpcSay(monster.getObjectId(), Say2.ALL, monster.getNpcId(), NpcStringId.LOOKS_DELICIOUS);
								else
									ns = new NpcSay(monster.getObjectId(), Say2.ALL, monster.getNpcId(), NpcStringId.LETS_GO_EAT);

								monster.broadcastPacket(ns);
								moveToRandomPoint(zone, monster, npc, 100, 110);
							}
						}
					}
				}
		}
		
		else if (event.equalsIgnoreCase("remove_effects"))
		{
			if (npc != null)
			{
				// myself.i_quest0 = 0;
				npc.setIsRunning(true);
				npc.setDisplayEffect(3);
			}
		}

		else if (event.equalsIgnoreCase("reset_full_bottle_prize"))
		{
			if (npc != null && !npc.isDead())
				npc.setBusyMessage("");
		}
		
		else if (event.equalsIgnoreCase("chef_heal_player"))
		{
			if (player != null && !player.isDead() && !npc.getBusyMessage().isEmpty() &&
					(npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_ATTACK || npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_CAST))
			{
				npc.setTarget(player);
				npc.doCast(SkillTable.getInstance().getInfo(6330, 1));
			}
			else
			{
				cancelQuestTimer("chef_set_invul", npc, null);
				npc.setBusy(false); // i_ai2 = 0
				npc.setIsRunning(false);
			} 
		}
		
		else if (event.equalsIgnoreCase("chef_set_invul"))
		{
			if (npc != null && !npc.isDead())
				npc.setIsInvul(true);
		}

		else if (event.equalsIgnoreCase("chef_remove_invul"))
		{
			if (npc != null && !npc.isDead() && npc instanceof L2MonsterInstance)
			{
				npc.setIsInvul(false);
				npc.setBusyMessage(""); // i_ai5 = 0
				
				if (player != null && !player.isDead() && npc.getKnownList().knowsThePlayer(player))
				{
					((L2MonsterInstance) npc).addDamageHate(player, 0, 999);
					((L2MonsterInstance) npc).getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player, null);
				}
			}
		}
   	
		return null;
	}

	@Override
	public String onAttack (L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		if (Util.contains(MAHUM_CHIEFS, npc.getNpcId()))
		{
			if (!npc.isDead() && !npc.isBusy())
			{
				if (Rnd.get(10) < 1)
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getNpcId(), CHIEF_FSTRINGS[Rnd.get(2)]));

				npc.setBusy(true);
				startQuestTimer("reset_busy_state", 60000, npc, null);
			}
		}
		
		else if (npc.getNpcId() == CHEF)
		{
			if (!npc.isBusy()) // i_ai2 == 0
			{
				if (npc.getBusyMessage().isEmpty()) // i_ai5 == 0
				{
					if (getQuestTimer("chef_remove_invul", npc, null) != null)
						cancelQuestTimer("chef_remove_invul", npc, null);

					startQuestTimer("chef_remove_invul", 180000, npc, attacker);

					_firstAttackTime.putIfAbsent(npc.getObjectId(), System.currentTimeMillis()); //i_ai6
					npc.setBusyMessage("!"); // i_ai5 == 1
				}
				startQuestTimer("chef_heal_player", 1000, npc, attacker);
				startQuestTimer("chef_set_invul", 60000, npc, null);
				npc.setBusy(true); // i_ai2 = 1
			}
		}
		return super.onAttack(npc, attacker, damage, isPet, skill);
	}

	@Override
	// @SCE_SOUP_FAILURE
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if (npc.getNpcId() == STOVE && skill.getId() == 9075 && Util.contains(targets, npc))
		{
			npc.doCast(SkillTable.getInstance().getInfo(6688, 1));

			L2ZoneType zone = getZone(npc, "sel_mahum_fire", false);
			
			if (zone != null)
			{
				for (L2Character ch : zone.getCharactersInside())
				{
					if (ch instanceof L2MonsterInstance)
					{ 
						L2MonsterInstance monster = (L2MonsterInstance) ch;
						if (Util.contains(FIRE_MAHUMS, monster.getNpcId()))
						{
							monster.setBusyMessage(caster.getName()); // REMEMBER: Use it in 289 quest
							startQuestTimer("reset_full_bottle_prize", 180000, monster, null);
						}
					}
				}
			}
			
		}
		return null;
	}

	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		if (npc.getNpcId() == CHEF && skill != null && skill.getId() == 6330)
		{
			if (player != null && !player.isDead() && !npc.getBusyMessage().isEmpty() &&
					(npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_ATTACK || npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_CAST))
			{
				npc.setTarget(player);
				npc.doCast(SkillTable.getInstance().getInfo(6330, 1));
			}
			else
			{
				cancelQuestTimer("chef_set_invul", npc, null);
				npc.setBusy(false); // i_ai2 = 0
				npc.setBusyMessage(""); // i_ai5 = 0
			}
		}
		return super.onSpellFinished(npc, player, skill);
	}

	@Override
	public String onKill (L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (Util.contains(MAHUM_CHIEFS, npc.getNpcId()))
		{
			L2ZoneType leaderZone = getZone(npc, "sel_mahum_training_grounds", false);
		
			if (leaderZone != null)	
			{
				for (L2Spawn sp : _spawns)
				{
					if (sp == null)
						continue;

					L2MonsterInstance soldier = (L2MonsterInstance) sp.getLastSpawn();
					if (soldier != null && !soldier.isDead())
					{
						L2ZoneType soldierZone = getZone(soldier, "sel_mahum_training_grounds", false);
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
				//Soldiers should return into spawn location, if they have "NO_DESIRE" state. It looks like AI_INTENTION_ACTIVE in L2J terms,
				//but we have no possibility to track AI intention change, so timer is used here. Time can be ajusted, if needed.
				if (!_scheduledReturnTasks.contains(leaderZone.getId())) //Check for shceduled task presence for this zone
				{
					_scheduledReturnTasks.add(leaderZone.getId()); //register scheduled task for zone
					ThreadPoolManager.getInstance().scheduleGeneral(new ReturnTask(leaderZone.getId()), 120000); //schedule task
				}
			}
		}
		
		else if (npc.getNpcId() == CHEF && npc instanceof L2MonsterInstance)
		{
			if (_firstAttackTime.containsKey(npc.getObjectId()))
			{
				if (System.currentTimeMillis() - _firstAttackTime.get(npc.getObjectId()) <= 60000)
				{
					if (Rnd.get(10) < 2)
						((L2MonsterInstance) npc).dropItem(killer, 15492, 1);
				}
				
				_firstAttackTime.remove(npc.getObjectId()); 
			}
		}

		return super.onKill(npc,killer,isPet);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if (!npc.isTeleporting())
		{
			if (Util.contains(MAHUM_CHIEFS, npc.getNpcId()) || Util.contains(MAHUM_SOLDIERS, npc.getNpcId()))
			{
				if (Util.contains(MAHUM_CHIEFS, npc.getNpcId()))
					startQuestTimer("do_social_action", 15000, npc, null);
			
				npc.disableCoreAI(false);
				npc.setBusy(false);
				npc.setIsNoRndWalk(true);
				npc.setRandomAnimationEnabled(false);
			}
			
			else if (npc.getNpcId() == CHEF && getRoute(npc) > 0)
			{
				WalkingManager.getInstance().startMoving(npc, getRoute(npc));
				npc.setBusyMessage("");
				npc.setBusy(false);
				npc.setIsInvul(false);
			}

			else if (npc.getNpcId() == FIRE)
			{
				startQuestTimer("fire", 1000, npc, null);
			}

			else if (Util.contains(FIRE_MAHUMS, npc.getNpcId()))
			{
				npc.setBusy(false);
				npc.setBusyMessage("");
				npc.setDisplayEffect(3);
			}
		}

		return super.onSpawn(npc);
	}

	@Override
	public String onNodeArrived(L2Npc npc)
	{
		L2ZoneType zone = getZone(npc, "sel_mahum_fire", true);
			
		if (zone != null)
		{
			for (L2Character ch : zone.getCharactersInside())
			{
				if (ch instanceof L2Npc)
				{
					L2Npc monster = (L2Npc) ch;
					if (monster.getNpcId() == FIRE && npc.isInsideRadius(monster, 300, true, true))
					{
						monster.setDisplayEffect(1);
						addSpawn(STOVE, monster.getX(), monster.getY(), monster.getZ() + 100, 0, false, 0);
						startQuestTimer("notify_dinner", 2000, monster, null); // @SCE_DINNER_EAT
						npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getNpcId(), CHEF_FSTRINGS[Rnd.get(2)]));
					}
				}
			}
		} 
  	return super.onNodeArrived(npc);
	}

	@Override
	public String onMoveFinished(L2Npc npc)
	{
		// Npc moved to fire
		if (npc.isNoRndWalk())
		{
			//npc.setRHandId(OHS_Weapon);
			startQuestTimer("fire_arrived", 3000, npc, null);
		}		

		return super.onMoveFinished(npc);
	}
	
	private L2ZoneType getZone(L2Npc npc, String nameTemplate, boolean currentLoc)
	{
		try
		{
			int x;
			int y;
			int z;

			if (currentLoc)
			{
				x = npc.getX();
				y = npc.getY();
				z = npc.getZ();
			}
			else
			{
				x = npc.getSpawn().getLocx();
				y = npc.getSpawn().getLocy();
				z = npc.getSpawn().getLocz();
			}
			
			for (L2ZoneType zone : ZoneManager.getInstance().getZones(x, y, z))
			{
				if (zone.getName().startsWith(nameTemplate))
					return zone;
			}
		}

		catch(NullPointerException e)
		{
		}

		catch(IndexOutOfBoundsException e)
		{
		}
		
		return null;
	}
	
	/**
	 * Returns monsters in their spawn location
	 */	 	
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
					L2ZoneType zone = getZone(monster, "sel_mahum_training_grounds", false);
					if (zone != null && zone.getId() == _zoneId)
					{
						if (monster.getX() != sp.getLocx() && monster.getY() != sp.getLocy()) //Check if there is monster not in spawn location
						{
							//Teleport him if not engaged in battle / not flee
							if (monster.getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE || monster.getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE) 
							{
								monster.setHeading(sp.getHeading());
								monster.teleToLocation(sp.getLocx(), sp.getLocy(), sp.getLocz());
							}
							else //There is monster('s) not in spawn location, but engaged in battle / flee. Set flag to repeat Return Task for this zone 
								_runAgain = true;
						}
					}
				}   
			}
			if (_runAgain) //repeat task
				ThreadPoolManager.getInstance().scheduleGeneral(new ReturnTask(_zoneId), 120000);
			else // Task is not sheduled ahain for this zone, unregister it
				_scheduledReturnTasks.remove(_zoneId);
		}
	}
	
	private static int getRoute(L2Npc npc)
	{
		int ret = -1;

		if (npc.getNpcId() != CHEF)
			return ret;

		switch (npc.getSpawn().getLocx())
		{
			case 85852: // 85852;53212;-3624 Cooker_01
			case 82814: // 82814;69481;-3192 Cooker_09
				ret = 11;
				break;
			case 93964: // 93964;55692;-3352  Cooker_02
				ret = 12;
				break;
			case 87612: // 87612;59356;-3552  Cooker_03
			case 88532: // 88532;60352;-3642  Cooker_05
				ret = 13;
				break;
			case 83724: // 83724;62668;-3472  Cooker_04							
				ret = 14;
				break;
			case 92981: // 92981;60834;-3288  Cooker_06
				ret = 15;
				break;
			case 78332: // 78332;63440;-3640 Cooker_07
				ret = 16;
				break;
			case 77836: // 77836;68796;-3312 Cooker_08
				ret = 17;
				break;
			case 83404: // 83404;65772;-3032 Cooker_10
				ret = 18;
				break;
			case 96487: // 96487;69432;-3408 Cooker_11
				ret = 19;
				break;
			case 91238: // 91238;67728;-3631 Cooker_12
				ret = 20;
				break;
		}

		return ret;
	}	

	private void moveToRandomPoint(L2ZoneType zone, L2MonsterInstance monster, L2Npc npc, int minRange, int maxRange)
	{
		int[] coord = null;
		for (int i = 0; i < 1000; i++)
		{
			coord = zone.getZone().getRandomPoint();
			if (Util.calculateDistance(npc.getX(), npc.getY(), coord[0], coord[1]) >= minRange && Util.calculateDistance(npc.getX(), npc.getY(), coord[0], coord[1]) <= maxRange)
			{
				break;
			} 
		}
		if (coord != null)
		{
				monster.stopMove(null);
				monster.getAI().setIntention( CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(coord[0], coord[1], npc.getZ(), npc.getHeading()));
		}
	
	}
	
	public static void main(String[] args)
	{
		// now call the constructor (starts up the ai)
		new SelMahums(-1,"sel_mahums","ai");
	}
}
