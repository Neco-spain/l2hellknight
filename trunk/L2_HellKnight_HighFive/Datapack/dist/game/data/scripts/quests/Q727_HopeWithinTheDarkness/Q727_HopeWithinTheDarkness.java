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
package quests.Q727_HopeWithinTheDarkness;

import gnu.trove.map.hash.TIntObjectHashMap;

import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.ThreadPoolManager;
import l2.hellknight.gameserver.instancemanager.FortManager;
import l2.hellknight.gameserver.instancemanager.GlobalVariablesManager;
import l2.hellknight.gameserver.instancemanager.InstanceManager;
import l2.hellknight.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2.hellknight.gameserver.model.L2Party;
import l2.hellknight.gameserver.model.actor.L2Attackable;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.L2Playable;
import l2.hellknight.gameserver.model.actor.instance.L2QuestGuardInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.entity.Castle;
import l2.hellknight.gameserver.model.entity.Fort;
import l2.hellknight.gameserver.model.entity.Instance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;
import l2.hellknight.gameserver.network.NpcStringId;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.clientpackets.Say2;
import l2.hellknight.gameserver.network.serverpackets.NpcSay;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;
import l2.hellknight.gameserver.model.holders.SkillHolder;
import l2.hellknight.gameserver.util.Util;
//import l2.hellknight.util.Rnd;

//import java.lang.Math;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import javolution.util.FastMap; 

public final class Q727_HopeWithinTheDarkness extends Quest
{

	private class CAUWorld extends InstanceWorld
	{
		public boolean underAttack = false;
		public boolean allMonstersDead = true;
	}
	
	public static class CastleDungeon
	{
		private final int INSTANCEID;
		private final int _wardenId;
		
		public CastleDungeon(int iId, int wardenId)
		{
			INSTANCEID = iId;
			_wardenId = wardenId;			
		}
		
		public int getInstanceId()
		{
			return INSTANCEID;
		}
		
		public long getReEnterTime()
		{
			String tmp = GlobalVariablesManager.getInstance().getStoredVariable ("Castle_dungeon_" + Integer.toString(_wardenId));
			
			return tmp == null ? 0 : Long.parseLong(tmp);
		}
		
		public void setReEnterTime(long time)
		{
			GlobalVariablesManager.getInstance().storeVariable("Castle_dungeon_" + Integer.toString(_wardenId), Long.toString(time));
		}
	}
	
	private static final String qn = "727_HopeWithinTheDarkness";
	private static final boolean debug = false;
	private static final boolean CHECK_FOR_CONTRACT = false;
	private static final long REENTER_INTERVAL = 14400000;
	private static final long INITIAL_SPAWN_DELAY = 120000; //Spawn NPC's and 1'st Wave bosses (2 min)
	private static final long WAVE_SPAWN_DELAY = 480000; //Spawn next wave's bosses (8 min)
	private static final long PRIVATE_SPAWN_DELAY = 180000; //Spawn monsters (3 min after boss had been spawned)
	
	private TIntObjectHashMap<CastleDungeon> _castleDungeons = new TIntObjectHashMap<CastleDungeon>(21);
	
	// REWARDS
	private static final int KNIGHT_EPALUETTE = 9912;

	//NPC's
	private static final int NPC_KNIGHT = 36562;
	private static final int NPC_RANGER = 36563;
	private static final int NPC_MAGE = 36564;
	private static final int NPC_WARRIOR = 36565;

	private static final int[] BOSSES = { 25653, 25654, 25655};
	private static final int[] MONSTERS = { 25656, 25657, 25658 };


	//Spawns
	private static final int[][] BOSSES_FIRST_WAVE =
	{
		{BOSSES[0], 50943, -12224, -9321 ,32768}
	};	

	private static final int[][] BOSSES_SECOND_WAVE =
	{
		{BOSSES[1], 50943, -12224, -9321 ,32768}
	};	

	private static final int[][] BOSSES_THIRD_WAVE =
	{
		{BOSSES[2], 50943, -12004, -9321, 32768},
		{BOSSES[2], 50943, -12475, -9321, 32768}
	};	
 
