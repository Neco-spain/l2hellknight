package bosses;

import javolution.util.FastMap;
import l2rt.common.ThreadPoolManager;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.instancemanager.InstancedZoneManager;
import l2rt.gameserver.instancemanager.ZoneManager;
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.tables.ReflectionTable;
import l2rt.util.GArray;

import java.util.concurrent.ScheduledFuture;

public class LastImperialTombManager extends Functions implements ScriptFile
{
	// instance list of monsters.
	private static GArray<L2NpcInstance> _hallAlarmDevices = new GArray<L2NpcInstance>();
	private static GArray<L2NpcInstance> _darkChoirCaptains = new GArray<L2NpcInstance>();
	private static GArray<L2NpcInstance> _room1Monsters = new GArray<L2NpcInstance>();
	private static GArray<L2NpcInstance> _room2InsideMonsters = new GArray<L2NpcInstance>();
	private static GArray<L2NpcInstance> _room2OutsideMonsters = new GArray<L2NpcInstance>();

	// instance list of doors.
	private static final int[] _room1Doors = { 17130042, 17130051, 17130052, 17130053, 17130054, 17130055, 17130056, 17130057, 17130058 };
    private static final int[] _room2InsideDoors = { 17130061, 17130062, 17130063, 17130064, 17130065, 17130066, 17130067, 17130068, 17130069, 17130070, };
    private static final int _room2OutsideDoor1 = 17130043;
    private static final int _room2OutsideDoor2 = 17130045;
    private static final int _room3Door = 17130046;

    //debug mode.
    private static boolean _debug = false;

	private static L2Player _commander = null;

	// Frintezza's Magic Force Field Removal Scroll.
	private static final int SCROLL = 8073;

	private static ScheduledFuture<?> _InvadeTask = null;
	private static ScheduledFuture<?> _RegistrationTimeInfoTask = null;
	private static ScheduledFuture<?> _Room1SpawnTask = null;
	private static ScheduledFuture<?> _Room2InsideDoorOpenTask = null;
	private static ScheduledFuture<?> _Room2OutsideSpawnTask = null;
	private static ScheduledFuture<?> _CheckTimeUpTask = null;

	private static L2Zone _zone;

	private static final int ALARM_DEVICE = 18328;
	private static final int CHOIR_PRAYER = 18339;
	private static final int CHOIR_CAPTAIN = 18334;
	
	private static void init()
	{
		_zone = ZoneManager.getInstance().getZoneById(ZoneType.epic, 702121, false);
		LastImperialTombSpawnlist.clear();
		LastImperialTombSpawnlist.fill();
		System.out.println("LastImperialTombManager: Init The Last Imperial Tomb.");
	}

