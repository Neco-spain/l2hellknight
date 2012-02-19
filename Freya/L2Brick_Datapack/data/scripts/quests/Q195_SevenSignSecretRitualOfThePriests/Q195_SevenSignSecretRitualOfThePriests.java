package quests.Q195_SevenSignSecretRitualOfThePriests;

import java.util.ArrayList;
import java.util.Collection;

import l2.brick.Config;
import l2.brick.gameserver.ThreadPoolManager;
import l2.brick.gameserver.ai.CtrlIntention;
import l2.brick.gameserver.datatables.SkillTable;
import l2.brick.gameserver.instancemanager.InstanceManager;
import l2.brick.gameserver.instancemanager.ZoneManager;
import l2.brick.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2.brick.gameserver.model.L2CharPosition;
import l2.brick.gameserver.model.actor.L2Character;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2DoorInstance;
import l2.brick.gameserver.model.actor.instance.L2MonsterInstance;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.entity.Instance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;
import l2.brick.gameserver.model.quest.State;
import l2.brick.gameserver.model.zone.L2ZoneType;
import l2.brick.gameserver.network.SystemMessageId;
import l2.brick.gameserver.network.serverpackets.ExStartScenePlayer;
import l2.brick.gameserver.network.serverpackets.MagicSkillUse;
import l2.brick.gameserver.network.serverpackets.MoveToLocation;
import l2.brick.gameserver.network.serverpackets.SystemMessage;
import l2.brick.gameserver.util.Broadcast;
import l2.brick.gameserver.util.Util;
import l2.brick.util.Rnd;

public class Q195_SevenSignSecretRitualOfThePriests extends Quest
{
	private static final String qn = "Q195_SevenSignSecretRitualOfThePriests";

	// INSTANCE DATA
	private static final int INSTANCE_ID = 111;
	
	// TELEPORT DATA
	private static final int[] ENTER_COORDS 	= { -76158, 213412, -7120 };
	private static final int[] EXIT_COORDS 		= { -12532, 122329, -2984 };
	private static final int[] TELE_IF_DETECTED1 	= { -74959, 209240, -7472 };
	private static final int[] TELE_IF_DETECTED2 	= { -77706, 208994, -7616 };
	private static final int[] TELE_IF_DETECTED3 	= { -80176, 205855, -7893 };
	private static final int[] TELE_IF_WRONG_CODE3 	= { -78240, 205858, -7856 };
	
	// ZONES
	private static final int[] ZONES = { 20500, 20501, 20502, 20503 };
	
	// NPC
	private static final int CLAUDIA_ATHEBALDT 	= 31001;
	private static final int JHON 			= 32576;
	private static final int RAYMOND 		= 30289;
	private static final int IASON_HEINE 		= 30969;
	private static final int LIGHT_OF_DAWN 		= 32575;
	private static final int DARKNESS_OF_DAWN 	= 32579;
	private static final int IDENTITY_CONFIRM_DEVICE = 32578;
	private static final int PASSWORD_ENTRY_DEVICE 	= 32577;
	
	// MONSTERS
	private static final int[] MONSTERS = { 18834, 18835, 27351 };
	
	// MONSTERS TEXT
	private static final String[] TEXT = 
	{
		"How dare you intrude with that transformation! Get lost!",
		"Intruder! Protect the Priests of Dawn!",
		"Who are you?! A new face like you can't approach this place!"
	};
	
	// CORRECT BOOKSHELF
	private static final int[] CORRECT_BOOK 	= { 32581, -81393, 205565 };
	
	// ITEMS
	private static final int IDENTITY_CARD 		= 13822;
	private static final int EMPEROR_CONTRACT 	= 13823;
	
	// TRANSFORMATION
	private static final int GUARD = 6204;
	
	// DOORS
	private static final int START_DOORS 		= 17240001;
	private static final int MIDDLE_DOORS 		= 17240003;
	private static final int FINAL_DOORS 		= 17240005;
	
