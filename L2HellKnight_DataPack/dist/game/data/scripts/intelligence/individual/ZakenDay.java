package intelligence.individual;

import java.util.Calendar;
import java.util.logging.Logger;

import javolution.util.FastList;

import l2.hellknight.Config;
import l2.hellknight.gameserver.GameTimeController;
import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.instancemanager.InstanceManager;
import l2.hellknight.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2.hellknight.gameserver.model.L2Party;
import l2.hellknight.gameserver.model.L2World;
import l2.hellknight.gameserver.model.actor.L2Attackable;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.entity.Instance;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.AbstractNpcInfo;
import l2.hellknight.gameserver.network.serverpackets.Scenkos;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;
import l2.hellknight.gameserver.util.Util;
import l2.hellknight.util.Rnd;

public class ZakenDay extends Quest
{
	private class ZakenDayWorld extends InstanceWorld
	{		

		public long startTime  = 0;
		public ZakenDayWorld()
		{
			InstanceManager.getInstance();
		}	
	}	
	
	private static final String qn = "ZakenDay";
	private static final Logger _log = Logger.getLogger(ZakenDay.class.getName());
	
	private FastList<Candle> _listCandles = new FastList<Candle>();
	
	private boolean _is60 = false;
	private boolean _is83 = false;
	
	private static final int INSTANCEID_60 = 133; // this is the client number
	private static final int INSTANCEID_83 = 135; // this is the client number
	private int INSTANCE_WORLD_ID;
	private static final int ZAKEN_60 = 29176;
	private static final int ZAKEN_83 = 29181;
	private static final int _PORT_CAS = 5;
	private static final int _HODINY = 6;
	private static final int _MINUTY = 30;
	private static final int _DEN_A = 2;
	private static final int _DEN_B = 4;
	private static final int _DEN_C = 6;
	
	private int ZAKEN_ROOM;
	private int _candleCount;
	
	// NPC 60
	private static final int TELEPORTER = 32713;
	private static final int ZAKENS_CANDLE = 32705; 
	private static final int DOLL_BLADER_60 = 29023;
	private static final int VALE_MASTER_60 = 29024;
	private static final int PIRATES_ZOMBIE_CAPTAIN_60 = 29026;
	private static final int PIRATES_ZOMBIE_60 = 29027;
	
	// NPC 83
	private static final int DOLL_BLADER_83 = 29182;
	private static final int VALE_MASTER_83 = 29183;
	private static final int PIRATES_ZOMBIE_CAPTAIN_83 = 29184;
	private static final int PIRATES_ZOMBIE_83 = 29185;
	
	private static final int[][] ROOM_SPAWN =
	{
		// Floor 1
		{54240, 220133, -3498, 1, 3, 4, 6},
		{54240, 218073, -3498, 2, 5, 4, 7},
		{55265, 219095, -3498, 4, 9, 6, 7},
		{56289, 220133, -3498, 8, 11, 6, 9},
		{56289, 218073, -3498, 10, 12, 7, 9},
		
				
		// Floor 2
		{54240, 220133, -3226, 13, 15, 16, 18},
		{54240, 218073, -3226, 14, 17, 16, 19},
		{55265, 219095, -3226, 21, 16, 19, 18},
		{56289, 220133, -3226, 20, 23, 21, 18},
		{56289, 218073, -3226, 22, 24, 19, 21},
		
		// Floor 3
		{54240, 220133, -2954, 25, 27, 28, 30},
		{54240, 218073, -2954, 26, 29, 28, 31},
		{55265, 219095, -2954, 33, 28, 31, 30},
		{56289, 220133, -2954, 32, 35, 30, 33},
		{56289, 218073, -2954, 34, 36, 31, 33}
	};
	