	private static final int[][] MONSTERS_FIRST_WAVE =
	{
		{MONSTERS[0], 50343, -12552, -9388, 32768},
		{MONSTERS[0], 50344, -12340, -9380, 32768},
		{MONSTERS[0], 50341, -12134, -9381, 32768},
		{MONSTERS[0], 50342, -11917, -9389, 32768},
		{MONSTERS[0], 50476, -12461, -9392, 32768},
		{MONSTERS[0], 50481, -12021, -9390, 32768},
		{MONSTERS[0], 50605, -12407, -9392, 32768},
		{MONSTERS[0], 50602, -12239, -9380, 32768},
		{MONSTERS[0], 50606, -12054, -9390, 32768}		
	};	
  
	private static final int[][] MONSTERS_SECOND_WAVE =
	{
		{MONSTERS[1], 50343, -12552, -9388, 32768},
		{MONSTERS[1], 50344, -12340, -9380, 32768},
		{MONSTERS[1], 50341, -12134, -9381, 32768},
		{MONSTERS[1], 50342, -11917, -9389, 32768},
		{MONSTERS[1], 50476, -12461, -9392, 32768},
		{MONSTERS[1], 50481, -12021, -9390, 32768},
		{MONSTERS[1], 50605, -12407, -9392, 32768},
		{MONSTERS[1], 50602, -12239, -9380, 32768},
		{MONSTERS[1], 50606, -12054, -9390, 32768}
	};

  private static final int[][] MONSTERS_THIRD_WAVE =
  {
		{MONSTERS[1], 50343, -12552, -9388, 32768},
		{MONSTERS[1], 50344, -12340, -9380, 32768},
		{MONSTERS[1], 50341, -12134, -9381, 32768},
		{MONSTERS[1], 50342, -11917, -9389, 32768},
		{MONSTERS[2], 50476, -12461, -9392, 32768},
		{MONSTERS[2], 50481, -12021, -9390, 32768},
		{MONSTERS[2], 50605, -12407, -9392, 32768},
		{MONSTERS[2], 50602, -12239, -9380, 32768},
		{MONSTERS[2], 50606, -12054, -9390, 32768}		  
	};
  
	//NPCs'_SPAWNS (id, x, y, z, heading)
	private static final int[][] NPCS = 
	{
		{ NPC_WARRIOR, 49093, -12077, -9395, 0 },
		{ NPC_RANGER, 49094, -12238, -9386, 0 },
		{ NPC_MAGE, 49093, -12401, -9388, 0 },
		{ NPC_KNIGHT, 49232, -12239, -9386, 0 }
	};

	//Strings
	private static final NpcStringId[] NPC_INJURED_FSTRINGID = { NpcStringId.YOUR_MIND_IS_GOING_BLANK, NpcStringId.YOUR_MIND_IS_GOING_BLANK }; 
	private static final NpcStringId[] NPC_DIE_FSTRINGID = { NpcStringId.I_CANT_STAND_IT_ANYMORE_AAH, NpcStringId.I_CANT_STAND_IT_ANYMORE_AAH, NpcStringId.KYAAAK, NpcStringId.GASP_HOW_CAN_THIS_BE};
	private static final NpcStringId NPC_WIN_FSTRINGID = NpcStringId.YOUVE_DONE_IT_WE_BELIEVED_IN_YOU_WARRIOR_WE_WANT_TO_SHOW_OUR_SINCERITY_THOUGH_IT_IS_SMALL_PLEASE_GIVE_ME_SOME_OF_YOUR_TIME;
	private static final NpcStringId BOSS_DIE_FSTRINGID = NpcStringId.HOW_DARE_YOU;
	private static final NpcStringId[] BOSS_SPAWN_FSTRINGID = { NpcStringId.ILL_RIP_THE_FLESH_FROM_YOUR_BONES, NpcStringId.ILL_RIP_THE_FLESH_FROM_YOUR_BONES, NpcStringId.YOULL_FLOUNDER_IN_DELUSION_FOR_THE_REST_OF_YOUR_LIFE };
	 

