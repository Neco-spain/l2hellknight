package quests.Q196_SevenSignSealOfTheEmperor;

import javolution.util.FastList;
import javolution.util.FastMap;

import l2.brick.gameserver.ai.CtrlIntention;
import l2.brick.gameserver.datatables.SkillTable;
import l2.brick.gameserver.instancemanager.InstanceManager;
import l2.brick.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2.brick.gameserver.model.L2Effect;
import l2.brick.gameserver.model.actor.L2Attackable;
import l2.brick.gameserver.model.actor.L2Character;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2DoorInstance;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.entity.Instance;
import l2.brick.gameserver.model.L2Object;
import l2.brick.gameserver.model.L2Skill;
import l2.brick.gameserver.model.L2World;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;
import l2.brick.gameserver.model.quest.State;
import l2.brick.gameserver.network.SystemMessageId;
import l2.brick.gameserver.network.serverpackets.*;

public class Q196_SevenSignSealOfTheEmperor extends Quest
{
	public static final String qn = "Q196_SevenSignSealOfTheEmperor";

	private static class SIGNSNpc
	{
		public L2Npc npc;
	
		public boolean isDead = false;
	}
	
	private static class SIGNSRoom
	{
		public FastList<SIGNSNpc> npcList = new FastList<SIGNSNpc>();
	}
	
	private class SIGNSWorld extends InstanceWorld
	{
		public FastMap<String,SIGNSRoom> rooms = new FastMap<String,SIGNSRoom>();
		public long[] storeTime = { 0, 0 };
		public SIGNSWorld() {}
	}
	
	private static boolean debug 		   = false;
	private static boolean noRndWalk 	   = true;
	
	private static final int INSTANCE_ID       = 112;
	private static final int HEINE             = 30969;
	private static final int MAMMON            = 32584;
	private static final int SHUNAIMAN         = 32586;
	private static final int MAGICAN           = 32598;
	private static final int WOOD              = 32593;
	private static final int LEON              = 32587;
	private static final int PROMICE_OF_MAMMON = 32585;
	private static final int DISCIPLES_GK      = 32657;
	private static final int LILITH        	   = 32715;
	private static final int LILITH_GUARD0 	   = 32716;
	private static final int LILITH_GUARD1 	   = 32717;
	private static final int ANAKIM        	   = 32718;
	private static final int ANAKIM_GUARD0 	   = 32719;
	private static final int ANAKIM_GUARD1 	   = 32720;
	private static final int ANAKIM_GUARD2 	   = 32721;
	private static final int DOOR2  	   = 17240102;
	private static final int DOOR4  	   = 17240104;
	private static final int DOOR6  	   = 17240106;
	private static final int DOOR8  	   = 17240108;
	private static final int DOOR10 	   = 17240110;
	private static final int DOOR 		   = 17240111;
	private static final int[] TELEPORT 	   = { -89559, 216030, -7488 };
	private static final int[] NPCS 	   = { HEINE, WOOD, MAMMON, MAGICAN, SHUNAIMAN, LEON, PROMICE_OF_MAMMON, DISCIPLES_GK };
	private static final int SEALDEVICE 	   = 27384;
	private static final int[] TOKILL 	   = {27371,27372,27373,27374,27375,27377,27378,27379,27384};
	private static final int[] TOCHAT 	   = {27371,27372,27373,27377,27378,27379};
	private static final int WATER 		   = 13808;
	private static final int SWORD 		   = 15310;
	private static final int SEAL  		   = 13846;
	private static final int STAFF 		   = 13809;
	private static final int EINHASAD_STRIKE   = 8357;
	
	private int mammonst = 0;
	