	private static final int[][] CANDLE_SPAWN =
	{
		// Floor 1
		{53313, 220133, -3498},
		{53313, 218079, -3498},
		{54240, 221045, -3498},
		{54325, 219095, -3498},
		{54240, 217155, -3498},
		{55257, 220028, -3498},
		{55257, 218172, -3498},
		{56280, 221045, -3498},
		{56195, 219095, -3498},
		{56280, 217155, -3498},
		{57215, 220133, -3498},
		{57215, 218079, -3498},
		
		// Floor 2
		{53313, 220133, -3226},
		{53313, 218079, -3226},
		{54240, 221045, -3226},
		{54325, 219095, -3226},
		{54240, 217155, -3226},
		{55257, 220028, -3226},
		{55257, 218172, -3226},
		{56280, 221045, -3226},
		{56195, 219095, -3226},
		{56280, 217155, -3226},
		{57215, 220133, -3226},
		{57215, 218079, -3226},
		   
		// Floor 3
		{53313, 220133, -2954},
		{53313, 218079, -2954},
		{54240, 221045, -2954},
		{54325, 219095, -2954},
		{54240, 217155, -2954},
		{55257, 220028, -2954},
		{55257, 218172, -2954},
		{56280, 221045, -2954},
		{56195, 219095, -2954},
		{56280, 217155, -2954},
		{57215, 220133, -2954},
		{57215, 218079, -2954},
	};
	
	private class Candle
	{
		int id;
		L2Npc npcCandle;
		boolean trueCandle;
		boolean fire;
	}

	public ZakenDay(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(TELEPORTER);
		addTalkId(TELEPORTER);
		addFirstTalkId(ZAKENS_CANDLE);
		addKillId(ZAKEN_60);
		addKillId(ZAKEN_83);
	}
	
	private void CheckCandle(L2PcInstance player, L2Npc can)
	{		
		for (Candle candlex : _listCandles)
		
			if (candlex.npcCandle == can)
			{
				if(candlex.fire)
					return;
				candlex.fire = true;
				if(candlex.trueCandle)
				{
					_candleCount += 1;
					startQuestTimer("burn_good_candle", 500, candlex.npcCandle, player);
					return;
				}
				startQuestTimer("burn_bad_candle", 500, candlex.npcCandle, player);
				return;				
			}		
		_log.info("Zaken: No candle");
		return ;
	}	
	