	//Buffs
	private static Map<Integer, SkillHolder> NPC_BUFFS = new FastMap<Integer, SkillHolder>();
	private static final SkillHolder RAID_CURSE = new SkillHolder(5456, 1);
	
	private String checkEnterConditions(L2PcInstance player, L2Npc npc)
	{
		if (debug)
			return null;

		Castle castle = npc.getCastle();
		CastleDungeon dungeon = _castleDungeons.get(npc.getNpcId());
		boolean haveContract = false;

		if (player == null || castle == null || dungeon == null)
			return "CastleWarden-03.htm";
		
		//check if castle have contract with fortress
		if (CHECK_FOR_CONTRACT)
		{
			for (Fort fort : FortManager.getInstance().getForts())
			{
				if (fort.getCastleId() == castle.getCastleId())
				{
					haveContract = true;
					break;
				}
			}
		
			if (!haveContract)
				return "CastleWarden-13a.htm";
		}

		QuestState st = player.getQuestState(qn);
		
		//check if player have quest
		if (st == null || st.getInt("cond") < 1)
		{
			//check if player is from clan, that owns castle
			if (player.getClan() == null || player.getClan().getCastleId() != castle.getCastleId())
				return "CastleWarden-08.htm";
			
			if (player.getLevel() >= 80)
				return "CastleWarden-06.htm";
			
			return "CastleWarden-07.htm";
		}
		
		L2Party party = player.getParty();
		
		if (party == null)
			return "CastleWarden-09.htm";

		if (party.getLeader() != player)
			return getHtm(player.getHtmlPrefix(), "CastleWarden-10.htm").replace("%leader%", party.getLeader().getName());

		for (L2PcInstance partyMember : party.getMembers())
		{
			st = partyMember.getQuestState(qn);
			
			//check if each party member has quest
			if (st == null || st.getInt("cond") < 1)
				return getHtm(player.getHtmlPrefix(), "CastleWarden-12.htm").replace("%player%", partyMember.getName());

			if (player.getClan() == null || player.getClan().getCastleId() != castle.getCastleId())
				return getHtm(player.getHtmlPrefix(), "CastleWarden-11.htm").replace("%player%", partyMember.getName());

			//check if each party member not very far from leader
			if (!Util.checkIfInRange(1000, player, partyMember, true))
				return getHtm(player.getHtmlPrefix(), "CastleWarden-17.htm").replace("%player%", partyMember.getName());
		}
		
		if (dungeon.getReEnterTime() > System.currentTimeMillis())
			return "CastleWarden-18.htm";

		return null;
	}