	// RegistrationMode = command channel.
	public void tryRegistration()
	{
        L2Player player = (L2Player) getSelf();
        L2Party party = player.getParty();

        InstancedZoneManager izm = InstancedZoneManager.getInstance();
        FastMap<Integer, InstancedZoneManager.InstancedZone> izs = InstancedZoneManager.getInstance().getById(136);
        if (izs == null) {
            player.sendPacket(Msg.SYSTEM_ERROR);
            return;
        }
        InstancedZoneManager.InstancedZone iz = izs.get(0);
        if (iz == null) {
            player.sendPacket(Msg.SYSTEM_ERROR);
            return;
        }
        String name = iz.getName();
        int minMembers = iz.getMinParty();
        int maxMembers = iz.getMaxParty();
        int min_level = iz.getMinLevel();
        int max_level = iz.getMaxLevel();

        //check party
        if (party == null) {
            player.sendPacket(Msg.YOU_ARE_NOT_CURRENTLY_IN_A_PARTY_SO_YOU_CANNOT_ENTER);
            return;
        }

        // Если игрок тпанулся из инста(смерть, сое), возвращаем его в инстанс
        if (player.getParty().isInReflection()) {
            Reflection old_ref = player.getParty().getReflection();
            if (old_ref.getInstancedZoneId() != 136) {
                player.sendMessage("Неправильно выбран инстанс");
                return;
            }

            if (player.getLevel() < min_level || player.getLevel() > max_level) {
                player.sendPacket(new SystemMessage(SystemMessage.C1S_LEVEL_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED).addName(player));
                return;
            }
            if (player.isCursedWeaponEquipped() || player.isInFlyingTransform() || player.isDead()) {
                player.sendPacket(new SystemMessage(SystemMessage.C1S_QUEST_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED).addName(player));
                return;
            }
            if (izm.getTimeToNextEnterInstance(name, player) > 0) {
                player.sendPacket(new SystemMessage(SystemMessage.C1_MAY_NOT_RE_ENTER_YET).addName(player));
                return;
            }
            player.setReflection(old_ref);
            player.teleToLocation(iz.getTeleportCoords(), old_ref.getId());
            return;
        }
        if(!_debug)
        {
            if (!player.getParty().isInCommandChannel()) {
                player.sendPacket(Msg.YOU_CANNOT_ENTER_BECAUSE_YOU_ARE_NOT_IN_A_CURRENT_COMMAND_CHANNEL);
                return;
            }

            L2CommandChannel cc = player.getParty().getCommandChannel();
            //check cc leader
            if (cc.getChannelLeader() != player) {
                player.sendMessage("You must be leader of the command channel.");
                return;
            }
            //check min-max member count for CC
            if (cc.getMemberCount() < minMembers) {
                player.sendMessage("The command channel must contains at least " + minMembers + " members.");
                return;
            }
            if (cc.getMemberCount() > maxMembers) {
                player.sendMessage("The command channel must contains not more than " + maxMembers + " members.");
                return;
            }

            for (L2Player member : cc.getMembers()) {
                if (member.getLevel() < min_level || member.getLevel() > max_level) {
                    player.sendPacket(new SystemMessage(SystemMessage.C1S_LEVEL_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED).addName(member));
                    return;
                }
                if (member.isCursedWeaponEquipped() || member.isInFlyingTransform() || member.isDead()) {
                    player.sendPacket(new SystemMessage(SystemMessage.C1S_QUEST_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED).addName(member));
                    return;
                }
                if (!player.isInRange(member, 500)) {
                    member.sendPacket(Msg.ITS_TOO_FAR_FROM_THE_NPC_TO_WORK);
                    player.sendPacket(Msg.ITS_TOO_FAR_FROM_THE_NPC_TO_WORK);
                    return;
                }
                if (izm.getTimeToNextEnterInstance(name, member) > 0) {
                    cc.broadcastToChannelMembers(new SystemMessage(SystemMessage.C1_MAY_NOT_RE_ENTER_YET).addName(member));
                    return;
                }
            }

            if(player.getInventory().getCountOf(SCROLL) < 1)
            {
                player.sendMessage("You must possess a \"Frintezza's Magic Force Field Removal Scroll\".");
                return;
            }
        }
		registration(player);
	}

	// registration to enter to tomb.
	private static synchronized void registration(L2Player pc)
	{
		if(_commander != null)
			return;
		_commander = pc;
		pc.getInventory().destroyItemByItemId(SCROLL, 1, true);
		if(_InvadeTask != null)
			_InvadeTask.cancel(true);
		_InvadeTask = ThreadPoolManager.getInstance().scheduleGeneral(new Invade(), 10000);
	}