	private void teleportPlayer(L2PcInstance player, ZakenDayWorld world)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(world.instanceId);
		player.teleToLocation(52680, 219088, -3232);
		if(player.getPet() != null)
		{
			player.getPet().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			player.getPet().setInstanceId(world.instanceId);
			player.getPet().teleToLocation(52680, 219088, -3232);
		}			
		return;
	}
	
	private void enterInstance(L2PcInstance player, String template)
	{		
		int instanceId = 0;
		_candleCount = 0;
		//check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		//existing instance
		if (world != null)
		{
			if (!(world instanceof ZakenDayWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return;
			}
			teleportPlayer(player,(ZakenDayWorld)world);
			return;
		}
		//New instance
		else
		{
			if (!checkConditions(player))
				return;
			L2Party party = player.getParty();
			instanceId = InstanceManager.getInstance().createDynamicInstance(template);
			world = new ZakenDayWorld();
			if (_is60)
			{
				world.instanceId = instanceId;
				world.templateId = INSTANCEID_60;
				world.status = 0;
				
				INSTANCE_WORLD_ID = instanceId;
				
				InstanceManager.getInstance().addWorld(world);
				((ZakenDayWorld)world).startTime = System.currentTimeMillis();
				_log.info("Zaken Day started " + template + " Instance: " + instanceId + " created by player: " + player.getName());
	
				 if (party.getCommandChannel() == null)
				 {
					 for (L2PcInstance partyMember : party.getPartyMembers())
					 {
						 world.allowed.add(partyMember.getObjectId());
						 teleportPlayer(partyMember,(ZakenDayWorld)world);
						 //partyMember.sendPacket(new ExSendUIEvent(player, false, true, 0, 3600, ":"));
						 //TODO: Need chceck
					 }
				 }
				 else
				 {
					 for (L2PcInstance channelMember : party.getCommandChannel().getMembers())
					 {
						 world.allowed.add(channelMember.getObjectId());
						 teleportPlayer(channelMember,(ZakenDayWorld)world);
						 //channelMember.sendPacket(new ExSendUIEvent(player, false, true, 0, 3600, ":"));
						 //TODO: Need chceck
					 }
				 }
			}
			else if (_is83)
			{
				world.instanceId = instanceId;
				world.templateId = INSTANCEID_83;
				world.status = 0;
				
				INSTANCE_WORLD_ID = instanceId;
				
				InstanceManager.getInstance().addWorld(world);
				_log.info("Zaken Day started " + template + " Instance: " + instanceId + " created by player: " + player.getName());
	
				 if (party.getCommandChannel() == null)
				 {
					 for (L2PcInstance partyMember : party.getPartyMembers())
					 {
						 world.allowed.add(partyMember.getObjectId());
						 teleportPlayer(partyMember,(ZakenDayWorld)world);
						 //partyMember.sendPacket(new ExSendUIEvent(player, false, true, 0, 3600, ":"));
						 //TODO: Need chceck
					 }
				 }
				 else
				 {
					 for (L2PcInstance channelMember : party.getCommandChannel().getMembers())
					 {
						 world.allowed.add(channelMember.getObjectId());
						 teleportPlayer(channelMember,(ZakenDayWorld)world);
						 //channelMember.sendPacket(new ExSendUIEvent(player, false, true, 0, 3600, ":"));
						 //TODO: Need chceck
					 }
				 }
			}
			 spawnCandles();
			return;
		}
	}	
	
    private boolean checkConditions(L2PcInstance player)
    {
    	if (_is60)
    	{
	    	if (getTimeHour() < 4 || getTimeHour() > 24)
	    	{
	    		player.sendPacket(SystemMessage.getSystemMessage(1940));
	    		return false;
	    	}
	        if (player.getParty() == null)
	        {
	                player.sendPacket(SystemMessage.getSystemMessage(2101));
	                return false;
	        }
	        if (player.getParty().getCommandChannel() == null)
	        {
	        	if (player.getParty().getLeader() != player)
	            {
	                    player.sendPacket(SystemMessage.getSystemMessage(2185));
	                    return false;
	            }
	        	if (player.getParty().getMemberCount() <  Config.MIN_DAYTIME_ZAKEN_PLAYERS)
	        	{
	        		player.sendPacket(SystemMessage.getSystemMessage(2102));
	                return false;
	        	}
	            for (L2PcInstance partyMember : player.getParty().getPartyMembers())
	            {
	                if (partyMember.getLevel() > 68 || partyMember.getLevel() < 52)
	                {
	                        SystemMessage sm = SystemMessage.getSystemMessage(2097);
	                        sm.addPcName(partyMember);
	                        player.getParty().broadcastToPartyMembers(sm);
	                        return false;
	                }
	                if (!Util.checkIfInRange(1000, player, partyMember, true))
	                {
	                        SystemMessage sm = SystemMessage.getSystemMessage(2096);
	                        sm.addPcName(partyMember);
	                        player.getParty().broadcastToPartyMembers(sm);
	                        return false;
	                }
	                Long reentertime = InstanceManager.getInstance().getInstanceTime(partyMember.getObjectId(), INSTANCEID_60);
	                if (System.currentTimeMillis() < reentertime)
	                {
	                        SystemMessage sm = SystemMessage.getSystemMessage(2100);
	                        sm.addPcName(partyMember);
	                        player.getParty().broadcastToPartyMembers(sm);
	                        return false;
	                }
	            }         
	        }
	        else
	        {
	            if (player.getParty().getCommandChannel().getChannelLeader() != player)
	            {
	                    player.sendPacket(SystemMessage.getSystemMessage(2185));
	                    return false;
	            }
	            else if (player.getParty().getCommandChannel().getMemberCount() < 9 || player.getParty().getCommandChannel().getMemberCount() > 27)
	            {
	                    player.sendPacket(SystemMessage.getSystemMessage(2102));
	                    return false;
	            }
	            
	            for (L2PcInstance channelMember : player.getParty().getCommandChannel().getMembers())
	            {
	                if (channelMember.getLevel() > 72)
	                {
	                        SystemMessage sm = SystemMessage.getSystemMessage(2097);
	                        sm.addPcName(channelMember);
	                        player.getParty().getCommandChannel().broadcastToChannelMembers(sm);
	                        return false;
	                }
	                if (!Util.checkIfInRange(1000, player, channelMember, true))
	                {
	                        SystemMessage sm = SystemMessage.getSystemMessage(2096);
	                        sm.addPcName(channelMember);
	                        player.getParty().getCommandChannel().broadcastToChannelMembers(sm);
	                        return false;
	                }
	                Long reentertime = InstanceManager.getInstance().getInstanceTime(channelMember.getObjectId(), INSTANCEID_60);
	                if (System.currentTimeMillis() < reentertime)
	                {
	                        SystemMessage sm = SystemMessage.getSystemMessage(2100);
	                        sm.addPcName(channelMember);
	                        player.getParty().getCommandChannel().broadcastToChannelMembers(sm);
	                        return false;
	                }
	            }
	        }
    	}
    	else if (_is83)
    	{
        	if (getTimeHour() < 4 || getTimeHour() > 24)
        	{
        		player.sendPacket(SystemMessage.getSystemMessage(835));
        		return false;
        	}
            if (player.getParty() == null)
            {
                    player.sendPacket(SystemMessage.getSystemMessage(2101));
                    return false;
            }
            if (player.getParty().getCommandChannel() == null)
            {
            	if (player.getParty().getLeader() != player)
                {
                        player.sendPacket(SystemMessage.getSystemMessage(2185));
                        return false;
                }
            	if (player.getParty().getMemberCount() <  Config.MIN_TOP_DAYTIME_ZAKEN_PLAYERS)
            	{
            		player.sendPacket(SystemMessage.getSystemMessage(2102));
                    return false;
            	}
                for (L2PcInstance partyMember : player.getParty().getPartyMembers())
                {
                    if (partyMember.getLevel() < 78)
                    {
                            SystemMessage sm = SystemMessage.getSystemMessage(2097);
                            sm.addPcName(partyMember);
                            player.getParty().broadcastToPartyMembers(sm);
                            return false;
                    }
                    if (!Util.checkIfInRange(1000, player, partyMember, true))
                    {
                            SystemMessage sm = SystemMessage.getSystemMessage(2096);
                            sm.addPcName(partyMember);
                            player.getParty().broadcastToPartyMembers(sm);
                            return false;
                    }
                    Long reentertime = InstanceManager.getInstance().getInstanceTime(partyMember.getObjectId(), INSTANCEID_83);
                    if (System.currentTimeMillis() < reentertime)
                    {
                            SystemMessage sm = SystemMessage.getSystemMessage(2100);
                            sm.addPcName(partyMember);
                            player.getParty().broadcastToPartyMembers(sm);
                            return false;
                    }
                }         
            }
            else
            {
                if (player.getParty().getCommandChannel().getChannelLeader() != player)
                {
                        player.sendPacket(SystemMessage.getSystemMessage(2185));
                        return false;
                }
                else if (player.getParty().getCommandChannel().getMemberCount() < 9 || player.getParty().getCommandChannel().getMemberCount() > 27)
                {
                        player.sendPacket(SystemMessage.getSystemMessage(2102));
                        return false;
                }
                
                for (L2PcInstance channelMember : player.getParty().getCommandChannel().getMembers())
                {
                    if (channelMember.getLevel() < 78)
                    {
                            SystemMessage sm = SystemMessage.getSystemMessage(2097);
                            sm.addPcName(channelMember);
                            player.getParty().getCommandChannel().broadcastToChannelMembers(sm);
                            return false;
                    }
                    if (!Util.checkIfInRange(1000, player, channelMember, true))
                    {
                            SystemMessage sm = SystemMessage.getSystemMessage(2096);
                            sm.addPcName(channelMember);
                            player.getParty().getCommandChannel().broadcastToChannelMembers(sm);
                            return false;
                    }
                    Long reentertime = InstanceManager.getInstance().getInstanceTime(channelMember.getObjectId(), INSTANCEID_83);
                    if (System.currentTimeMillis() < reentertime)
                    {
                            SystemMessage sm = SystemMessage.getSystemMessage(2100);
                            sm.addPcName(channelMember);
                            player.getParty().getCommandChannel().broadcastToChannelMembers(sm);
                            return false;
                    }
                }
            }
    	}
	         return true;
    }
	
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.startsWith("burn_good_candle"))
		{
			if (npc.getRightHandItem() == 0)
			{
				npc.setRHandId(15280);
				//npc.broadcastPacket(new AbstractNpcInfo.NpcInfo(npc, player));
				Scenkos.toPlayersInInstance(new AbstractNpcInfo.NpcInfo(npc, player), player.getInstanceId());
				startQuestTimer("burn_blue_candle", 10000, npc, player);
				if (_candleCount == 4)
				{
					startQuestTimer("spawn_zaken", 20000, npc, player);
					_candleCount = 0;
				}
			}
		}
		else if (event.startsWith("burn_bad_candle"))
		{
			if (npc.getRightHandItem() == 0)
			{
				npc.setRHandId(15280);
				//npc.broadcastPacket(new AbstractNpcInfo.NpcInfo(npc, player));
				Scenkos.toPlayersInInstance(new AbstractNpcInfo.NpcInfo(npc, player), player.getInstanceId());
				startQuestTimer("burn_red_candle", 10000, npc, player);
			}
		}
		else if (event.startsWith("burn_red_candle"))
		{
			if (npc.getRightHandItem() == 15280)
			{
				npc.setRHandId(15281);
				//npc.broadcastPacket(new AbstractNpcInfo.NpcInfo(npc, player));
				Scenkos.toPlayersInInstance(new AbstractNpcInfo.NpcInfo(npc, player), player.getInstanceId());
				int room = getRoomByCandle(npc); 
				if (_is60)
				{
					spawnInRoom(DOLL_BLADER_60, room, player);
					spawnInRoom(VALE_MASTER_60, room, player);
					spawnInRoom(PIRATES_ZOMBIE_60, room, player);
					spawnInRoom(PIRATES_ZOMBIE_CAPTAIN_60, room, player);
				}
				else if (_is83)
				{
					spawnInRoom(DOLL_BLADER_83, room, player);
					spawnInRoom(VALE_MASTER_83, room, player);
					spawnInRoom(PIRATES_ZOMBIE_83, room, player);
					spawnInRoom(PIRATES_ZOMBIE_CAPTAIN_83, room, player);
				}
			}
		}
		else if (event.startsWith("burn_blue_candle"))
		{
			if (npc.getRightHandItem() == 15280)
			{
				npc.setRHandId(15302);
				//npc.broadcastPacket(new AbstractNpcInfo.NpcInfo(npc, player));
				Scenkos.toPlayersInInstance(new AbstractNpcInfo.NpcInfo(npc, player), player.getInstanceId());
			}
		}
		else if (event.equalsIgnoreCase("spawn_zaken"))
		{
			if (_is60)
			{
				spawnInRoom(ZAKEN_60, ZAKEN_ROOM, player);
				spawnInRoom(DOLL_BLADER_60, ZAKEN_ROOM, player);
				spawnInRoom(PIRATES_ZOMBIE_60, ZAKEN_ROOM, player);
				spawnInRoom(PIRATES_ZOMBIE_CAPTAIN_60, ZAKEN_ROOM, player);
				spawnInRoom(VALE_MASTER_60, ZAKEN_ROOM, player);
			}
			else if (_is83)
			{
				spawnInRoom(ZAKEN_83, ZAKEN_ROOM, player);
				spawnInRoom(DOLL_BLADER_83, ZAKEN_ROOM, player);
				spawnInRoom(PIRATES_ZOMBIE_83, ZAKEN_ROOM, player);
				spawnInRoom(PIRATES_ZOMBIE_CAPTAIN_83, ZAKEN_ROOM, player);
				spawnInRoom(VALE_MASTER_83, ZAKEN_ROOM, player);
			}
		}
		else if (event.equalsIgnoreCase("60"))
		{
			_is60 = true;
			_is83 = false;
			enterInstance(player, "ZakenDay.xml");
			return "";
		}
		else if (event.equalsIgnoreCase("83"))
		{
			_is60 = false;
			_is83 = true;
			enterInstance(player, "ZakenDay83.xml");
			return "";
		}
		return "";
	}

	
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		InstanceWorld tmpWorld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpWorld instanceof ZakenDayWorld)
		{
			ZakenDayWorld world = (ZakenDayWorld)tmpWorld;
			long finishDiff = System.currentTimeMillis() - world.startTime;
			int npcId = npc.getNpcId();
			if (npcId == ZAKEN_60)
			{
				//Scenkos.toPlayersInInstance(new ExSendUIEvent(killer, true, true, 0, 0, ":"), killer.getInstanceId());
				 //TODO: Need chceck
				finishInstance(world);
				despawnCandles();
				_candleCount = 0;
			}
			else if (npcId == ZAKEN_83)
			{
				//Scenkos.toPlayersInInstance(new ExSendUIEvent(killer, true, true, 0, 0, ":"), killer.getInstanceId());
				 //TODO: Need chceck
				finishInstance(world);
				despawnCandles();
				_candleCount = 0;
			}
			
			if (finishDiff <= 900000)
			{
				if (npc.getNpcId() == ZAKEN_83 && killer.isInParty() && killer.getParty().getCommandChannel() != null)
				{
					for(L2PcInstance player : killer.getParty().getCommandChannel().getMembers())
					{
						timebonus(world, npc, player);
					}
				}
				else if (npc.getNpcId() == ZAKEN_83 && killer.isInParty())
				{
					for(L2PcInstance player : killer.getParty().getPartyMembers())
					{
						timebonus(world, npc, player);
					}
				}
				else if (npc.getNpcId() == ZAKEN_83 && !killer.isInParty())
				{
					timebonus(world, npc, killer);
				}
			}
		}
		return null;
	}
	
	@Override
	public String onTalk (L2Npc npc, L2PcInstance player)
	{
		int npcId = npc.getNpcId();
		QuestState st = player.getQuestState(qn);
		if (st == null)
			st = newQuestState(player);
		if (npcId == TELEPORTER)
		{
			enterInstance(player, "ZakenDay.xml");
		}
		return "";
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (npc.getNpcId() == ZAKENS_CANDLE)
		{
			CheckCandle(player, npc);
		}
		return "";
	}
	
	private void timebonus(ZakenDayWorld world, L2Npc npc, L2PcInstance player)
	{
		long finishDiff = System.currentTimeMillis() - world.startTime;
		if (player.isInsideRadius(npc, 2000, false, false))
		{
			int rand = Rnd.get(100);
			if( finishDiff <= 300000 )
			{
				if( rand < 50 )
				{
					player.addItem("Zaken", 15763, 1, npc, true); // Sealed Vorpal Ring
				}
			}
			else if( finishDiff <= 600000 )
			{
				if( rand < 30 )
				{
					player.addItem("Zaken", 15764, 1, npc, true); // Sealed Vorpal Earring
				}
			}
			else if( finishDiff <= 900000 )
			{
				if( rand < 25 )
				{
					player.addItem("Zaken", 15763, 1, npc, true); // Sealed Vorpal Ring
				}
			}
		}
	}
	
	private void spawnCandles()
	{
		for (int i = 0; i < 36; i++)
		{
			Candle candlex = new Candle();
			candlex.id = i+1;
			candlex.npcCandle = addSpawn(ZAKENS_CANDLE, CANDLE_SPAWN[i][0], CANDLE_SPAWN[i][1], CANDLE_SPAWN[i][2], 0, false,0,false,INSTANCE_WORLD_ID);
			candlex.trueCandle = false;
			//_log.info("Candle " + candlex.id + " " + candlex.trueCandle);
			_listCandles.add(candlex);
		}
		
		// Generate random room
		ZAKEN_ROOM = Rnd.get(1,15);
		_listCandles.get(ROOM_SPAWN[ZAKEN_ROOM-1][3]-1).trueCandle = true;
		_listCandles.get(ROOM_SPAWN[ZAKEN_ROOM-1][4]-1).trueCandle = true;
		_listCandles.get(ROOM_SPAWN[ZAKEN_ROOM-1][5]-1).trueCandle = true;
		_listCandles.get(ROOM_SPAWN[ZAKEN_ROOM-1][6]-1).trueCandle = true;
	}
	
	private void despawnCandles()
	{
		for (int i = 0; i < _listCandles.size(); i++)
		{
			_listCandles.get(i).npcCandle.decayMe();
		}
		_listCandles.clear();
	}
	
	private int getRoomByCandleId(int candleId)
	{
		for (int i = 0; i < 15; i++)
		{
			if (ROOM_SPAWN[i][3] == candleId || ROOM_SPAWN[i][4] == candleId)
			{
				return i+1;
			}
		}
		if (candleId == 6 || candleId == 7)
			return 3;
		if (candleId == 18 || candleId == 19)
			return 8;
		if (candleId == 30 || candleId == 31)
			return 13;
		return 0;
	}
	
	private int getRoomByCandle(L2Npc candle)
	{
		for (Candle candlex: _listCandles)
		{
			if (candlex.npcCandle == candle)
			{
				return getRoomByCandleId(candlex.id);
			}
		}
		return 0;
	}
	
	private void spawnInRoom(int npcId, int roomId, L2PcInstance player)
	{
		if (player != null && npcId != ZAKEN_60 && npcId != ZAKEN_83)
		{
			L2Npc mob = addSpawn(npcId, ROOM_SPAWN[roomId-1][0] + Rnd.get(350), ROOM_SPAWN[roomId-1][1] + Rnd.get(350), ROOM_SPAWN[roomId-1][2], 0, false, 0, false, INSTANCE_WORLD_ID);
			mob.setRunning();
			mob.setTarget(player);
			((L2Attackable) mob).addDamageHate(player, 0, 999);
			mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
		}
		else
		{
			addSpawn(npcId, ROOM_SPAWN[roomId-1][0], ROOM_SPAWN[roomId-1][1], ROOM_SPAWN[roomId-1][2], 0, false, 0, false, INSTANCE_WORLD_ID);
		}
	}

	private void finishInstance(InstanceWorld world)
	{
		Calendar reenter = Calendar.getInstance();
		reenter.set(Calendar.MINUTE, _MINUTY);
		reenter.set(Calendar.HOUR_OF_DAY, _HODINY);
		// if time is >= _HODINY - roll to the next day
		if (reenter.getTimeInMillis() <= System.currentTimeMillis())
			reenter.add(Calendar.DAY_OF_MONTH, 1);
		if (reenter.get(Calendar.DAY_OF_WEEK) <= _DEN_A || reenter.get(Calendar.DAY_OF_WEEK) > _DEN_C)
			while(reenter.get(Calendar.DAY_OF_WEEK) != _DEN_A)
				reenter.add(Calendar.DAY_OF_MONTH, 1);
		else if (reenter.get(Calendar.DAY_OF_WEEK) <= _DEN_B)
			while(reenter.get(Calendar.DAY_OF_WEEK) != _DEN_B)
				reenter.add(Calendar.DAY_OF_MONTH, 1);
		else
			while(reenter.get(Calendar.DAY_OF_WEEK) != _DEN_C)
				reenter.add(Calendar.DAY_OF_MONTH, 1);

		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.INSTANT_ZONE_S1_RESTRICTED);
		sm.addString(InstanceManager.getInstance().getInstanceIdName(world.templateId));

		// set instance reenter time for all allowed players
		for (int objectId : world.allowed)
		{
			L2PcInstance player = L2World.getInstance().getPlayer(objectId);
			InstanceManager.getInstance().setInstanceTime(objectId, world.templateId, reenter.getTimeInMillis());
			if (player != null && player.isOnline())
				player.sendPacket(sm);
		}
		 // destroy instance after EXIT_TIME
		Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
		inst.setDuration(_PORT_CAS * 60000);
		inst.setEmptyDestroyTime(0);
	}
	
	private int getTimeHour()
	{
		return (GameTimeController.getInstance().getGameTime() / 60) % 24;
	}
	
	public static void main(String[] args)
	{
		new ZakenDay(-1, qn, "ai");
	}
}