	// GUARDS
	private static final int[][] MOVING_GUARDS =
	{
		{ 18835, -75048, 212116, -7318, -74842, 212116, -7318 },
		{ 18835, -75371, 212116, -7317, -75628, 212116, -7319 },
		{ 18835, -74480, 212116, -7319, -74253, 212116, -7319 },
		{ 18835, -74703, 211466, -7317, -74703, 211172, -7319 },
		{ 18835, -75197, 211466, -7317, -75197, 211172, -7319 },
		{ 18834, -75245, 210148, -7415, -74677, 210148, -7415 },
		{ 18834, -74683, 209819, -7415, -75241, 209819, -7415 },
		{ 18834, -74224, 208285, -7511, -74498, 208285, -7511 },
		{ 18834, -74202, 207063, -7509, -74508, 207063, -7509 },
		{ 18834, -74954, 206671, -7511, -74954, 206356, -7511 },
		{ 18834, -74270, 206518, -7511, -75654, 206518, -7511 },
		{ 18834, -75412, 206894, -7504, -75699, 206894, -7504 },
		{ 18834, -75553, 208838, -7511, -75553, 207660, -7511 },
		{ 18834, -76390, 207855, -7607, -76623, 207855, -7607 },
		{ 18834, -76610, 208182, -7606, -76392, 208182, -7606 },
		{ 18834, -76384, 208832, -7606, -76620, 208832, -7606 },
		{ 18834, -76914, 209443, -7610, -76914, 209195, -7610 },
		{ 18834, -77188, 209191, -7607, -77188, 209440, -7607 },
		{ 18835, -78039, 208472, -7703, -77369, 208472, -7703 },
		{ 18835, -77703, 208231, -7701, -77703, 207284, -7701 },
		{ 18835, -77304, 208027, -7701, -76979, 208027, -7703 },
		{ 18835, -77044, 207796, -7701, -78350, 207796, -7704 },
		{ 18835, -78085, 208038, -7701, -78454, 208038, -7703 },
		{ 18835, -77336, 207413, -7702, -77032, 207112, -7703 },
		{ 18834, -78894, 206130, -7893, -78729, 206298, -7893 },
		{ 18834, -79050, 206272, -7893, -78874, 206442, -7893 },
		{ 18834, -79360, 206372, -7893, -79360, 206718, -7893 },
		{ 18834, -78910, 205582, -7893, -78748, 205416, -7893 },
		{ 18834, -79057, 205436, -7893, -78899, 205275, -7893 },
		{ 18834, -79361, 205336, -7893, -79363, 204998, -7893 },
		{ 18834, -79655, 205440, -7893, -79820, 205273, -7893 },
		{ 18834, -79802, 205579, -7893, -79964, 205415, -7893 },
		{ 18834, -79792, 206111, -7893, -79964, 206295, -7893 },
		{ 18834, -79648, 206258, -7893, -79814, 206430, -7893 },
		{ 27351, -81963, 205857, -7989, -81085, 205857, -7989 }
	};
	