	private static void doInvade()
	{
        FastMap<Integer, InstancedZoneManager.InstancedZone> izs = InstancedZoneManager.getInstance().getById(136);
        if (izs == null) {
            _commander.sendPacket(Msg.SYSTEM_ERROR);
            return;
        }
        InstancedZoneManager.InstancedZone iz = izs.get(0);
        if (iz == null) {
            _commander.sendPacket(Msg.SYSTEM_ERROR);
            return;
        }

        String name = iz.getName();

        Reflection r = new Reflection(name);
        r.setInstancedZoneId(136);
        for (InstancedZoneManager.InstancedZone i : izs.values())
        {
            if (r.getTeleportLoc() == null)
            {
                r.setTeleportLoc(i.getTeleportCoords());
            }
            r.FillSpawns(i.getSpawnsInfo());
            r.FillDoors(i.getDoors());
        }

        r.setCoreLoc(r.getReturnLoc());
        r.setReturnLoc(_commander.getLoc());
        if(!_debug)
        {
            if (_commander.getParty().isInCommandChannel())
            {
                L2CommandChannel cc = _commander.getParty().getCommandChannel();
                for (L2Player member : cc.getMembers())
                {
                    member.setVar("backCoords", r.getReturnLoc().toXYZString());
                    member.teleToLocation(iz.getTeleportCoords(), r.getId());
                }
                cc.setReflection(r);
                r.setCommandChannel(cc);
            }
        }
        else
        {
            for (L2Player member : _commander.getParty().getPartyMembers())
            {
                member.setVar("backCoords", r.getReturnLoc().toXYZString());
                member.teleToLocation(iz.getTeleportCoords(), r.getId());
            }
            _commander.getParty().setReflection(r);
            r.setParty(_commander.getParty());
        }
	}

	public static void OnDie(L2Character self, L2Character killer)
	{
		if(self == null)
			return;
		switch(self.getNpcId())
		{
			case ALARM_DEVICE:
				onKillHallAlarmDevice();
				break;
			case CHOIR_PRAYER:
				onKillDarkChoirPlayer();
				break;
			case CHOIR_CAPTAIN:
				onKillDarkChoirCaptain();
				break;
		}
	}

