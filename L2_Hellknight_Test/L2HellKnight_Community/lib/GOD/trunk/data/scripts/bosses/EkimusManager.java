package bosses;

import javolution.util.FastMap;
import l2rt.common.ThreadPoolManager;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.gameserver.ai.CtrlEvent;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.instancemanager.InstancedZoneManager;
import l2rt.gameserver.instancemanager.InstancedZoneManager.InstancedZone;
import l2rt.gameserver.instancemanager.SeedOfInfinityManager;
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.network.serverpackets.ExShowScreenMessage;
import l2rt.gameserver.network.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.skills.EffectType;
import l2rt.gameserver.skills.Env;
import l2rt.gameserver.skills.effects.EffectTemplate;
import l2rt.gameserver.tables.NpcTable;
import l2rt.gameserver.tables.SkillTable;
import l2rt.util.Location;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;

public class EkimusManager
{
	private static final Location feralSpawn1 = new Location(-179686, 208854, -15496, 16384); // Feral Hound of the Netherworld
	private static final Location feralSpawn2 = new Location(-179387, 208854, -15496, 16384); // Feral Hound of the Netherworld
	private static final Location ekimusSpawn = new Location(-179537, 209551, -15504, 16384); // Ekimus

	//private static final int[] mob_list = new int[] { 22516, 22517, 22518, 22519, 22520, 22521, 22522, 22524, 22526, 22528, 22530, 22532, 22534 }; // эти мобы спаунятся возле Туморов
	private static final int[] mob_list = new int[] { 22524, 22520, 22525, 22527, 22521, 22533, 22528, 22519, 22531,
			22529, 22521, 22535, 22520, 22524, 22526 }; // эти мобы спаунятся возле Туморов
	private static final Location[] spawnList = new Location[] { new Location(-179779, 212540, -15520, 49151, 301),
			new Location(-177028, 211135, -15520, 36863, 302), new Location(-176355, 208043, -15520, 28672, 303),
			new Location(-179284, 205990, -15520, 16384, 304), new Location(-182268, 208218, -15520, 4096, 305),
			new Location(-182069, 211140, -15520, 61439, 306) };

	private static final L2Skill feral_notAggr = SkillTable.getInstance().getInfo(5909, 1);
	private static final L2Skill feral_Aggr = SkillTable.getInstance().getInfo(5910, 1);
	private static final L2Skill[] skills_list = { null, SkillTable.getInstance().getInfo(5923, 1),
			SkillTable.getInstance().getInfo(6020, 1), SkillTable.getInstance().getInfo(6021, 1),
			SkillTable.getInstance().getInfo(6022, 1), SkillTable.getInstance().getInfo(6023, 1) };
	private static final ReentrantLock lock = new ReentrantLock();

	private static L2NpcInstance spawn(int npcId, Location loc, int rndm, long refl)
	{
		try
		{
			L2NpcInstance npc = NpcTable.getTemplate(npcId).getNewInstance();
			if(npc != null)
			{
				npc.setReflection(refl);
				npc.setSpawnedLoc(rndm > 100 ? loc.rnd(70, rndm, false) : loc);
				npc.onSpawn();
				npc.spawnMe(npc.getSpawnedLoc());
				return npc;
			}
		}
		catch(Exception e)
		{}
		return null;
	}

