package zone_scripts.GraciaContinent.SeedOfInfinity.HallOfErosion;

import java.util.Calendar;
import java.util.List;


import javolution.util.FastList;
import l2.hellknight.Config;
import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.instancemanager.InstanceManager;
import l2.hellknight.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2.hellknight.gameserver.model.L2CommandChannel;
import l2.hellknight.gameserver.model.L2Effect;
import l2.hellknight.gameserver.model.L2Skill;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.L2Summon;
import l2.hellknight.gameserver.model.actor.instance.L2DoorInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;
import l2.hellknight.gameserver.util.Util;

public class HallOfErosion extends Quest
{
	private class HallOfErosionWorld extends InstanceWorld
	{
		public boolean ZoneWaitForTP                = true;
		public List<L2PcInstance> PlayersInInstance = new FastList<L2PcInstance>();
		
		public HallOfErosionWorld(Long time)
		{
			InstanceManager.getInstance();
		}
	}
	
	private static final String qn = "HallOfErosion";
	private static final int INSTANCEID = 119; // clientske id
	private static final int MIN_PLAYERS = 1; //min hracu
	private static final int MAX_PLAYERS = 24; //max hracu
	private static final boolean debug = true; //test mod zapnutej
	private static final int EXIT_TIME = 5; //exit time
	// teleport
	private static final int[] ENTER_TELEPORT = {-187567,205570,-9538};
	//npc a prisery
	private static final int MOUTHOFEKIMUS                 = 32526;
	private static final int[] TUMOR_MOBY 			= {22509,22510,22511,22512,22513,22514,22515}; //neni retail
	private static final int KAHAMENES 				= 25651;
	private static final int TUMOR_ZIVY 			= 18708;
	private static final int TUMOR_MRTVI 			= 18705;
	
	//spawny
	private static final int[][] MISTNOST_1 = {
		{22509, -175935, 209536, -11925},
		{22509, -175958, 209708, -11928},
		{22509, -175991, 209959, -11935},
		{22510, -175976, 210146, -11942},
		{22510, -175868, 210196, -11942}
	};
	
	private static final int[][] MISTNOST_2 = {
		{22509, -177905, 211426, -12025},
		{22509, -177899, 211665, -12024},
		{22509, -178071, 211885, -12034},
		{22510, -178293, 212000, -12041},
		{22510, -178513, 212021, -12034},
		{22510, -178755, 211907, -12024},
		{22510, -178907, 211744, -12016},
		{22510, -178911, 211521, -12015},
	};
	
	private static final int[][] MISTNOST_3 = {
		{22509, -179402, 210944, -12792},
		{22509, -179502, 210934, -12793},
		{22509, -179620, 210957, -12795},
		{22510, -179683, 211027, -12793},
		{22510, -179714, 211167, -12797},
		{22510, -179702, 211225, -12795}
	};
	
	private static final int[][] MISTNOST_4 = {
		{22509, -181546, 211262, -12022},
		{22509, -181539, 211564, -12024},
		{22509, -181422, 211834, -12033},
		{22510, -181212, 212055, -12046},
		{22510, -180986, 212154, -12042},
		{22510, -180766, 212109, -12034},
		{22510, -180587, 211974, -12027},
		{22510, -180446, 211657, -12015},
	};
	
	private static final int[][] MISTNOST_5 = {
		{22509, -183280, 209531, -11926},
		{22509, -183375, 209723, -11927},
		{22509, -183398, 209989, -11939},
		{22510, -183297, 210187, -11940},
		{22510, -183188, 210237, -11940},
	};
	
	private static final int[][] MISTNOST_6 = {
		{22509, -182948, 207774, -11930},
		{22509, -183146, 207927, -11942},
		{22509, -183300, 208107, -11945},
		{22510, -183275, 208327, -11948},
		{22510, -183221, 208426, -11938},
	};
	