	public Q195_SevenSignSecretRitualOfThePriests(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(CLAUDIA_ATHEBALDT);
		addTalkId(CLAUDIA_ATHEBALDT);
		addTalkId(JHON);
		addTalkId(RAYMOND);
		addTalkId(IASON_HEINE);
		addTalkId(LIGHT_OF_DAWN);
		addTalkId(IDENTITY_CONFIRM_DEVICE);
		addTalkId(DARKNESS_OF_DAWN);
		addTalkId(PASSWORD_ENTRY_DEVICE);
		addTalkId(CORRECT_BOOK[0]);
		for (int i : MONSTERS)
		{
			addAggroRangeEnterId(i);
			addAttackId(i);
		}
		
		questItemIds = new int[] { IDENTITY_CARD, EMPEROR_CONTRACT };
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		if (st == null)
			return null;
		
		String htmltext = event;

		if (event.equalsIgnoreCase("31001-4.htm"))
		{
			st.set("cond","1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("32576-1.htm"))
		{
			st.set("cond","2");
			st.giveItems(IDENTITY_CARD, 1);
		}
		else if (event.equalsIgnoreCase("30289-3.htm"))
		{
			st.set("cond","3");
			st.getPlayer().doCast(SkillTable.getInstance().getInfo(GUARD, 1));
		}
		else if (event.equalsIgnoreCase("30289-6.htm"))
		{
			if (st.getPlayer().isTransformed())
				st.getPlayer().untransform();
			st.getPlayer().doCast(SkillTable.getInstance().getInfo(GUARD, 1));
		}
		else if (event.equalsIgnoreCase("30289-7.htm"))
		{
			if (st.getPlayer().isTransformed())
				st.getPlayer().untransform();
		}
		else if (event.equalsIgnoreCase("30289-10.htm"))
		{
			if (st.getPlayer().isTransformed())
				st.getPlayer().untransform();
			st.set("cond","4");
		}
		else if (event.equalsIgnoreCase("32581-3.htm"))
		{
			int instID = st.getPlayer().getInstanceId();
    			if (instID != 0)
    				InstanceManager.getInstance().destroyInstance(instID);
    			teleportPlayer(st.getPlayer(),EXIT_COORDS,0);
		}
		else if (event.equalsIgnoreCase("30969-2.htm"))
		{
			st.addExpAndSp(52518015, 5817677);
			st.unset("cond");
			st.exitQuest(false);
			st.playSound("ItemSound.quest_finish");
		}
		else if (event.equalsIgnoreCase("ok"))
		{
			InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(st.getPlayer());
			if (world == null)
				return "";		
			openDoor(st.getPlayer(), world.instanceId);
			htmltext = "32577-1.htm";
		}
		else if (event.equalsIgnoreCase("wrong"))
		{
			st.getPlayer().teleToLocation(TELE_IF_WRONG_CODE3[0], TELE_IF_WRONG_CODE3[1], TELE_IF_WRONG_CODE3[2], false);
			htmltext = "32577-2.htm";
		}
		else if (event.equalsIgnoreCase("empty"))
		{
			return null;
		}
		return htmltext;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player) 
	{
		if (event.equalsIgnoreCase("return"))
		{
			int[] cords = ENTER_COORDS;
			L2ZoneType zone = getZoneForCharacter(npc);
			if (zone != null)
			switch (zone.getId())
			{
				case 20500:
					cords = ENTER_COORDS;
					break;
				case 20501:
					cords = TELE_IF_DETECTED1;
					break;
				case 20502:
					cords = TELE_IF_DETECTED2;
					break;
				case 20503:
					cords = TELE_IF_DETECTED3;
					break;
			}
			InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
			if (world != null && world.templateId == INSTANCE_ID)
				ThreadPoolManager.getInstance().scheduleGeneral(new Teleport(player, cords, world.instanceId), 1000);
			return null;
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
    	String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(qn);
		if(st == null)
			return htmltext;
		
		final int cond = st.getInt("cond");
		final int npcId = npc.getNpcId();
		
		switch (st.getState())
		{
			case State.COMPLETED:
				htmltext = getNoQuestMsg(player);
				break;
			case State.CREATED:
		        	if (npcId == CLAUDIA_ATHEBALDT)
		        	{
		            		if (player.getLevel() >= 79)
		            		{
						final QuestState qs = st.getPlayer().getQuestState("Q194_SevenSignContractOfMammon");
						if (qs != null)
						{
							if (qs.isCompleted())
								htmltext = "31001-0.htm";
							else
								htmltext = "31001-0b.htm";
						}
		            		}
		            		else
		            		{
		            			htmltext = "31001-0a.htm";
		            			st.exitQuest(true);
		            		}
		        	}
		        	break;
			case State.STARTED:
		        switch (npcId)
		        {
		        	case CLAUDIA_ATHEBALDT:
		        		if (cond == 1)
		        			htmltext = "31001-5.htm";
		        		break;
		        	case JHON:
		        		switch (cond)
		        		{
		        			case 1:
		        				htmltext = "32576-0.htm";
		        				break;
		        			case 2:
		        				htmltext = "32576-2.htm";
		        				break;
		        		}
		        		break;
		        	case RAYMOND:
		        		switch (cond)
		        		{
		        			case 2:
		        				htmltext = "30289-0.htm";
		        				break;
		        			case 3:
		        				if (player.getInventory().getItemByItemId(EMPEROR_CONTRACT) != null)
		        					htmltext = "30289-8.htm";
		        				else
		        					htmltext = "30289-5.htm";
		        				break;
		        			case 4:
		        				htmltext = "30289-11.htm";
		        				break;
		        		}
		        		break;
		        	case IASON_HEINE:
		        		if (cond == 4)
		        			htmltext = "30969-0.htm";
		        		break;
		        	case LIGHT_OF_DAWN:
		    			if (player.isTransformed())
		    			{
		    				if (player.getTransformationId() == 113)
		    				{
		    					enterInstance(player);
		    					htmltext = "32575-2.htm";
		    				}
		    			}
		    			else
		    				htmltext = "32575-1.htm";
		        		break;
		        	case DARKNESS_OF_DAWN:
	        			int instID = st.getPlayer().getInstanceId();
		        		if (instID != 0)
			    			InstanceManager.getInstance().destroyInstance(instID);

		        		teleportPlayer(st.getPlayer(),EXIT_COORDS,0);
		    			htmltext = "32579-0.htm";
		        		break;
		        	case IDENTITY_CONFIRM_DEVICE:
		    			InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(st.getPlayer());
		    			openDoor(st.getPlayer(), world.instanceId);
		    			htmltext = "32578-0.htm";
		        		break;
		        	case PASSWORD_ENTRY_DEVICE:
		        		htmltext = "32577-0.htm";
		        		break;
		        	case 32581:
		    			htmltext = "32581-0.htm";
						if (npc.getSpawn().getLocx() == CORRECT_BOOK[1] && npc.getSpawn().getLocy() == CORRECT_BOOK[2])
						{
							if (st.getPlayer().getInventory().getItemByItemId(EMPEROR_CONTRACT) == null)
								st.giveItems(EMPEROR_CONTRACT, 1);
							htmltext = "32581-1.htm";
						}
		        		break;
		        }
		        break;
		}
		return htmltext;
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player,	boolean isPet) 
	{
		handleReturnMagic(player,npc);
		return super.onAggroRangeEnter(npc, player, isPet);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet) 
	{
		handleReturnMagic(attacker,npc);
		return super.onAttack(npc, attacker, damage, isPet);
	}

	private void handleReturnMagic(L2PcInstance player, L2Npc npc)
	{
		if (!npc.isCastingNow())
		{
			switch (npc.getNpcId())
			{
				case 18834:
					npc.broadcastNpcSay(TEXT[0]);
					break;
				case 18835:
					npc.broadcastNpcSay(TEXT[1]);
					break;
				case 27351:
					npc.broadcastNpcSay(TEXT[2]);
					break;
			}
			
			npc.broadcastPacket(new MagicSkillUse(npc, player, 5978, 1, 2400, 0));
			startQuestTimer("return", 2000, npc, player);
			ThreadPoolManager.getInstance().scheduleGeneral(new returnTask(npc), 5000);
		}
	}

	private synchronized void enterInstance(L2PcInstance player)
	{
		// Check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if (world != null)
		{
			if (world.templateId != INSTANCE_ID)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return;
			}
			
			Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			if (inst != null)
			{
				teleportPlayer(player, ENTER_COORDS, world.instanceId);
				if (player != null)
					ThreadPoolManager.getInstance().scheduleGeneral(new handleNpcAiAndMoveTask(player), 20000);
			}
			return;
		}
		else
		{
			final int instanceId = InstanceManager.getInstance().createDynamicInstance("SecretRitualOfThePriests.xml");
			world = new InstanceWorld();
			world.instanceId = instanceId;
			world.templateId = INSTANCE_ID;
			InstanceManager.getInstance().addWorld(world);
			_log.info("SecretRitualOfThePriests: started instance: " + instanceId + " created by player: " + player.getName());
			world.allowed.add(player.getObjectId());
			teleportPlayer(player, ENTER_COORDS, instanceId);
			spawnMovingGuards(world.instanceId);
			ThreadPoolManager.getInstance().scheduleGeneral(new handleNpcAiAndMoveTask(player), 20000);
		}
	}

	private static final void teleportPlayer(L2PcInstance player, int[] coords, int instanceId)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
        	player.setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2], false);
	}

	private synchronized void openDoor(L2PcInstance player, int instanceId)
	{
		final ArrayList<L2DoorInstance> doors = InstanceManager.getInstance().getInstance(instanceId).getDoors();
		for (L2DoorInstance door : doors)
		{
			switch (door.getDoorId())
			{
				case START_DOORS:
					Collection<L2PcInstance> knows = door.getKnownList().getKnownPlayersInRadius(500);
					for (L2PcInstance pc : knows)
					{
						if (pc == player && !door.getOpen())
						{
							player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.USING_INVISIBLE_SKILL_SNEAK_IN));
							player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.MALE_GUARDS_CAN_DETECT_FEMALE_CANT));
							player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FEMALE_GUARDS_NOTICE_FROM_FAR_AWAY_BEWARE));
							door.openMe();
							
							// Set npc to invul here, when player is starting
							for (L2Npc npc : InstanceManager.getInstance().getInstance(instanceId).getNpcs())
							{
								if (Util.contains(MONSTERS, npc.getNpcId()))
								{
									((L2MonsterInstance) npc).setCanAgroWhileMoving();
									npc.setIsInvul(true);
								}
							}
						}
					}
					break;
				case MIDDLE_DOORS:
					knows = door.getKnownList().getKnownPlayersInRadius(500);
					for (L2PcInstance pc : knows)
					{
						if (pc == player && !door.getOpen())
						{
							player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DOOR_IS_ENTRANCE_APPROACH_DEVICE));
							door.openMe();
							player.showQuestMovie(ExStartScenePlayer.SSQ_RITUAL_OF_PRIEST);
						}
					}
					break;
				case FINAL_DOORS:
					knows = door.getKnownList().getKnownPlayersInRadius(500);
					for (L2PcInstance pc : knows)
					{
						if (pc == player && !door.getOpen())
							door.openMe();
					}
					break;
			}
		}
	}

	private void spawnMovingGuards(int instanceId)
	{
		for (int i = 0; i < MOVING_GUARDS.length; i++)
		{
			ThreadPoolManager.getInstance().scheduleAi(
					new movmentTask(
							addSpawn(MOVING_GUARDS[i][0], MOVING_GUARDS[i][1], MOVING_GUARDS[i][2], MOVING_GUARDS[i][3], 0, false, 0, false, instanceId),
							MOVING_GUARDS[i][1],
							MOVING_GUARDS[i][2],
							MOVING_GUARDS[i][3],
							MOVING_GUARDS[i][4],
							MOVING_GUARDS[i][5],
							MOVING_GUARDS[i][6],
							getMoveDelay(MOVING_GUARDS[i])
					),
			Rnd.get(1000, 5000)
			);
		}
	}

	private int getMoveDelay(int[] array)
	{
		return (int) ((Util.calculateDistance(array[1], array[2], array[4], array[5])/50)*1000)+200;
	}

	private class movmentTask implements Runnable
	{
		private L2Npc _npc;
		private int _x1;
		private int _y1;
		private int _z1;
		private int _x2;
		private int _y2;
		private int _z2;
		private int _moveDelay;
		
		private movmentTask(L2Npc npc, int x1, int y1, int z1, int x2, int y2, int z2, int moveDelay)
		{
			_npc = npc;
			_x1 = x1;
			_y1 = y1;
			_z1 = z1;
			_x2 = x2;
			_y2 = y2;
			_z2 = z2;
			_moveDelay = moveDelay;
		}
		
		@Override
		public void run()
		{
			if (_npc == null)
				return;

			if (_npc.getPosition().getX() == _x1 && _npc.getPosition().getY() == _y1)
				_npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(_x2, _y2, _z2, 0));
			else if (_npc.getPosition().getX() == _x2 && _npc.getPosition().getY() == _y2)
				_npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(_x1, _y1, _z1, 0));
			else
				_npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(_x1, _y1, _z1, 0));
			
			MoveToLocation move = new MoveToLocation(_npc);
			_npc.broadcastPacket(move, 5000);
			ThreadPoolManager.getInstance().scheduleAi(new movmentTask(_npc, _x1, _y1, _z1, _x2, _y2, _z2, _moveDelay), _moveDelay);
		}
	}
	
	private class returnTask implements Runnable
	{
		private L2Npc _npc;

		private returnTask(L2Npc npc)
		{
			_npc = npc;
		}
		
		@Override
		public void run()
		{
			if (_npc == null)
				return;

			_npc.setWalking();
			_npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(_npc.getSpawn().getLocx(), _npc.getSpawn().getLocy(), _npc.getSpawn().getLocz(), _npc.getSpawn().getHeading()));
			MoveToLocation move = new MoveToLocation(_npc);
			_npc.broadcastPacket(move);
		}
	}

	private class handleNpcAiAndMoveTask implements Runnable
	{
		private L2PcInstance _player;

		private handleNpcAiAndMoveTask(L2PcInstance player)
		{
			_player = player;
		}
		
		@Override
		public void run()
		{
			if (_player != null)
			{
				L2ZoneType zone = getZoneForCharacter(_player);
				if (zone != null)
				{
					for (L2Character c : zone.getCharactersInsideArray())
					{
						if (c.getInstanceId() != _player.getInstanceId())
							continue;
						
						if (c instanceof L2MonsterInstance)
						{
							if (!c.isMoving())
								c.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
							else
							{
								MoveToLocation move = new MoveToLocation(c);
								Broadcast.toPlayersInInstance(move, _player.getInstanceId());
							}
						}
					}
					
					InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(_player);
					if (world != null && world.templateId == INSTANCE_ID)
						if (_player != null)
							ThreadPoolManager.getInstance().scheduleGeneral(new handleNpcAiAndMoveTask(_player), 10000);
				}
			}
			return;
		}
	}

	private static final class Teleport implements Runnable
	{
		private final L2PcInstance _player;
		private final int _instanceId;
		private final int[] _cords;

		public Teleport(L2PcInstance player, int[] cords, int id)
		{
			_player = player;
			_cords = cords;
			_instanceId = id;
		}

		public void run()
		{
			try
			{
				teleportPlayer(_player, _cords, _instanceId);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	private static L2ZoneType getZoneForCharacter(L2Character character)
	{
		L2ZoneType zone = ZoneManager.getInstance().getZoneById(ZONES[0]);
		for (int i : ZONES)
		{
			zone = ZoneManager.getInstance().getZoneById(i);
			if (zone != null && zone.isCharacterInZone(character))
				return zone;
		}
		return zone;
	}

	public static void main(String[] args)
	{
		new Q195_SevenSignSecretRitualOfThePriests(195, qn, "Seven Sign Secret Ritual Of The Priests");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Quest: Seven Sign Secret Ritual Of The Priests");
	}
}