	private static void removeBuffs(L2Character ch)
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
	}

	public Q196_SevenSignSealOfTheEmperor(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(HEINE);
		addSkillSeeId(SEALDEVICE);
		addAttackId(SEALDEVICE);

		for (int i : NPCS)
			addTalkId(i);
		
		for (int mob : TOKILL )
			addKillId(mob);
		
		for (int mob1 : TOCHAT )
			addAggroRangeEnterId(mob1);

		addAttackId(LILITH);
		addAttackId(LILITH_GUARD0);
		addAttackId(LILITH_GUARD1);		
		addAttackId(ANAKIM);
		addAttackId(ANAKIM_GUARD0);
		addAttackId(ANAKIM_GUARD1);
		addAttackId(ANAKIM_GUARD2);
		
		questItemIds = new int[] { SWORD, WATER, SEAL, STAFF };
	}
	
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(player.getInstanceId());
		if (tmpworld instanceof SIGNSWorld)
		{
			if (npc.getNpcId() == 27371)
			{
				((L2Attackable) npc).abortAttack();
				npc.setTarget(player);
				npc.setIsRunning(true);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
			}
			if (npc.getNpcId() == 27372)
			{
				((L2Attackable) npc).abortAttack();
				npc.setTarget(player);
				npc.setIsRunning(true);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
			}
			if (npc.getNpcId() == 27373 || npc.getNpcId() == 27379)
			{
				((L2Attackable) npc).abortAttack();
				npc.setTarget(player);
				npc.setIsRunning(true);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
			}
			if (npc.getNpcId() == 27377)
			{
				((L2Attackable) npc).abortAttack();
				npc.setTarget(player);
				npc.setIsRunning(true);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
			}
			if (npc.getNpcId() == 27378)
			{
				((L2Attackable) npc).abortAttack();
				npc.setTarget(player);
				npc.setIsRunning(true);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
			}
		}
		return null;
	}
	
	protected void runStartRoom(SIGNSWorld world)
	{
		world.status = 0;
		SIGNSRoom StartRoom = new SIGNSRoom();
		SIGNSNpc thisnpc;
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(SHUNAIMAN, -89456, 216184, -7504, 40960, false, 0, false, world.instanceId);
		StartRoom.npcList.add(thisnpc);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(LEON, -89400, 216125, -7504, 40960, false, 0, false, world.instanceId);
		StartRoom.npcList.add(thisnpc);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(DISCIPLES_GK, -84385, 216117, -7497, 0, false, 0, false, world.instanceId);
		StartRoom.npcList.add(thisnpc);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(MAGICAN, -84945, 220643, -7495, 0, false, 0, false, world.instanceId);
		StartRoom.npcList.add(thisnpc);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(MAGICAN, -89563, 220647, -7491, 0, false, 0, false, world.instanceId);
		StartRoom.npcList.add(thisnpc);
		
		world.rooms.put("StartRoom", StartRoom);
		if (debug)
			_log.info("SevenSignSealOfTheEmperor: first room spawned in instance " + world.instanceId);
	}
	
	protected void runFirstRoom(SIGNSWorld world)
	{
		SIGNSRoom FirstRoom = new SIGNSRoom();
		SIGNSNpc thisnpc;
		thisnpc = new SIGNSNpc();
		thisnpc.isDead = false;
		thisnpc.npc = addSpawn(27371,-89049, 217979, -7495, 0, false, 0, false, world.instanceId);
		FirstRoom.npcList.add(thisnpc);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27372,-89049, 217979, -7495, 0, false, 0, false, world.instanceId);
		FirstRoom.npcList.add(thisnpc);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27373,-89049, 217979, -7495, 0, false, 0, false, world.instanceId);
		FirstRoom.npcList.add(thisnpc);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27374,-89049, 217979, -7495, 0, false, 0, false, world.instanceId);
		FirstRoom.npcList.add(thisnpc);
		
		world.rooms.put("FirstRoom", FirstRoom);
		world.status = 1;

		if (debug)
			_log.info("SevenSignSealOfTheEmperor: spawned First room");
	}
	
	protected void runSecondRoom(SIGNSWorld world)
	{
		SIGNSRoom SecondRoom = new SIGNSRoom();
		SIGNSNpc thisnpc;
		thisnpc = new SIGNSNpc();
		thisnpc.isDead = false;
		thisnpc.npc = addSpawn(27371, -88599, 220071, -7495, 0, false, 0, false, world.instanceId);
		SecondRoom.npcList.add(thisnpc);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27371, -88599, 220071, -7495, 0, false, 0, false, world.instanceId);
		SecondRoom.npcList.add(thisnpc);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27372, -88599, 220071, -7495, 0, false, 0, false, world.instanceId);
		SecondRoom.npcList.add(thisnpc);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27373, -88599, 220071, -7495, 0, false, 0, false, world.instanceId);
		SecondRoom.npcList.add(thisnpc);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27373, -88599, 220071, -7495, 0, false, 0, false, world.instanceId);
		SecondRoom.npcList.add(thisnpc);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27374, -88599, 220071, -7495, 0, false, 0, false, world.instanceId);
		SecondRoom.npcList.add(thisnpc);
		
		world.rooms.put("SecondRoom", SecondRoom);
		world.status = 2;
		
		if (debug)
			_log.info("SevenSignSealOfTheEmperor: spawned second room");
	}
	
	protected void runThirdRoom(SIGNSWorld world)
	{
		SIGNSRoom ThirdRoom = new SIGNSRoom();
		SIGNSNpc thisnpc;
		thisnpc = new SIGNSNpc();
		thisnpc.isDead = false;
		thisnpc.npc = addSpawn(27371, -86846, 220639, -7495, 0, false, 0, false, world.instanceId);
		ThirdRoom .npcList.add(thisnpc);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27371, -86846, 220639, -7495, 0, false, 0, false, world.instanceId);
		ThirdRoom .npcList.add(thisnpc);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27372, -86846, 220639, -7495, 0, false, 0, false, world.instanceId);
		ThirdRoom .npcList.add(thisnpc);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27372, -86846, 220639, -7495, 0, false, 0, false, world.instanceId);
		ThirdRoom .npcList.add(thisnpc);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27373, -86846, 220639, -7495, 0, false, 0, false, world.instanceId);
		ThirdRoom .npcList.add(thisnpc);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27373, -86846, 220639, -7495, 0, false, 0, false, world.instanceId);
		ThirdRoom .npcList.add(thisnpc);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27374, -86846, 220639, -7495, 0, false, 0, false, world.instanceId);
		ThirdRoom .npcList.add(thisnpc);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27374, -86846, 220639, -7495, 0, false, 0, false, world.instanceId);
		ThirdRoom .npcList.add(thisnpc);
		
		world.rooms.put("ThirdRoom", ThirdRoom);
		world.status = 3;
		
		if (debug)
			_log.info("SevenSignSealOfTheEmperor: spawned Third room");
	}
	
	protected void runForthRoom(SIGNSWorld world)
	{
		SIGNSRoom ForthRoom = new SIGNSRoom();
		SIGNSNpc thisnpc;
		thisnpc = new SIGNSNpc();
		thisnpc.isDead = false;
		thisnpc.npc = addSpawn(27371, -85463, 219227, -7495, 0, false, 0, false, world.instanceId);
		ForthRoom.npcList.add(thisnpc);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27372, -85463, 219227, -7495, 0, false, 0, false, world.instanceId);
		ForthRoom.npcList.add(thisnpc);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27373, -85463, 219227, -7495, 0, false, 0, false, world.instanceId);
		ForthRoom.npcList.add(thisnpc);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27374, -85463, 219227, -7495, 0, false, 0, false, world.instanceId);
		ForthRoom.npcList.add(thisnpc);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27375, -85463, 219227, -7495, 0, false, 0, false, world.instanceId);
		ForthRoom.npcList.add(thisnpc);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27377, -85463, 219227, -7495, 0, false, 0, false, world.instanceId);
		ForthRoom.npcList.add(thisnpc);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27378, -85463, 219227, -7495, 0, false, 0, false, world.instanceId);
		ForthRoom.npcList.add(thisnpc);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27379, -85463, 219227, -7495, 0, false, 0, false, world.instanceId);
		ForthRoom.npcList.add(thisnpc);
		
		world.rooms.put("ForthRoom", ForthRoom);
		world.status = 4;
		
		if (debug)
			_log.info("SevenSignSealOfTheEmperor: spawned Forth room");
	}
	
	protected void runFifthRoom(SIGNSWorld world)
	{
		SIGNSRoom FifthRoom = new SIGNSRoom();
		SIGNSNpc thisnpc;
		thisnpc = new SIGNSNpc();
		thisnpc.isDead = false;
		thisnpc.npc = addSpawn(27371, -87441, 217623, -7495, 0, false, 0, false, world.instanceId);
		FifthRoom.npcList.add(thisnpc);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27372, -87441, 217623, -7495, 0, false, 0, false, world.instanceId);
		FifthRoom.npcList.add(thisnpc);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27373, -87441, 217623, -7495, 0, false, 0, false, world.instanceId);
		FifthRoom.npcList.add(thisnpc);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27374, -87441, 217623, -7495, 0, false, 0, false, world.instanceId);
		FifthRoom.npcList.add(thisnpc);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27375, -87441, 217623, -7495, 0, false, 0, false, world.instanceId);
		FifthRoom.npcList.add(thisnpc);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27375, -87441, 217623, -7495, 0, false, 0, false, world.instanceId);
		FifthRoom.npcList.add(thisnpc);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27377, -87441, 217623, -7495, 0, false, 0, false, world.instanceId);
		FifthRoom.npcList.add(thisnpc);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27377, -87441, 217623, -7495, 0, false, 0, false, world.instanceId);
		FifthRoom.npcList.add(thisnpc);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27378, -87441, 217623, -7495, 0, false, 0, false, world.instanceId);
		FifthRoom.npcList.add(thisnpc);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27378, -87441, 217623, -7495, 0, false, 0, false, world.instanceId);
		FifthRoom.npcList.add(thisnpc);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27379, -87441, 217623, -7495, 0, false, 0, false, world.instanceId);
		FifthRoom.npcList.add(thisnpc);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27379, -87441, 217623, -7495, 0, false, 0, false, world.instanceId);
		FifthRoom.npcList.add(thisnpc);
		
		world.rooms.put("FifthRoom", FifthRoom);
		world.status = 5;
		
		if (debug)
			_log.info("SevenSignSealOfTheEmperor: spawned Fifth room");
	}
	
	protected void runBossRoom(SIGNSWorld world)
	{	
		SIGNSRoom BossRoom = new SIGNSRoom();
		SIGNSNpc thisnpc;

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(LILITH, -83175, 217021, -7504, 0, false, 0, false, world.instanceId);

		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(LILITH_GUARD0, -83127, 217056, -7504, 0, false, 0, false, world.instanceId);

		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(LILITH_GUARD1, -83222, 217055, -7504, 0, false, 0, false, world.instanceId);

		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(ANAKIM, -83179, 216479, -7504, 0, false, 0, false, world.instanceId);

		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(ANAKIM_GUARD0, -83227, 216443, -7504, 0, false, 0, false, world.instanceId);

		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(ANAKIM_GUARD1, -83134, 216443, -7504, 0, false, 0, false, world.instanceId);

		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(ANAKIM_GUARD2, -83179, 216432, -7504, 0, false, 0, false, world.instanceId);

		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = (L2Attackable) addSpawn(SEALDEVICE, -83177, 217353, -7520, 32768, false, 0, false, world.instanceId);
		BossRoom.npcList.add(thisnpc);
		if (noRndWalk)
			thisnpc.npc.setIsNoRndWalk(true);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = (L2Attackable) addSpawn(SEALDEVICE, -83177, 216137, -7520, 32768, false, 0, false, world.instanceId);
		BossRoom.npcList.add(thisnpc);
		if (noRndWalk)
			thisnpc.npc.setIsNoRndWalk(true);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = (L2Attackable) addSpawn(SEALDEVICE, -82588, 216754, -7520, 32768, false, 0, false, world.instanceId);
		BossRoom.npcList.add(thisnpc);
		if (noRndWalk)
			thisnpc.npc.setIsNoRndWalk(true);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = (L2Attackable) addSpawn(SEALDEVICE, -83804, 216754, -7520, 32768, false, 0, false, world.instanceId);
		BossRoom.npcList.add(thisnpc);
		if (noRndWalk)
			thisnpc.npc.setIsNoRndWalk(true);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(32592, -83176, 216753, -7497, 0, false, 0, false, world.instanceId);
		BossRoom.npcList.add(thisnpc);
		if (noRndWalk)
			thisnpc.npc.setIsNoRndWalk(true);
		
		world.rooms.put("BossRoom", BossRoom);
		world.status = 6;
		if (debug)
			_log.info("SevenSignSealOfTheEmperor: spawned Boss room");
	}
	
	protected void runSDRoom(SIGNSWorld world)
	{
		SIGNSRoom SDRoom = new SIGNSRoom();
		SIGNSNpc thisnpc;
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(SEALDEVICE, -83177, 217353, -7520, 32768, false, 0, false, world.instanceId);
		SDRoom.npcList.add(thisnpc);
		if (noRndWalk)
			thisnpc.npc.setIsNoRndWalk(true);
			thisnpc.npc.setRHandId(15281);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(SEALDEVICE, -83177, 216137, -7520, 32768, false, 0, false, world.instanceId);
		SDRoom.npcList.add(thisnpc);
		if (noRndWalk)
			thisnpc.npc.setIsNoRndWalk(true);
			thisnpc.npc.setRHandId(15281);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(SEALDEVICE, -82588, 216754, -7520, 32768, false, 0, false, world.instanceId);
		SDRoom.npcList.add(thisnpc);
		if (noRndWalk)
			thisnpc.npc.setIsNoRndWalk(true);
			thisnpc.npc.setRHandId(15281);
		
		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(SEALDEVICE, -83804, 216754, -7520, 32768, false, 0, false, world.instanceId);
		SDRoom.npcList.add(thisnpc);
		if (noRndWalk)
			thisnpc.npc.setIsNoRndWalk(true);
			thisnpc.npc.setRHandId(15281);
		
		world.rooms.put("SDRoom", SDRoom);
		if (debug)
			_log.info("SevenSignSealOfTheEmperor: spawned SD room");
	}
	
	protected boolean checkKillProgress(L2Npc npc, SIGNSRoom room)
	{
		boolean cont = true;
		for (SIGNSNpc npcobj : room.npcList)
		{
			if (npcobj.npc == npc)
				npcobj.isDead = true;
			if (npcobj.isDead == false)
				cont = false;
		}
		
		return cont;
	}
	
	private static void teleportPlayer(L2PcInstance player, int[] coords, int instanceId)
	{
		removeBuffs(player);
		if (player.getPet() != null)
		{
			removeBuffs(player.getPet());
		}
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2], true);
	}
	
	private static void openDoor(int doorId, int instanceId)
	{
		for (L2DoorInstance door : InstanceManager.getInstance().getInstance(instanceId).getDoors())
			if (door.getDoorId() == doorId)
				door.openMe();
	}
	
	private synchronized void enterInstance(L2PcInstance player)
	{
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
				teleportPlayer(player, TELEPORT, world.instanceId);
			return;
		}
		else
		{
			final int instanceId = InstanceManager.getInstance().createDynamicInstance("SanctumSealOfTheEmperor.xml");
			
			world = new SIGNSWorld();
			world.instanceId = instanceId;
			world.templateId = INSTANCE_ID;
			InstanceManager.getInstance().addWorld(world);
			((SIGNSWorld) world).storeTime[0] = System.currentTimeMillis();
			runStartRoom((SIGNSWorld) world);
			runFirstRoom((SIGNSWorld) world);
			world.allowed.add(player.getObjectId());
			teleportPlayer(player, TELEPORT, instanceId);
			
			_log.info("SanctumSealOfTheEmperor " + instanceId + " created by player: " + player.getName());
		}
	}
	
	protected void exitInstance(L2PcInstance player)
	{
		player.setInstanceId(0);
		player.teleToLocation(171782, -17612, -4901);
	}
	
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		int npcId = npc.getNpcId();
		if (npcId == LILITH || npcId == LILITH_GUARD0 || npcId == LILITH_GUARD1)
		{
			npc.setCurrentHp(npc.getCurrentHp() + damage);
			((L2Attackable) npc).stopHating(attacker);
		}

		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof SIGNSWorld)
		{
			SIGNSWorld world = (SIGNSWorld) tmpworld;
			
			if (world.status == 6 && npc.getNpcId() == SEALDEVICE)
			{
				npc.doCast(SkillTable.getInstance().getInfo(5980, 3));
			}
		}
		return super.onAttack(npc, attacker, damage, isPet, skill);
	}
	
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof SIGNSWorld)
		{
			SIGNSWorld world = (SIGNSWorld) tmpworld;
			
			if (skill.getId() == EINHASAD_STRIKE && world.status == 6 && npc.getNpcId() == SEALDEVICE)
			{
				npc.doCast(SkillTable.getInstance().getInfo(5980, 3));
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(qn);
			
			if (event.equalsIgnoreCase("30969-05.htm"))
			{
				st.set("cond", "1");
				st.setState(State.STARTED);
				st.playSound("ItemSound.quest_accept");
			}
			else if (event.equalsIgnoreCase("32598-02.htm"))
			{
				st.giveItems(STAFF, 1);
				st.playSound("ItemSound.quest_middle");
			}
			else if (event.equalsIgnoreCase("30969-11.htm"))
			{
				st.set("cond", "6");
				st.playSound("ItemSound.quest_middle");
		
			}
			else if (event.equalsIgnoreCase("32584-05.htm"))
			{
				st.set("cond", "2");
				st.playSound("ItemSound.quest_middle");
				npc.deleteMe();
			}
			else if (event.equalsIgnoreCase("32586-06.htm"))
			{
				st.playSound("ItemSound.quest_middle");
				st.set("cond", "4");
				st.giveItems(SWORD, 1);
				st.giveItems(WATER, 1);
			}
			else if (event.equalsIgnoreCase("32586-12.htm"))
			{
				st.playSound("ItemSound.quest_middle");
				st.set("cond", "5");
				st.takeItems(SEAL, 4);
				st.takeItems(SWORD, 1);
				st.takeItems(WATER, 1);
				st.takeItems(STAFF, 1);
			}
			else if (event.equalsIgnoreCase("32593-02.htm"))
			{
				st.addExpAndSp(52518015, 5817676);
				st.unset("cond");
				st.setState(State.COMPLETED);
				st.exitQuest(false);
				st.playSound("ItemSound.quest_finish");
			}
			else if (event.equalsIgnoreCase("30969-06.htm"))
			{
				if (mammonst == 0)
				{
					mammonst = 1;
					L2Npc mammon = addSpawn(MAMMON, 109742, 219978, -3520, 0, false, 120000, true);
					mammon.broadcastPacket(new NpcSay(mammon.getObjectId(), 0, mammon.getNpcId(), "Who dares summon the Merchant of Mammon?"));
					st.startQuestTimer("despawn", 120000, mammon);
				}
				else
					return "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.... </body></html>";
			}
			else if (event.equalsIgnoreCase("despawn"))
			{
				mammonst = 0;
				return null;
			}
			else if (event.equalsIgnoreCase("DOORS"))
			{
				InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
				if (tmpworld instanceof SIGNSWorld)
				{
					SIGNSWorld world = (SIGNSWorld) tmpworld;

					openDoor(DOOR, world.instanceId);
					for(int objId : world.allowed)
					{
						L2PcInstance pl = L2World.getInstance().getPlayer(objId);
						if (pl != null)
							pl.showQuestMovie(12);
							runBossRoom(world);
					}
					return null;
				}
			}
			else if (event.equalsIgnoreCase("Tele"))
			{
				player.teleToLocation(-89528, 216056, -7516);
				return null;
			}
			return event;
	}
	
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		final int cond = st.getInt("cond");
		switch (npc.getNpcId())
		{
			case HEINE:
				if (player.getLevel() < 79)
				{
					st.exitQuest(true);
					htmltext = "30969-00.htm";
				}
				QuestState qs = player.getQuestState("Q195_SevenSingSecretRitualOfThePriests");
				if (qs.isCompleted() && st.getState() == State.CREATED)
					htmltext = "30969-01.htm";
				else
				{
					switch (cond)
					{
						case 0:
							st.exitQuest(true);
							htmltext = "30969-00.htm";
							break;
						case 1:
							htmltext = "30969-05.htm";
							break;
						case 2:
							st.set("cond", "3");
							htmltext = "30969-08.htm";
							break;
						case 5:
							htmltext = "30969-09.htm";
							break;
						case 6:
							htmltext = "30969-11.htm";
							break;
					}
				}
				break;
			case WOOD:
				if (cond == 6)
					htmltext = "32593-01.htm";
				else if (st.getState() == State.COMPLETED)
					htmltext = getAlreadyCompletedMsg(player);
				break;
			case MAMMON:
				switch (cond)
				{
					case 1:
						htmltext = "32584-01.htm";
						break;
				}
				break;
			case PROMICE_OF_MAMMON:
				switch (cond)
				{
					case 0:
						return null;
					case 1:
						return null;
					case 2:
						return null;
					case 3:
						enterInstance(player);
					case 4:
						enterInstance(player);
					case 5:
						return null;
					case 6:
						return null;
				}
				break;
			case MAGICAN:
				switch (cond)
				{
					case 4:
						if (st.getQuestItemsCount(STAFF) == 0)
							htmltext = "32598-01.htm";
						if (st.getQuestItemsCount(STAFF) >= 1)
							htmltext = "32598-03.htm";
						break;
				}
				break;
			case SHUNAIMAN:
				switch (cond)
				{
					case 3:
						htmltext = "32586-01.htm";
						break;
					case 4:
						if (st.getQuestItemsCount(SWORD) == 0)
						{
							st.giveItems(SWORD, 1);
							htmltext = "32586-14.htm";
						}
						if (st.getQuestItemsCount(WATER) == 0)
						{
							st.giveItems(WATER, 1);
							htmltext = "32586-14.htm";
						}
						if (st.getQuestItemsCount(SEAL) <= 3)
							htmltext = "32586-07.htm";
						if (st.getQuestItemsCount(SEAL) == 4)
							htmltext = "32586-08.htm";
						break;
					case 5:
						htmltext = "32586-13.htm";
						break;
				}
				break;
			case DISCIPLES_GK:
				switch (cond)
				{
					case 4:
						htmltext = "32657-01.htm";
						break;
				}
				break;
			case LEON:
				switch (cond)
				{
					case 3:
						exitInstance(player);
						htmltext = "32587-02.htm";
						break;
					case 4:
						exitInstance(player);
						htmltext = "32587-02.htm";
						break;
					case 5:
						exitInstance(player);
						htmltext = "32587-02.htm";
						break;
				}
				break;
		}
		return htmltext;
	}
	
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(qn);
		
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		SIGNSWorld world;
		
		if (st == null)
			return null;
		
		if (tmpworld instanceof SIGNSWorld)
		{
			world = (SIGNSWorld)tmpworld;
			
			if (world.status == 1)
			{
				if (checkKillProgress(npc, world.rooms.get("FirstRoom")))
				{
					runSecondRoom(world);
					openDoor(DOOR2, world.instanceId);
				}
			}
			else if (world.status == 2)
			{
				if (checkKillProgress(npc, world.rooms.get("SecondRoom")))
				{
					runThirdRoom(world);
					openDoor(DOOR4, world.instanceId);
				}
			}
			else if (world.status == 3)
			{
				if (checkKillProgress(npc, world.rooms.get("ThirdRoom")))
				{
					runForthRoom(world);
					openDoor(DOOR6, world.instanceId);
				}
			}
			else if (world.status == 4)
			{
				if (checkKillProgress(npc, world.rooms.get("ForthRoom")))
				{
					runFifthRoom(world);
					openDoor(DOOR8, world.instanceId);
				}
			}
			else if (world.status == 5)
			{
				if (checkKillProgress(npc, world.rooms.get("FifthRoom")))
				{
					openDoor(DOOR10, world.instanceId);
				}
			}
			else if (world.status == 6)
			{
				if (npc.getNpcId() == SEALDEVICE)
				{
					if (st.getQuestItemsCount(SEAL) < 3)
					{
						npc.setRHandId(15281);
						st.playSound("ItemSound.quest_itemget");
						st.giveItems(SEAL, 1);
					}
					else
					{
						npc.setRHandId(15281);
						st.giveItems(SEAL, 1);
						st.playSound("ItemSound.quest_middle");
						runSDRoom(world);
						player.showQuestMovie(13);
						startQuestTimer("Tele", 26000, null, player);
					}
				}
			}
		}
		return "";
	}
	
	public static void main(String[] args)
	{
		new Q196_SevenSignSealOfTheEmperor(196, qn, "Seven Sign Seal Of The Emperor");
	}
}