	private static final int[][] MISTNOST_7 = {
		{22509, -180431, 206941, -12018},
		{22509, -180463, 206746, -12016},
		{22509, -180592, 206551, -12021},
		{22510, -180884, 206350, -12032},
		{22510, -181201, 206345, -12039},
		{22510, -181364, 206447, -12028},
		{22510, -181378, 206642, -12024},
		{22510, -181335, 206906, -12032},
	};
	
	private static final int[][] MISTNOST_8 = {
		{22509, -178087, 207199, -12018},
		{22509, -177995, 207029, -12016},
		{22509, -177903, 206635, -12021},
		{22510, -178112, 206201, -12032},
		{22510, -178424, 206011, -12039},
		{22510, -178700, 206142, -12028},
		{22510, -178791, 206382, -12024},
		{22510, -178824, 206766, -12032},
	};
	
	// Initialization at 6:30 am on Wednesday and Saturday
	private static final int RESET_HOUR  = 6;
	private static final int RESET_MIN   = 30;
	private static final int RESET_DAY_1 = 4;
	private static final int RESET_DAY_2 = 7;

	private class teleCoord {int instanceId; int x; int y; int z;}

	private boolean checkConditions(L2PcInstance player)
	{
		if (debug)
		{
			return true;
		}
		else
		{
			if (player.getParty() == null)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_IN_PARTY_CANT_ENTER));
				return false;
			}
			L2CommandChannel channel = player.getParty().getCommandChannel();
			if (channel == null)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_IN_COMMAND_CHANNEL_CANT_ENTER));
				return false;
			}
			else if (channel.getChannelLeader() != player)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER));
				return false;
			}
			else if (channel.getMemberCount() < MIN_PLAYERS || channel.getMemberCount() > MAX_PLAYERS)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER));
				return false;
			}
			for (L2PcInstance channelMember : channel.getMembers())
			{
				if (channelMember.getLevel() < Config.MIN_LEVEL_PLAYER_FOR_SOD)
				{
					SystemMessage sm = (SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT));
					sm.addPcName(channelMember);
					channel.broadcastToChannelMembers(sm);
					return false;
				}
				if (!Util.checkIfInRange(1000, player, channelMember, true))
				{
					SystemMessage sm = (SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED));
					sm.addPcName(channelMember);
					channel.broadcastToChannelMembers(sm);
					return false;
				}
				Long reentertime = InstanceManager.getInstance().getInstanceTime(channelMember.getObjectId(), INSTANCEID);
				if (System.currentTimeMillis() < reentertime)
				{
					SystemMessage sm = (SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET));
					sm.addPcName(channelMember);
					channel.broadcastToChannelMembers(sm);
					return false;
				}
			}
			return true;
		}
	}
	//TODO: Error
	private int checkworld(L2PcInstance player)
	{
		InstanceWorld checkworld = InstanceManager.getInstance().getPlayerWorld(player);
		if (checkworld != null)
		{
			if (!(checkworld instanceof HallOfErosionWorld))
			{
				return 0;
			}
			return 1;
		}
		return 2;
	}
	
	
	//TODO: Error 
	protected int enterInstance(L2PcInstance player, String template, teleCoord teleto)
	{
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		int inst = checkworld(player);
		if (inst == 0)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
			return 0;
		}
		else if (inst == 1)
		{
			teleto.instanceId = world.instanceId;
			teleportplayer(player,teleto,(HallOfErosionWorld)world);
			return world.instanceId;
		}
		//New instance
		else
		{
			if (!checkConditions(player))
				return 0;
			int instanceId = InstanceManager.getInstance().createDynamicInstance(template);
			world = new HallOfErosionWorld(System.currentTimeMillis() + 5400000);
			world.instanceId = instanceId;
			world.templateId = INSTANCEID;
			world.status = 0;
			InstanceManager.getInstance().addWorld(world);
			teleto.instanceId = instanceId;
			if (debug)
			{
				player.sendMessage("Welcome to Seed of Destruction. Time to finish the resort is 130 minutes.");
				InstanceManager.getInstance().setInstanceTime(player.getObjectId(), INSTANCEID, (System.currentTimeMillis()));
				teleportplayer(player,teleto,(HallOfErosionWorld)world);
				removeBuffs(player);
				world.allowed.add(player.getObjectId());
			}
			else
			{
				for (L2PcInstance channelMember : player.getParty().getCommandChannel().getMembers())
				{
					player.sendMessage("Welcome to Seed of Destruction. Time to finish the resort is 130 minutes.");
					InstanceManager.getInstance().setInstanceTime(channelMember.getObjectId(), INSTANCEID, (System.currentTimeMillis()));
					teleportplayer(channelMember,teleto,(HallOfErosionWorld)world);
					removeBuffs(channelMember);
					world.allowed.add(channelMember.getObjectId());
				}
			}
			return instanceId;
		}
	}
	
	@Override
	public String onTalk (L2Npc npc, L2PcInstance player)
	{
		int npcId = npc.getNpcId();
		QuestState st = player.getQuestState(qn);
		if (st == null)
			st = newQuestState(player);
		if (npcId == MOUTHOFEKIMUS)
		{
			//enterInstance(player, "HallOfErosion.xml", ENTER_TELEPORT); TODO:
			return "";
		}
		return "";
	}
	
	private static final void removeBuffs(L2Character ch)
	{
		for (L2Effect e : ch.getAllEffects())
		{
			if (e == null)
				continue;
			L2Skill skill = e.getSkill();
			if (skill.isDebuff() || skill.isStayAfterDeath())
				continue;
			e.exit();
		}
		if (ch.getPet() != null)
		{
			for (L2Effect e : ch.getPet().getAllEffects())
			{
				if (e == null)
					continue;
				L2Skill skill = e.getSkill();
				if (skill.isDebuff() || skill.isStayAfterDeath())
					continue;
				e.exit();
			}
		}
	}
	
	private void teleportplayer(L2PcInstance player, teleCoord teleto, HallOfErosionWorld world )
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(teleto.instanceId);
		player.teleToLocation(teleto.x, teleto.y, teleto.z);
		L2Summon pet = player.getPet();
		if (pet != null)
		{
			pet.setInstanceId(teleto.instanceId);
			pet.teleToLocation(teleto.x, teleto.y, teleto.z);
		}
		world.PlayersInInstance.add(player);
	}
	
	protected void setInstanceTimeRestrictions(HallOfErosionWorld world)
	{
		Calendar reenter = Calendar.getInstance();
		reenter.set(Calendar.MINUTE, RESET_MIN);
		reenter.set(Calendar.HOUR_OF_DAY, RESET_HOUR);
		if (reenter.getTimeInMillis() <= System.currentTimeMillis())
			reenter.add(Calendar.DAY_OF_MONTH, 1);
		if (reenter.get(Calendar.DAY_OF_WEEK) <= RESET_DAY_1)
			while(reenter.get(Calendar.DAY_OF_WEEK) != RESET_DAY_1)
				reenter.add(Calendar.DAY_OF_MONTH, 1);
		else
			while(reenter.get(Calendar.DAY_OF_WEEK) != RESET_DAY_2)
				reenter.add(Calendar.DAY_OF_MONTH, 1);

		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.INSTANT_ZONE_RESTRICTED);
		sm.addString(InstanceManager.getInstance().getInstanceIdName(INSTANCEID));
	}
	//TODO:
	public HallOfErosion(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(MOUTHOFEKIMUS);
		addTalkId(MOUTHOFEKIMUS);
	}
	//TODO: check it
	public static void main(String[] args)
	{
		new HallOfErosion(-1,qn,"zone_scripts/GraciaContinent");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.config("Loaded Gracia Area: Hall Of Erosion");
	}
}





