	public static void enterInstance(L2Player player)
	{
		SystemMessage msg = InstancedZoneManager.checkCondition(121, player, true, null, null);
		if(msg != null)
		{
			player.sendPacket(msg);
			return;
		}

		FastMap<Integer, InstancedZone> izs = InstancedZoneManager.getInstance().getById(121);
		if(izs == null)
		{
			player.sendPacket(Msg.SYSTEM_ERROR);
			return;
		}
		InstancedZone iz = izs.get(0);
		if(iz == null)
		{
			player.sendPacket(Msg.SYSTEM_ERROR);
			return;
		}
		String name = iz.getName();
		int time = iz.getTimelimit();
		Reflection ref = new Reflection(name);
		ref.setInstancedZoneId(121);
		for(InstancedZone i : izs.values())
		{
			if(ref.getReturnLoc() == null)
				ref.setReturnLoc(i.getReturnCoords());
			if(ref.getTeleportLoc() == null)
				ref.setTeleportLoc(i.getTeleportCoords());
			ref.FillDoors(i.getDoors());
		}
		EkimusWorld EkimWorld = new EkimusWorld(ref);
		EkimWorld.status = 0;
		lock.lock();
		try
		{
			addWorld(ref.getId(), EkimWorld);
		}
		finally
		{
			lock.unlock();
		}
		L2CommandChannel channel = player.getParty().getCommandChannel();
		for(L2Player members : channel.getMembers())
		{
			members.setReflection(ref);
			members.teleToLocation(iz.getTeleportCoords());
			members.setVar("backCoords", ref.getReturnLoc().toXYZString());
		}
		channel.setReflection(ref);
		ref.setCommandChannel(channel);
		ref.startCollapseTimer(time * 60000);
		channel.broadcastToChannelMembers(new ExShowScreenMessage(new CustomMessage("EkimusMenedger.mes9", player).toString(), 8000, ScreenMessageAlign.TOP_CENTER, false));
		ThreadPoolManager.getInstance().scheduleGeneral(new TelePlayerToEkimus(player), 40000);
	}

	public static boolean teleToHeart(L2Player player, L2NpcInstance npc)
	{
		EkimusWorld world = getWorld(player.getReflectionId());
		if(world != null && !world.partyInHeart)
		{
			world.enterInHeart(player.getParty(), npc);
			npc.deleteMe();
			return true;
		}
		return false;
	}

	public static void OnDie(L2Character self, L2Character killer)
	{
		if(killer == null || killer.getReflectionId() <= 0 || killer.getReflection().getInstancedZoneId() != 121)
			return;
		EkimusWorld worlds = getWorld(killer.getReflectionId());
		if(worlds == null)
			return;
		switch(self.getNpcId())
		{
			case 18708:
				worlds.status++;
				if(worlds.ekimus != null && !worlds.ekimus.isDead() && worlds.status < 6)
				{
					worlds.ekimus.getEffectList().stopEffects(EffectType.SoulRetain);
					try
					{
						L2Skill skills = skills_list[worlds.status];
						for(EffectTemplate et : skills.getEffectTemplates())
						{
							Env env = new Env(worlds.ekimus, worlds.ekimus, skills);
							L2Effect effect = et.getEffect(env);
							worlds.ekimus.getEffectList().addEffect(effect);
						}
					}
					catch(Exception e)
					{
						System.out.println("EkimusManager OnDie - Buff Error" + e);
					}
				}
				if(worlds.status == 6)
				{
					for(L2Player members : worlds.reflection.getPlayers())
						members.sendPacket(new ExShowScreenMessage(new CustomMessage("EkimusMenedger.mes3", members).toString(), 8000, ScreenMessageAlign.TOP_CENTER, false));
					worlds.feral_hound1.getAI().notifyEvent(CtrlEvent.EVT_SEE_SPELL, feral_notAggr, null);
					worlds.feral_hound2.getAI().notifyEvent(CtrlEvent.EVT_SEE_SPELL, feral_notAggr, null);
				}
				else
				{
					for(L2Player members : worlds.reflection.getPlayers())
						members.sendPacket(new ExShowScreenMessage(new CustomMessage("EkimusMenedger.mes1", members).toString(), 8000, ScreenMessageAlign.TOP_CENTER, false));
				}
				L2NpcInstance npc = spawn(32535, self.getLoc(), 0, self.getReflectionId());
				ThreadPoolManager.getInstance().scheduleGeneral(new ReSpawn(npc), 120000);
				break;
			case 29150:
				worlds.rb_died = true;
				worlds.ekimus.deleteMe();
				worlds.feral_hound1.deleteMe();
				worlds.feral_hound2.deleteMe();
				worlds.reflection.startCollapseTimer(300000);
				SeedOfInfinityManager.addEkimusKill();
				worlds.showMovieSuccess();
				ThreadPoolManager.getInstance().scheduleGeneral(new sendMesseg(worlds.reflection, 4), 20000);
		}
	}