	private void teleportPlayer(L2PcInstance player, int[] coords, int instanceId)
	{
		player.setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2]);
	}

	protected String enterInstance(L2PcInstance player, String template, int[] coords, CastleDungeon dungeon, String ret)
	{
		//check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		//existing instance
		if (world != null)
		{
			if (!(world instanceof CAUWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return "";
			}
			teleportPlayer(player, coords, world.instanceId);
			return "";
		}
		//New instance
		if (ret != null)
			return ret;

		L2Party party = player.getParty();
		int instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		Instance ins = InstanceManager.getInstance().getInstance(instanceId);
		ins.setSpawnLoc(new int[]{player.getX(),player.getY(),player.getZ()});
		world = new CAUWorld();
		world.instanceId = instanceId;
		world.templateId = dungeon.getInstanceId();
		world.status = 0;
		dungeon.setReEnterTime(System.currentTimeMillis() + REENTER_INTERVAL);
		InstanceManager.getInstance().addWorld(world);
		_log.info("Castle HopeWithinTheDarkness started " + template + " Instance: " + instanceId + " created by player: " + player.getName());
		ThreadPoolManager.getInstance().scheduleGeneral(new spawnNpcs((CAUWorld) world), INITIAL_SPAWN_DELAY);

		// teleport players
		if (player.getParty() == null)
		{
			return "CastleWarden-09.htm";
		}
		else if ((player.getParty() != null))
		{
			for (L2PcInstance partyMember : party.getMembers())
			{
				teleportPlayer(partyMember, coords, instanceId);
				world.allowed.add(partyMember.getObjectId());
				if (partyMember.getQuestState(qn) == null)
					newQuestState(partyMember);
				
				partyMember.getQuestState(qn).set("cond", "2");
			}
		}
		return getHtm(player.getHtmlPrefix(), "CastleWarden-13.htm").replace("%clan%", player.getClan().getName());
	}
	
	//Spawns npc's and bosses
	private class spawnNpcs  implements Runnable
	{
		private CAUWorld _world;
		
		public spawnNpcs(CAUWorld world)
		{
			_world = world;
		}
		
		@Override
		public void run()
		{
			try
			{
				if (_world.status == 0)
				{
					for(int[] spawn : NPCS)
						addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, _world.instanceId);
					for(int[] spawn : BOSSES_FIRST_WAVE)
						addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, _world.instanceId);
					
					ThreadPoolManager.getInstance().scheduleGeneral(new spawnNpcs(_world), WAVE_SPAWN_DELAY);
					ThreadPoolManager.getInstance().scheduleGeneral(new spawnPrivates(_world), PRIVATE_SPAWN_DELAY);
				}
				else if (_world.status == 1)
				{
					for(int[] spawn : BOSSES_SECOND_WAVE)
						addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, _world.instanceId);

					ThreadPoolManager.getInstance().scheduleGeneral(new spawnNpcs(_world), WAVE_SPAWN_DELAY);
					ThreadPoolManager.getInstance().scheduleGeneral(new spawnPrivates(_world), PRIVATE_SPAWN_DELAY);
				}
				else if (_world.status == 2)
				{
					for(int[] spawn : BOSSES_THIRD_WAVE)
						addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, _world.instanceId);

					ThreadPoolManager.getInstance().scheduleGeneral(new spawnPrivates(_world), PRIVATE_SPAWN_DELAY);
				}
			}
			catch (Exception e)
			{
				_log.warning("Castle HopeWithinTheDarkness NPC Spawn error: " + e);
			}
		}
	}

	//Spawns monsters (minions) 
	private class spawnPrivates  implements Runnable
	{
		private CAUWorld _world;
		
		public spawnPrivates(CAUWorld world)
		{
			_world = world;
		}
		
		@Override
		public void run()
		{
			try
			{
				if (_world.status == 0)
				{
					for(int[] spawn : MONSTERS_FIRST_WAVE)
						addSpawn(spawn[0],spawn[1],spawn[2],spawn[3],spawn[4],false,0,false, _world.instanceId);
					
					_world.underAttack = true;
				}
				else if (_world.status == 1)
				{
					for(int[] spawn : MONSTERS_SECOND_WAVE)
						addSpawn(spawn[0],spawn[1],spawn[2],spawn[3],spawn[4],false,0,false, _world.instanceId);
				}
				else if (_world.status == 2)
				{
					for(int[] spawn : MONSTERS_THIRD_WAVE)
						addSpawn(spawn[0],spawn[1],spawn[2],spawn[3],spawn[4],false,0,false, _world.instanceId);
				}
				
				_world.status++;
			}
			catch (Exception e)
			{
				_log.warning("Castle HopeWithinTheDarkness Monster Spawn error: " + e);
			}
		}
	}
	
	//Manages win event
	private class Win implements Runnable
	{
		private CAUWorld _world;
		private L2PcInstance _player;
		
		public Win(CAUWorld world, L2PcInstance player)
		{
			_world = world;
			_player = player;
		}
		
		@Override
		public void run()
		{
			try
			{
				if (_world.status == 4)
				{
					_world.underAttack = false;

					Instance inst = InstanceManager.getInstance().getInstance(_world.instanceId);
					
					for (L2Npc _npc : inst.getNpcs())
					{
					
						if (_npc != null && (_npc.getNpcId() >= NPC_KNIGHT && _npc.getNpcId() <= NPC_WARRIOR))
						{
							cancelQuestTimer("check_for_foes", _npc, null);
							cancelQuestTimer("buff", _npc, null);
							
							if (_npc.getNpcId() == NPC_KNIGHT)
								_npc.broadcastPacket(new NpcSay(_npc.getObjectId(), Say2.SHOUT, _npc.getNpcId(), NPC_WIN_FSTRINGID));
						}
					}
					
					if (_player != null)
					{
						L2Party party = _player.getParty();
						
						if (party == null)
							rewardPlayer(_player);
						else
						{
							for (L2PcInstance partyMember : party.getMembers())
							{
								if (partyMember != null && partyMember.getInstanceId() == _player.getInstanceId())
									rewardPlayer(partyMember);
							}
						}
					}
				}
			}
			catch (Exception e)
			{
				_log.warning("Win manage error: " + e);
			}
		}
	}

	private void rewardPlayer(L2PcInstance player)
	{
			QuestState st = player.getQuestState(qn);
			if (st != null && st.getInt("cond") == 2)
				st.set("cond", "3");
	}

	@Override
	public String onAdvEvent (String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		if (event.equalsIgnoreCase("enter"))
		{
			int[] tele = new int[3];
			tele[0] = 48163;
			tele[1] = -12195;
			tele[2] = -9140;

			//htmltext = checkEnterConditions(player, npc);
			
				return enterInstance(player, "castlepailaka.xml", tele, _castleDungeons.get(npc.getNpcId()), checkEnterConditions(player, npc));
		}

		else if (event.equalsIgnoreCase("suicide"))
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			tmpworld.status = 5;
			Instance inst = InstanceManager.getInstance().getInstance(npc.getInstanceId());
			if (inst != null)
			{
				for (L2Npc _npc : inst.getNpcs())
				{
					if (_npc != null && (_npc.getNpcId() >= NPC_KNIGHT && _npc.getNpcId() <= NPC_WARRIOR))
					{
						cancelQuestTimer("check_for_foes", _npc, null);
						cancelQuestTimer("buff", _npc, null);

						if (!_npc.isDead())
							_npc.doDie(null);
					}
				}
				
				//Destroy instance after 5 minutes
				inst.setDuration(5 * 60000);
				inst.setEmptyDestroyTime(0);
			}
		
			return null;
		}
		
		else if (event.equalsIgnoreCase("buff"))
		{
			Collection<L2PcInstance> players = npc.getKnownList().getKnownPlayers().values();
			for (L2PcInstance pl : players)
			{
				if (pl != null && Util.checkIfInRange(75, npc, pl, false) && NPC_BUFFS.get(npc.getNpcId()) != null)
				{
					npc.setTarget(pl);
					npc.doCast(NPC_BUFFS.get(npc.getNpcId()).getSkill());
				}
			}
			startQuestTimer("buff", 120000, npc, null);
			return null;
		}
		
		else if (event.equalsIgnoreCase("check_for_foes"))
		{
			if (npc.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
			{
				for (L2Character foe : npc.getKnownList().getKnownCharactersInRadius(npc.getAggroRange()))
				{
					if (foe instanceof L2Attackable && !(foe instanceof L2QuestGuardInstance))
					{
								((L2QuestGuardInstance) npc).addDamageHate(foe, 0, 999);
								((L2QuestGuardInstance) npc).getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, foe, null);
					}
				}
			}
			
			startQuestTimer("check_for_foes", 5000, npc, null);
			return null;
		}

		QuestState st = player.getQuestState(qn);
		if (st == null)
			st = newQuestState(player);

		int cond = st.getInt("cond");
		if (event.equalsIgnoreCase("CastleWarden-05.htm"))
		{
			if (cond == 0)
			{
				st.set("cond","1");
				st.setState(State.STARTED);
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if (event.equalsIgnoreCase("CastleWarden-15.htm"))
		{
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(true);
		}
		return htmltext;
	}

	@Override
	public String onFirstTalk (L2Npc npc, L2PcInstance player)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(player.getInstanceId());
		if (tmpworld instanceof CAUWorld)
		{
			CAUWorld world = (CAUWorld) tmpworld;
			
			if (world.underAttack)
				return "Victim-02.htm";
			
			else if (world.status == 4)
				return "Victim-03.htm";
			
			else
				return "Victim-01.htm";
		}
		
		return null;
	}

	@Override
	public String onTalk (L2Npc npc, L2PcInstance player)
	{
		String htmltext = Quest.getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);

		if (npc.getNpcId() >= NPC_KNIGHT && npc.getNpcId() <= NPC_WARRIOR)
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(player.getInstanceId());
			if (tmpworld != null && tmpworld instanceof CAUWorld)
			{
				CAUWorld world = (CAUWorld) tmpworld;

				world.allowed.remove(world.allowed.indexOf(player.getObjectId()));
				final Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
				teleportPlayer(player, inst.getSpawnLoc(), 0);
				return null;
			}
		}

		if (player.getClan() == null || npc.getCastle() == null || player.getClan().getCastleId() != npc.getCastle().getCastleId())
			return "CastleWarden-03.htm";

		else if (st != null)
		{
			int npcId = npc.getNpcId();
			int cond = 0;
			if (st.getState() == State.CREATED)
				st.set("cond","0");
			else
				cond = st.getInt("cond");
			if (_castleDungeons.containsKey(npcId) && cond == 0)
			{
				if (player.getLevel() >= 80)
					htmltext = "CastleWarden-01.htm";
				else
				{
					htmltext = "CastleWarden-04.htm";
					st.exitQuest(true);
				}
			}
			else if (_castleDungeons.containsKey(npcId) && cond > 0 && st.getState() == State.STARTED)
			{
				if (cond == 1)
				{
					htmltext = "CastleWarden-15.htm";
				}
				else if (cond == 3)
				{
					st.playSound("ItemSound.quest_finish");
					st.giveItems(KNIGHT_EPALUETTE, 159);
					st.exitQuest(true);
					htmltext = "CastleWarden-16.htm";
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onAttack(L2Npc npc,L2PcInstance player, int damage, boolean isPet)
	{
		if (npc.getNpcId() >= NPC_KNIGHT && npc.getNpcId() <= NPC_WARRIOR)
		{
			if (npc.getCurrentHp() <= npc.getMaxHp() * 0.1)
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getNpcId(), NPC_INJURED_FSTRINGID[1]));
			else if (npc.getCurrentHp() <= npc.getMaxHp() * 0.4)
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getNpcId(), NPC_INJURED_FSTRINGID[0]));
			
			return null;
		} 

		if (player != null)
		{
			L2Playable attacker = (isPet ? player.getPet() : player);
			if (attacker.getLevel() - npc.getLevel() >= 9)
			{
				if (attacker.getBuffCount() > 0 || attacker.getDanceCount() > 0)
				{
					npc.setTarget(attacker);
					npc.doSimultaneousCast(RAID_CURSE.getSkill());
				}
				else if (player.getParty() != null)
					for(L2PcInstance pmember : player.getParty().getMembers())
					{
						if (pmember.getBuffCount() > 0 || pmember.getDanceCount() > 0)
						{
							npc.setTarget(pmember);
							npc.doSimultaneousCast(RAID_CURSE.getSkill());
						}
					}
			}
		}
		return super.onAttack(npc, player, damage, isPet);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if (npc.getNpcId() >= NPC_KNIGHT && npc.getNpcId() <= NPC_WARRIOR)
		{
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getNpcId(), NPC_DIE_FSTRINGID[npc.getNpcId() - 36562]));
				
			//All other friendly NPC's do suicide - start timer
			startQuestTimer("suicide", 1500, npc, null);
			cancelQuestTimer("check_for_foes", npc, null);
			cancelQuestTimer("buff", npc, null);
			return null;
		}


		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof CAUWorld)
		{
			CAUWorld world = (CAUWorld) tmpworld;
			if (Util.contains(BOSSES, npc.getNpcId()))
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getNpcId(), BOSS_DIE_FSTRINGID));

			if 	(tmpworld.status == 3 && (Util.contains(BOSSES, npc.getNpcId()) || Util.contains(MONSTERS, npc.getNpcId())))
			{
				world.allMonstersDead = true;
				Instance inst = InstanceManager.getInstance().getInstance(tmpworld.instanceId);
				
				if (inst != null)
				{
					for (L2Npc _npc : inst.getNpcs())
					{
						if (_npc != null && !_npc.isDead() && (Util.contains(BOSSES, _npc.getNpcId()) || Util.contains(MONSTERS, _npc.getNpcId())))
						{
							world.allMonstersDead = false;
							break;
						}
						
					}
					
					
					if (world.allMonstersDead)
					{
						tmpworld.status = 4;
						
						//Destroy instance after 5 minutes
						inst.setDuration(5 * 60000);
						inst.setEmptyDestroyTime(0);
						ThreadPoolManager.getInstance().scheduleGeneral(new Win(world, player), 1500);
					}
				}
			}
		}

		return null;
	}

  @Override
	public final String onSpawn(L2Npc npc)
	{
		//buff players every two minutes, check for foes in aggro range
		if (npc.getNpcId() >= NPC_KNIGHT && npc.getNpcId() <= NPC_WARRIOR)
		{
			startQuestTimer("buff", 120000, npc, null);
			startQuestTimer("check_for_foes", 120000, npc, null);

			if (npc.getNpcId() == NPC_KNIGHT)
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getNpcId(), NpcStringId.WARRIORS_HAVE_YOU_COME_TO_HELP_THOSE_WHO_ARE_IMPRISONED_HERE));
		} 
		
		else if (Arrays.binarySearch(BOSSES, npc.getNpcId()) >= 0)
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getNpcId(), BOSS_SPAWN_FSTRINGID[Arrays.binarySearch(BOSSES, npc.getNpcId())]));
			
		return null;
	}
	
	public Q727_HopeWithinTheDarkness(int questId, String name, String descr)
	{
		super(questId, name, descr);
		_castleDungeons.put(36403, new CastleDungeon(80, 36403));
		_castleDungeons.put(36404, new CastleDungeon(81, 36404));
		_castleDungeons.put(36405, new CastleDungeon(82, 36405));
		_castleDungeons.put(36406, new CastleDungeon(83, 36406));
		_castleDungeons.put(36407, new CastleDungeon(84, 36407));
		_castleDungeons.put(36408, new CastleDungeon(85, 36408));
		_castleDungeons.put(36409, new CastleDungeon(86, 36409));
		_castleDungeons.put(36410, new CastleDungeon(87, 36410));
		_castleDungeons.put(36411, new CastleDungeon(88, 36411));
		
		NPC_BUFFS.put(NPC_KNIGHT, new SkillHolder(5970, 1));
		NPC_BUFFS.put(NPC_RANGER, new SkillHolder(5971, 1));
		NPC_BUFFS.put(NPC_MAGE, new SkillHolder(5972, 1));
		NPC_BUFFS.put(NPC_WARRIOR, new SkillHolder(5973, 1));

		for(int i : _castleDungeons.keys())
		{
			addStartNpc(i);
			addTalkId(i);
		}
		
		for (int i = NPC_KNIGHT; i <= NPC_WARRIOR; i++)
		{
			addSpawnId(i);
			addKillId(i);
			addAttackId(i);
			addTalkId(i);
			addFirstTalkId(i);
		}
		
		for(int i : BOSSES)
		{
			addSpawnId(i);
			addKillId(i);
			addAttackId(i);
		} 
		
		for(int i : MONSTERS)
		{
			addKillId(i);
			addAttackId(i);
		} 
		
	}
	
	public static void main(String[] args)
	{
		// now call the constructor (starts up the)
		new Q727_HopeWithinTheDarkness(727, qn, "Hope Within The Darkness");
	}
}