	// Is the door of room1 in confirmation to open.
	private static void onKillHallAlarmDevice()
	{
		if(_Room1SpawnTask != null)
		{
			_Room1SpawnTask.cancel(true);
			_Room1SpawnTask = null;
		}
		_Room1SpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new SpawnRoom1Mobs1st(), 5000);
		spawnRoom2InsideMob();
	}

	// Is the door of inside of room2 in confirmation to open.
	private static void onKillDarkChoirPlayer()
	{
		int killCnt = 0;
		for(L2NpcInstance DarkChoirPlayer : _room2InsideMonsters)
			if(DarkChoirPlayer.isDead())
				killCnt++;
		if(_room2InsideMonsters.size() <= killCnt)
		{
			if(_Room2InsideDoorOpenTask != null)
				_Room2InsideDoorOpenTask.cancel(true);
			if(_Room2OutsideSpawnTask != null)
				_Room2OutsideSpawnTask.cancel(true);
			_Room2InsideDoorOpenTask = ThreadPoolManager.getInstance().scheduleGeneral(new OpenRoom2InsideDoors(), 3000);
			_Room2OutsideSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new SpawnRoom2OutsideMobs(), 4000);
		}
	}

	// Is the door of outside of room2 in confirmation to open.
	private static void onKillDarkChoirCaptain()
	{
		int killCnt = 0;
		for(L2NpcInstance DarkChoirCaptain : _darkChoirCaptains)
			if(DarkChoirCaptain.isDead())
				killCnt++;
		if(_darkChoirCaptains.size() <= killCnt)
		{
            ReflectionTable.getInstance().get(_commander.getReflectionId()).openDoor(_room2OutsideDoor1);
            ReflectionTable.getInstance().get(_commander.getReflectionId()).openDoor(_room2OutsideDoor2);
            ReflectionTable.getInstance().get(_commander.getReflectionId()).openDoor(_room3Door);
		}
	}

	private static void openRoom1Doors()
	{
		for(int door : _room1Doors)
			ReflectionTable.getInstance().get(_commander.getReflectionId()).openDoor(door);
	}

	private static void spawnRoom2InsideMob()
	{
		for(L2Spawn spawn : LastImperialTombSpawnlist.getRoom2InsideSpawnList())
		{
            spawn.setReflection(_commander.getReflectionId());
			L2NpcInstance mob = spawn.doSpawn(true);
			mob.getSpawn().stopRespawn();
			_room2InsideMonsters.add(mob);
		}
	}

	public static void cleanUpTomb(boolean banish)
	{
		cleanUpMobs();
		if(banish)
			banishForeigners();
		_commander = null;
		if(_InvadeTask != null)
			_InvadeTask.cancel(true);
		if(_RegistrationTimeInfoTask != null)
			_RegistrationTimeInfoTask.cancel(true);
		if(_Room1SpawnTask != null)
			_Room1SpawnTask.cancel(true);
		if(_Room2InsideDoorOpenTask != null)
			_Room2InsideDoorOpenTask.cancel(true);
		if(_Room2OutsideSpawnTask != null)
			_Room2OutsideSpawnTask.cancel(true);
		if(_CheckTimeUpTask != null)
			_CheckTimeUpTask.cancel(true);
		_InvadeTask = null;
		_RegistrationTimeInfoTask = null;
		_Room1SpawnTask = null;
		_Room2InsideDoorOpenTask = null;
		_Room2OutsideSpawnTask = null;
		_CheckTimeUpTask = null;
	}

	private static void cleanUpMobs()
	{
		for(L2NpcInstance mob : _hallAlarmDevices)
			mob.deleteMe();
		for(L2NpcInstance mob : _darkChoirCaptains)
			mob.deleteMe();
		for(L2NpcInstance mob : _room1Monsters)
			mob.deleteMe();
		for(L2NpcInstance mob : _room2InsideMonsters)
			mob.deleteMe();
		for(L2NpcInstance mob : _room2OutsideMonsters)
			mob.deleteMe();
		_hallAlarmDevices.clear();
		_darkChoirCaptains.clear();
		_room1Monsters.clear();
		_room2InsideMonsters.clear();
		_room2OutsideMonsters.clear();
	}

	private static class SpawnRoom1Mobs1st implements Runnable
	{
		public void run()
		{
			L2NpcInstance mob;
			for(L2Spawn spawn : LastImperialTombSpawnlist.getRoom1SpawnList1st())
				if(spawn.getNpcId() != ALARM_DEVICE)
				{
                    spawn.setReflection(_commander.getReflectionId());
					mob = spawn.doSpawn(true);
					mob.getSpawn().stopRespawn();
					_room1Monsters.add(mob);
				}
			openRoom1Doors();
			ReflectionTable.getInstance().get(_commander.getReflectionId()).openDoor(_room2OutsideDoor1);
			if(_Room1SpawnTask != null)
				_Room1SpawnTask.cancel(true);				
		}
	}

	private static class OpenRoom2InsideDoors implements Runnable
	{
		public void run()
		{
            ReflectionTable.getInstance().get(_commander.getReflectionId()).closeDoor(_room2OutsideDoor1);
			for(int door : _room2InsideDoors)
				 ReflectionTable.getInstance().get(_commander.getReflectionId()).openDoor(door);
		}
	}

	private static class SpawnRoom2OutsideMobs implements Runnable
	{
		public void run()
		{
			for(L2Spawn spawn : LastImperialTombSpawnlist.getRoom2OutsideSpawnList())
			{
				if(spawn.getNpcId() == CHOIR_CAPTAIN)
				{
                    spawn.setReflection(_commander.getReflectionId());
					L2NpcInstance mob = spawn.doSpawn(true);
					mob.getSpawn().stopRespawn();
					_darkChoirCaptains.add(mob);
				}
				else
				{
                    spawn.setReflection(_commander.getReflectionId());
					L2NpcInstance mob = spawn.doSpawn(true);
					mob.getSpawn().stopRespawn();
					_room2OutsideMonsters.add(mob);
				}
			}
		}
	}

	private static class Invade implements Runnable
	{
		public void run()
		{
			doInvade();
		}
	}

	private static void banishForeigners()
	{
		for(L2Player player : getPlayersInside())
			if(!player.isGM())
				player.teleToClosestTown();
	}

	private static GArray<L2Player> getPlayersInside()
	{
		return getZone().getInsidePlayers();
	}

	public static L2Zone getZone()
	{
		return _zone;
	}

	public void onLoad()
	{
		init();
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}