	public static class EkimusWorld extends World
	{
		public L2NpcInstance ekimus;
		public L2NpcInstance feral_hound1;
		public L2NpcInstance feral_hound2;
		public ScheduledFuture<?> remainingTimeTask;
		public Reflection reflection;
		public boolean partyInHeart;
		public boolean rb_died = false;
		public long timer;
		public int status;

		public void showMovieOpening()
		{
			for(L2Player player : reflection.getPlayers())
				if(player != null)
					player.showQuestMovie(2);
		}

		public class MovieEkimusTask implements Runnable
		{
			@Override
			public void run()
			{
				showMovieOpening();
			}
		}

		public void enterInHeart(L2Party party, L2NpcInstance npc)
		{
			partyInHeart = true;
			ExShowScreenMessage messeg = new ExShowScreenMessage("%name%'s party has entered the Chamber of Ekimus through the crack in the tumor!".replace("%name%", party.getPartyLeader().getName()), 8000, ScreenMessageAlign.TOP_CENTER, false);
			for(L2Player member : party.getPartyMembers())
			{
				member.teleToLocation(new Location(-179548, 209584, -15504).rnd(50, 200, false));
				member.sendPacket(new ExShowScreenMessage(new CustomMessage("EkimusMenedger.mes7", member).toString(), 8000, ScreenMessageAlign.TOP_CENTER, false));
			}
			for(L2Player member : reflection.getPlayers())
				member.sendPacket(messeg);
			reflection.openDoor(14240102);
			timer = 25;
			remainingTimeTask = ThreadPoolManager.getInstance().scheduleGeneral(new TimerTusk(reflection), 300000);

			ThreadPoolManager.getInstance().scheduleGeneral(new SpawnEkimusTusk(this), 80000);
			ThreadPoolManager.getInstance().scheduleGeneral(new MovieEkimusTask(), 17000);
		}

		public void showMovieSuccess()
		{
			for(L2Player player : reflection.getPlayers())
				if(player != null)
					player.showQuestMovie(3);
		}

		public EkimusWorld(Reflection ref)
		{
			reflection = ref;
		}

		public void spawnEkimus()
		{
			for(Location loc : spawnList)
			{
				spawn(18708, loc, 0, reflection.getId());
				for(int i = 0; i < mob_list.length; i++)
					spawn(mob_list[i], loc, 450, reflection.getId());
			}
			ekimus = spawn(29150, ekimusSpawn, 0, reflection.getId());
			feral_hound1 = spawn(29151, feralSpawn1, 0, reflection.getId());
			feral_hound2 = spawn(29151, feralSpawn2, 0, reflection.getId());
		}

		public void showMovieFailure()
		{
			for(L2Player player : reflection.getPlayers())
				if(player != null)
					player.showQuestMovie(4);
		}

		public void initialInstance(L2Player player)
		{
			spawn(32536, spawnList[3], 0, player.getReflectionId());
		}
	}

	static class TimerTusk implements Runnable
	{
		private Reflection ref;

		public TimerTusk(Reflection rf)
		{
			ref = rf;
		}

		@Override
		public void run()
		{
			EkimusWorld worldss = getWorld(ref.getId());
			if(ref == null || worldss == null || worldss.rb_died)
				return;
			worldss.timer -= 5;
			if(worldss.timer == 0)
			{
				worldss.showMovieFailure();
				ThreadPoolManager.getInstance().scheduleGeneral(new sendMesseg(ref, 6), 19000);
				for(L2MonsterInstance npc : ref.getMonsters())
					if(npc != null)
						npc.deleteMe();
			}
			else
			{
				for(L2Player player : ref.getPlayers())
					player.sendPacket(new ExShowScreenMessage("Heart of Infinity Attack " + worldss.timer + " minute(s) are remaining.", 8000, ScreenMessageAlign.TOP_CENTER, false));
				worldss.remainingTimeTask = ThreadPoolManager.getInstance().scheduleGeneral(new TimerTusk(ref), 300000);
			}
		}
	}