/*

public class HallOfErosion extends Quest
{
	
	private class HoEWorld extends InstanceWorld
	{
		public           Map<L2Npc,Boolean> npcList                      = new FastMap<L2Npc,Boolean>();
		public           L2Npc kahamenes                                  = null;
		public           boolean isBossesAttacked                        = false;
		public           long startTime                                  = 0;
		public           String ptLeaderName                             = "";
		public           int rewardItemId                                = -1;
		public List<L2PcInstance> PlayersInInstance = new FastList<L2PcInstance>();
		public           String rewardHtm                                = "";
		public           boolean isRewarded                              = false;
	}
	
	private static final String qn = "HallOfErosion";
	private static final int INSTANCEID = 115;
	private static final boolean debug = false;
	private static final int MOUTHOFEKIMUS = 32537;
	private static final int TEPIOS = 32530;
	private static final int[] ENTER_TELEPORT = {-175883,208233,-11936};
	private static final int INSTANCEPENALTY = 24; //24ctyri hodin
	
	private static final int MIN_PLAYERS = 36;
	private static final int MAX_PLAYERS = 45;
	
	private static final int KAHAMENES = 25651;
	private static final int TUMOR_ZIVY = 18708;
	private static final int TUMOR_MRTVI = 18705;
	private static final int[] TUMOR_MOBY = {22509,22510,22511,22512,22513,22514,22515}; //No retail mobs
	
	private class teleCoord {int instanceId; int x; int y; int z;}
	private static final int[][] TUMOR_SPAWNY = {
		{-175919,210154,-11939},{-178437,212019,-12036},{-179650,211119,-12796},
		{-180935,211896,-12035},{-183396,209998,-11936},{-183382,208173,-11937},
		{-180895,206542,-12032},{-178490,206314,-12032}
	};
	
	private static final int[][] MISTNOST_1 = {
		{22509, -175935, 209536, -11925},
		{22509, -175958, 209708, -11928},
		{22509, -175991, 209959, -11935},
		{22510, -175976, 210146, -11942},
		{22510, -175868, 210196, -11942}
	};
	
	private static final int[][] MISTNOST_2 = {
		{22509, -177905, 211426, -12025},
		{22509, -177899, 211665, -12024},
		{22509, -178071, 211885, -12034},
		{22510, -178293, 212000, -12041},
		{22510, -178513, 212021, -12034},
		{22510, -178755, 211907, -12024},
		{22510, -178907, 211744, -12016},
		{22510, -178911, 211521, -12015},
	};
	
	private static final int[][] MISTNOST_3 = {
		{22509, -179402, 210944, -12792},
		{22509, -179502, 210934, -12793},
		{22509, -179620, 210957, -12795},
		{22510, -179683, 211027, -12793},
		{22510, -179714, 211167, -12797},
		{22510, -179702, 211225, -12795}
	};
	
	private static final int[][] MISTNOST_4 = {
		{22509, -181546, 211262, -12022},
		{22509, -181539, 211564, -12024},
		{22509, -181422, 211834, -12033},
		{22510, -181212, 212055, -12046},
		{22510, -180986, 212154, -12042},
		{22510, -180766, 212109, -12034},
		{22510, -180587, 211974, -12027},
		{22510, -180446, 211657, -12015},
	};
	
	private static final int[][] MISTNOST_5 = {
		{22509, -183280, 209531, -11926},
		{22509, -183375, 209723, -11927},
		{22509, -183398, 209989, -11939},
		{22510, -183297, 210187, -11940},
		{22510, -183188, 210237, -11940},
	};
	
	private static final int[][] MISTNOST_6 = {
		{22509, -182948, 207774, -11930},
		{22509, -183146, 207927, -11942},
		{22509, -183300, 208107, -11945},
		{22510, -183275, 208327, -11948},
		{22510, -183221, 208426, -11938},
	};
	
	private static final int[][] MISTNOST_7 = {
		{22509, -180431, 206941, -12018},
		{22509, -180463, 206746, -12016},
		{22509, -180592, 206551, -12021},
		{22510, -180884, 206350, -12032},
		{22510, -181201, 206345, -12039},
		{22510, -181364, 206447, -12028},
		{22510, -181378, 206642, -12024},
		{22510, -181335, 206906, -12032},
	};
	
	private static final int[][] MISTNOST_8 = {
		{22509, -178087, 207199, -12018},
		{22509, -177995, 207029, -12016},
		{22509, -177903, 206635, -12021},
		{22510, -178112, 206201, -12032},
		{22510, -178424, 206011, -12039},
		{22510, -178700, 206142, -12028},
		{22510, -178791, 206382, -12024},
		{22510, -178824, 206766, -12032},
	};
	
	private boolean checkConditions(L2PcInstance player)
	{
		if (debug)
		{
			return true;
		}
		else
		{
			if (player.getParty() == null)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_IN_PARTY_CANT_ENTER));
				return false;
			}
			L2CommandChannel channel = player.getParty().getCommandChannel();
			if (channel == null)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_IN_COMMAND_CHANNEL_CANT_ENTER));
				return false;
			}
			else if (channel.getChannelLeader() != player)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER));
				return false;
			}
			else if (channel.getMemberCount() < MIN_PLAYERS || channel.getMemberCount() > MAX_PLAYERS)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER));
				return false;
			}
			for (L2PcInstance channelMember : channel.getMembers())
			{
				if (channelMember.getLevel() < 75)
				{
					SystemMessage sm = (SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT));
					sm.addPcName(channelMember);
					channel.broadcastToChannelMembers(sm);
					return false;
				}
				if (!Util.checkIfInRange(1000, player, channelMember, true))
				{
					SystemMessage sm = (SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED));
					sm.addPcName(channelMember);
					channel.broadcastToChannelMembers(sm);
					return false;
				}
				Long reentertime = InstanceManager.getInstance().getInstanceTime(channelMember.getObjectId(), INSTANCEID);
				if (System.currentTimeMillis() < reentertime)
				{
					SystemMessage sm = (SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET));
					sm.addPcName(channelMember);
					channel.broadcastToChannelMembers(sm);
					return false;
				}
			}
			return true;
		}
	}
	
	private int checkworld(L2PcInstance player)
	{
		InstanceWorld checkworld = InstanceManager.getInstance().getPlayerWorld(player);
		if (checkworld != null)
		{
			if (!(checkworld instanceof HoEWorld))
			{
				return 0;
			}
			return 1;
		}
		return 2;
	}
	
	protected int enterInstance(L2PcInstance player, String template, int[] coords,  teleCoord teleto)
	{
		int instanceId = 0;
		//check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		//existing instance
		if (world != null)
		{
			if (!(world instanceof HoEWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return 0;
			}
			teleportPlayer(player, coords, world.instanceId);
			return world.instanceId;
		}
		else
		{
			if (!checkConditions(player))
				return 0;
			L2Party party = player.getParty();
			instanceId = InstanceManager.getInstance().createDynamicInstance(template);
			world = new HoEWorld();
			world.instanceId = instanceId;
			world.templateId = INSTANCEID;
			world.status = 0;
			((HoEWorld)world).startTime = System.currentTimeMillis();
			((HoEWorld)world).ptLeaderName = player.getName();
			InstanceManager.getInstance().addWorld(world);
			_log.info("Hall Of Suffering started " + template + " Instance: " + instanceId + " created by player: " + player.getName());
			runTumors((HoEWorld)world);
			if (debug)
			{
				player.sendMessage("Welcome to Hall of Erosion.");
				InstanceManager.getInstance().setInstanceTime(player.getObjectId(), INSTANCEID, (System.currentTimeMillis()));
				teleportplayer(player,teleto,(HoEWorld)world);
				world.allowed.add(player.getObjectId());
			}
			else
			{
				for (L2PcInstance channelMember : player.getParty().getCommandChannel().getMembers())
				{
					player.sendMessage("Welcome to Hall of Erosion.");					InstanceManager.getInstance().setInstanceTime(channelMember.getObjectId(), INSTANCEID, (System.currentTimeMillis()));
					teleportplayer(channelMember,teleto,(HoEWorld)world);
					removeBuffs(channelMember);
					world.allowed.add(channelMember.getObjectId());
				}
			}
			return instanceId;
		}
	}
	
	private void teleportPlayer(L2PcInstance player, int[] coords, int instanceId)
	{
		player.setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2]);
	}
	
	private void teleportplayer(L2PcInstance player, teleCoord teleto, HoEWorld world )
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(teleto.instanceId);
		player.teleToLocation(teleto.x, teleto.y, teleto.z);
		L2Summon pet = player.getPet();
		if (pet != null)
		{
			pet.setInstanceId(teleto.instanceId);
			pet.teleToLocation(teleto.x, teleto.y, teleto.z);
		}
		world.PlayersInInstance.add(player);
	}
	
	private static final void removeBuffs(L2Character ch)
	{
		for (L2Effect e : ch.getAllEffects())
		{
			if (e == null)
				continue;
			L2Skill skill = e.getSkill();
			if (skill.isDebuff() || skill.isStayAfterDeath())
				continue;
			e.exit();
		}
		if (ch.getPet() != null)
		{
			for (L2Effect e : ch.getPet().getAllEffects())
			{
				if (e == null)
					continue;
				L2Skill skill = e.getSkill();
				if (skill.isDebuff() || skill.isStayAfterDeath())
					continue;
				e.exit();
			}
		}
	}
	
	protected boolean checkKillProgress(L2Npc mob, HoEWorld world)
	{
		if (world.npcList.containsKey(mob))
			world.npcList.put(mob, true);
		for(boolean isDead: world.npcList.values())
			if (!isDead)
				return false;
		return true;
	}
	
	protected int[][] getRoomSpawns(int room)
	{
		switch(room)
		{
			case 0:
				return MISTNOST_1;
			case 1:
				return MISTNOST_2;
			case 2:
				return MISTNOST_3;
			case 3:
				return MISTNOST_4;
			case 4:
				return MISTNOST_5;
			case 5:
				return MISTNOST_6;
			case 6:
				return MISTNOST_7;
			case 7:
				return MISTNOST_8;
		}
		_log.warning("Loaded Hall of Erosion Spawns");
		return new int[][]{};
	}
	
	protected void runTumors(HoEWorld world)
	{
		for (int[] mob : getRoomSpawns(world.status))
		{
			L2Npc npc = addSpawn(mob[0], mob[1], mob[2], mob[3], 0, false,0,false,world.instanceId);
			world.npcList.put(npc, false);
		}
		L2Npc mob = addSpawn(TUMOR_ZIVY, TUMOR_SPAWNY[world.status][0], TUMOR_SPAWNY[world.status][1], TUMOR_SPAWNY[world.status][2], 0, false,0,false,world.instanceId);
		mob.disableCoreAI(true);
		mob.setIsImmobilized(true);
		mob.setCurrentHp(mob.getMaxHp()*0.5);
		world.npcList.put(mob, false);
		world.status++;
	}
	
	
	public HallOfErosion(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(MOUTHOFEKIMUS);
		addTalkId(MOUTHOFEKIMUS);
		addStartNpc(TEPIOS);
		addFirstTalkId(TEPIOS);
		addTalkId(TEPIOS);
		addKillId(TUMOR_MRTVI);
		addKillId(KAHAMENES);
		addAttackId(KAHAMENES);
		for(int mobId : TUMOR_MOBY)
		{
			addSkillSeeId(mobId);
			addKillId(mobId);
		}
	}
	
	public static void main(String[] args)
	{
		new HallOfErosion(-1,qn,"instances");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Gracia Area: Hall of Erosion");
	}
}
*/