	static class sendMesseg implements Runnable
	{
		private Reflection refl;
		private int packets;

		public sendMesseg(Reflection ref, int packet)
		{
			refl = ref;
			packets = packet;
		}

		@Override
		public void run()
		{
			if(refl != null && refl.getInstancedZoneId() == 121)
				for(L2Player members : refl.getPlayers())
				{
					if(packets == 4)
						members.sendPacket(new ExShowScreenMessage(new CustomMessage("EkimusMenedger.mes4", members).toString(), 8000, ScreenMessageAlign.TOP_CENTER, false));
					else if(packets == 6)
						members.sendPacket(new ExShowScreenMessage(new CustomMessage("EkimusMenedger.mes6", members).toString(), 8000, ScreenMessageAlign.TOP_CENTER, false));
				}
		}
	}

	static class TelePlayerToEkimus implements Runnable
	{
		private long storeId;

		public TelePlayerToEkimus(L2Player player)
		{
			storeId = player.getStoredId();
		}

		@Override
		public void run()
		{
			L2Player player = L2ObjectsStorage.getAsPlayer(storeId);
			EkimusWorld world = getWorld(player.getReflectionId());
			if(player == null || world == null)
				return;
			world.initialInstance(player);
		}
	}

	static class SpawnEkimusTusk implements Runnable
	{
		private EkimusWorld world;

		private SpawnEkimusTusk(EkimusWorld wor)
		{
			world = wor;
		}

		@Override
		public void run()
		{
			world.spawnEkimus();
		}
	}

	static class ReSpawn implements Runnable
	{
		private L2NpcInstance _npc;

		private ReSpawn(L2NpcInstance npc)
		{
			_npc = npc;
		}

		@Override
		public void run()
		{
			EkimusWorld world = getWorld(_npc.getReflectionId());
			if(world == null || world.rb_died)
				return;
			world.status--;
			if(world.ekimus != null && !world.ekimus.isDead())
			{
				world.ekimus.getEffectList().stopEffects(EffectType.SoulRetain);
				if(world.status > 0)
					try
					{
						L2Skill skills = skills_list[world.status];
						for(EffectTemplate et : skills.getEffectTemplates())
						{
							Env env = new Env(world.ekimus, world.ekimus, skills);
							L2Effect effect = et.getEffect(env);
							world.ekimus.getEffectList().addEffect(effect);
						}
					}
					catch(Exception e)
					{
						System.out.println("EkimusManager ReSpawn - Buff Error" + e);
					}
			}
			Reflection ref = _npc.getReflection();
			if(world.status == 0)
			{
				for(L2Player members : ref.getPlayers())
					members.sendPacket(new ExShowScreenMessage(new CustomMessage("EkimusMenedger.mes8", members).toString(), 8000, ScreenMessageAlign.TOP_CENTER, false));
				world.feral_hound1.getAI().notifyEvent(CtrlEvent.EVT_SEE_SPELL, feral_Aggr, null);
				world.feral_hound2.getAI().notifyEvent(CtrlEvent.EVT_SEE_SPELL, feral_Aggr, null);
			}
			_npc.deleteMe();
			spawn(18708, _npc.getSpawnedLoc(), 0, _npc.getReflectionId());
			for(L2Player members : ref.getPlayers())
				members.sendPacket(new ExShowScreenMessage(new CustomMessage("EkimusMenedger.mes5", members).toString(), 8000, ScreenMessageAlign.TOP_CENTER, false));
		}
	}

	public static FastMap<Long, EkimusWorld> worlds = new FastMap<Long, EkimusWorld>();

	public static void addWorld(long id, EkimusWorld world)
	{
		worlds.put(id, world);
	}

	public static EkimusWorld getWorld(long id)
	{
		EkimusWorld world = worlds.get(id);
		if(world != null)
			return world;
		return null;
	}

	public static class World
	{}
}