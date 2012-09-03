package l2rt.gameserver.model.entity.siege.fortress;

import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.idfactory.IdFactory;
import l2rt.gameserver.instancemanager.FortressSiegeManager;
import l2rt.gameserver.instancemanager.MercTicketManager;
import l2rt.gameserver.instancemanager.SiegeGuardManager;
import l2rt.gameserver.instancemanager.SiegeManager;
import l2rt.gameserver.model.L2Clan;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.entity.residence.Fortress;
import l2rt.gameserver.model.entity.siege.*;
import l2rt.gameserver.model.instances.L2CommanderInstance;
import l2rt.gameserver.model.instances.L2DoorInstance;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.instances.L2StaticObjectInstance;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.SiegeInfo;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.tables.ClanTable;
import l2rt.gameserver.tables.MapRegion;
import l2rt.gameserver.tables.NpcTable;
import l2rt.gameserver.xml.ItemTemplates;
import l2rt.util.GArray;

import java.util.Calendar;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;

public class FortressSiege extends Siege
{
	private final GArray<L2CommanderInstance> _commanders = new GArray<L2CommanderInstance>();
	private final GArray<L2StaticObjectInstance> _flagPoles = new GArray<L2StaticObjectInstance>();
	private final GArray<L2ItemInstance> _flags = new GArray<L2ItemInstance>();

	private ScheduledFuture<?> _commanderRespawnTask = null;

	public FortressSiege(Fortress siegeUnit)
	{
		super(siegeUnit);
		_database = new FortressSiegeDatabase(this);
		_siegeGuardManager = new SiegeGuardManager(getSiegeUnit());
		_database.loadSiegeClan();
	}

	@Override
	public void startSiege()
	{
		if(!_isInProgress)
		{
			setRegistrationOver(true);

			_database.loadSiegeClan(); // Load siege clan from db

			if(getAttackerClans().isEmpty())
			{
				if(getSiegeUnit().getOwnerId() <= 0)
					announceToPlayer(new SystemMessage(SystemMessage.THE_SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST).addString(getSiegeUnit().getName()), false, true);
				else
					announceToPlayer(new SystemMessage(SystemMessage.S1S_SIEGE_WAS_CANCELED_BECAUSE_THERE_WERE_NO_CLANS_THAT_PARTICIPATED).addString(getSiegeUnit().getName()), false, true);
				return;
			}

			getZone().setActive(true);
			//TODO: Включить активацию после описания residence зон крепостей
			//getResidenseZone().setActive(true);

			_isInProgress = true; // Flag so that same siege instance cannot be started again
			_isMidVictory = true; // Для того, чтобы атакующие могли атаковать друг друга
			_ownerBeforeStart = getSiegeUnit().getOwnerId();

			updateSiegeClans();
			updatePlayerSiegeStateFlags(false);

			teleportPlayer(TeleportWhoType.Attacker, MapRegion.TeleportWhereType.ClosestTown); // Teleport to the closest town
			teleportPlayer(TeleportWhoType.Spectator, MapRegion.TeleportWhereType.ClosestTown); // Teleport to the closest town

			// Despawn commanders (Npcs)
			for(L2NpcInstance commanderNpc : FortressSiegeManager.getCommanderNpcsList(getSiegeUnit().getId()))
				if(commanderNpc != null)
					commanderNpc.decayMe();

			// Spawn commanders (Siege guards)
			spawnCommanders();

			getSiegeUnit().spawnDoor(); // Spawn door
			getSiegeGuardManager().spawnSiegeGuard(); // Spawn siege guard
			MercTicketManager.getInstance().deleteTickets(getSiegeUnit().getId()); // remove the tickets from the ground
			_defenderRespawnPenalty = 0; // Reset respawn delay

			// Schedule a task to prepare auto siege end
			_siegeEndDate = Calendar.getInstance();
			_siegeEndDate.add(Calendar.MINUTE, getSiegeLength());
			ThreadPoolManager.getInstance().scheduleGeneral(new SiegeEndTask(this), 1000); // Prepare auto end task
			_fameTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new SiegeFameTask(), 5 * 60 * 1000L, 5 * 60 * 1000L);

			announceToPlayer(new SystemMessage(SystemMessage.THE_FORTRESS_BATTLE_S1_HAS_BEGAN).addString(getSiegeUnit().getName()), false, true);
		}
	}

	@Override
	public void midVictory()
	{
		// Если осада закончилась
		if(!isInProgress() || getSiegeUnit().getOwnerId() <= 0)
			return;

		// Поменять местами атакующих и защитников
		for(SiegeClan sc : getDefenderClans().values())
			if(sc != null)
			{
				removeSiegeClan(sc, SiegeClanType.DEFENDER);
				addSiegeClan(sc, SiegeClanType.ATTACKER);
			}

		SiegeClan sc_newowner = getAttackerClan(getSiegeUnit().getOwner());
		removeSiegeClan(sc_newowner, SiegeClanType.ATTACKER);
		addSiegeClan(sc_newowner, SiegeClanType.OWNER);

		endSiege();
	}

	@Override
	public void endSiege()
	{
		getZone().setActive(false);
		//TODO: Включить деактивацию после описания residence зон крепостей
		//getResidenseZone().setActive(false);

		if(isInProgress())
		{
			announceToPlayer(new SystemMessage(SystemMessage.THE_FORTRESS_BATTLE_OF_S1_HAS_FINISHED).addString(getSiegeUnit().getName()), false, true);

			if(getSiegeUnit().getOwnerId() <= 0)
				announceToPlayer(new SystemMessage(SystemMessage.THE_SIEGE_OF_S1_HAS_ENDED_IN_A_DRAW).addString(getSiegeUnit().getName()), false, true);
			else
			{
				L2Clan oldOwner = null;
				if(_ownerBeforeStart != 0)
					oldOwner = ClanTable.getInstance().getClan(_ownerBeforeStart);
				L2Clan newOwner = ClanTable.getInstance().getClan(getSiegeUnit().getOwnerId());

				if(oldOwner == null)
				{ // fortress was taken over from scratch
					if(newOwner.getLevel() >= 5)
						newOwner.broadcastToOnlineMembers(new SystemMessage(SystemMessage.SINCE_YOUR_CLAN_EMERGED_VICTORIOUS_FROM_THE_SIEGE_S1_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLAN_REPUTATION_SCORE).addNumber(newOwner.incReputation(200, true, "FortressSiege")));
					SiegeManager.clearFortressRegistrations(newOwner);
				}
				else if(newOwner.equals(oldOwner))
				{ // fortress was defended
					if(newOwner.getLevel() >= 5)
						newOwner.broadcastToOnlineMembers(new SystemMessage(SystemMessage.SINCE_YOUR_CLAN_EMERGED_VICTORIOUS_FROM_THE_SIEGE_S1_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLAN_REPUTATION_SCORE).addNumber(newOwner.incReputation(200, true, "FortressSiege")));
					SiegeManager.clearFortressRegistrations(newOwner);
				}
				else
				{ // fortress was taken over by another clan
					announceToPlayer(new SystemMessage(SystemMessage.S1_CLAN_IS_VICTORIOUS_IN_THE_FORTRESS_BATLE_OF_S2).addString(newOwner.getName()).addString(getSiegeUnit().getName()), false, true);
					if(newOwner.getLevel() >= 5)
						newOwner.broadcastToOnlineMembers(new SystemMessage(SystemMessage.SINCE_YOUR_CLAN_EMERGED_VICTORIOUS_FROM_THE_SIEGE_S1_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLAN_REPUTATION_SCORE).addNumber(newOwner.incReputation(500, true, "FortressSiege")));
					if(oldOwner.getLevel() >= 5)
						oldOwner.broadcastToOnlineMembers(new SystemMessage(SystemMessage.YOUR_CLAN_HAS_FAILED_TO_DEFEND_THE_CASTLE_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_CLAN_REPUTATION_SCORE).addNumber(-oldOwner.incReputation(-500, true, "FortressSiege")));
					SiegeManager.clearFortressRegistrations(newOwner);
				}

				// Spawn envoys
				for(L2NpcInstance envoyNpc : FortressSiegeManager.getEnvoyNpcsList(getSiegeUnit().getId()))
					if(envoyNpc != null)
						envoyNpc.spawnMe();
			}

			// Despawn commanders (Siege guards)
			unspawnCommanders();

			// Spawn commanders (Npcs)
			for(L2NpcInstance commanderNpc : FortressSiegeManager.getCommanderNpcsList(getSiegeUnit().getId()))
				if(commanderNpc != null)
					commanderNpc.spawnMe();

			removeHeadquarters();
			unSpawnFlags();
			teleportPlayer(TeleportWhoType.Attacker, MapRegion.TeleportWhereType.ClosestTown); // Teleport to the closest town
			teleportPlayer(TeleportWhoType.Spectator, MapRegion.TeleportWhereType.ClosestTown); // Teleport to the closest town
			removeSiegeSummons();
			_isInProgress = false; // Flag so that siege instance can be started
			updatePlayerSiegeStateFlags(true);
			saveSiege(); // Save fortress specific data
			_database.clearSiegeClan(); // Clear siege clan from db
			getSiegeGuardManager().unspawnSiegeGuard(); // Remove all spawned siege guard from this fortress
			SiegeGuardManager.removeMercsFromDb(getSiegeUnit().getId());
			getSiegeUnit().spawnDoor(); // Respawn door to fortress
			if(_ownerBeforeStart != getSiegeUnit().getOwnerId())
				getSiegeUnit().setOwnDate((int) (System.currentTimeMillis() / 1000));
			getSiegeUnit().saveOwnDate();
			clearSiegeClans();

			if(_siegeStartTask != null)
			{
				_siegeStartTask.cancel(false);
				_siegeStartTask = null;
			}
			if(_fameTask != null)
			{
				_fameTask.cancel(true);
				_fameTask = null;
			}

			setRegistrationOver(false);
		}
	}

	@Override
	public void Engrave(L2Clan clan, int objId)
	{
		if(clan.getHasCastle() > 0)
		{
			getSiegeUnit().changeOwner(null);
			announceToPlayer(Msg.THE_REBEL_ARMY_RECAPTURED_THE_FORTRESS, false, true);
		}
		else
			getSiegeUnit().changeOwner(clan);
	}

	@Override
	public void registerAttacker(L2Player player, boolean force)
	{
		super.registerAttacker(player, force);
		startAutoTask(false);
	}

	/**
	 * Start the auto tasks<BR><BR>
	 */
	@Override
	public void startAutoTask(boolean isServerStarted)
	{
		if(getAttackerClans().isEmpty() || _siegeStartTask != null)
			return;

		_siegeDate.setTimeInMillis(((Fortress) getSiegeUnit()).getSiegeDate() * 1000L);

		setNextSiegeDate();

		_siegeRegistrationEndDate = Calendar.getInstance();
		_siegeRegistrationEndDate.setTimeInMillis(_siegeDate.getTimeInMillis());
		_siegeRegistrationEndDate.add(Calendar.MINUTE, -10);

		// Если сервер только что стартовал, осада начнется не ранее чем через час
		if(isServerStarted)
		{
			Calendar minDate = Calendar.getInstance();
			minDate.add(Calendar.HOUR_OF_DAY, 1);
			_siegeDate.setTimeInMillis(Math.max(minDate.getTimeInMillis(), _siegeDate.getTimeInMillis()));
			_database.saveSiegeDate();

			// Если был рестарт во время осады, даем зарегистрироваться еще раз
			if(_siegeDate.getTimeInMillis() <= minDate.getTimeInMillis())
			{
				setRegistrationOver(false);
				_siegeRegistrationEndDate.setTimeInMillis(_siegeDate.getTimeInMillis());
				_siegeRegistrationEndDate.add(Calendar.MINUTE, -10);
			}
		}

		_siegeStartTask = ThreadPoolManager.getInstance().scheduleGeneral(new SiegeStartTask(this), 1000);
	}

	/** Set the date for the next siege. */
	@Override
	protected void setNextSiegeDate()
	{
		if(_siegeDate.getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
		{
			_siegeDate = Calendar.getInstance();
			// Осада не чаще, чем каждые 4 часа + 1 час на подготовку.
			if(Calendar.getInstance().getTimeInMillis() - getSiegeUnit().getLastSiegeDate() * 1000L > 14400000)
				_siegeDate.add(Calendar.HOUR_OF_DAY, 1);
			else
			{
				_siegeDate.setTimeInMillis(getSiegeUnit().getLastSiegeDate() * 1000L);
				_siegeDate.add(Calendar.HOUR_OF_DAY, 5);
			}
			_database.saveSiegeDate();
		}
	}

	@Override
	protected void correctSiegeDateTime()
	{}

	@Override
	protected void saveSiege()
	{
		// Выставляем дату прошедшей осады
		getSiegeUnit().setLastSiegeDate((int) (getSiegeDate().getTimeInMillis() / 1000));
		// Сохраняем дату прошедшей осады
		_database.saveLastSiegeDate();
	}

	/** Display list of registered clans */
	@Override
	public void listRegisterClan(L2Player player)
	{
		player.sendPacket(new SiegeInfo(getSiegeUnit()));
	}

	/** Один из командиров убит */
	public void killedCommander(L2CommanderInstance ct)
	{
		_commanders.remove(ct);
		operateGuardDoors(true, _commanders.size());
		if(_commanders.size() == 0)
		{
			spawnFlags();
			operateCommandCenterDoors(true);
			if(_commanderRespawnTask != null)
				_commanderRespawnTask.cancel(true);
			_commanderRespawnTask = null;
		}
		else if(_commanderRespawnTask == null)
			_commanderRespawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new CommanderRespawnTask(), 600000);
	}

	private class CommanderRespawnTask implements Runnable
	{
		public void run()
		{
			if(isInProgress())
			{
				unspawnCommanders();
				spawnCommanders();
			}
			_commanderRespawnTask = null;
		}
	}

	private void unspawnCommanders()
	{
		for(L2CommanderInstance commander : _commanders)
			if(commander != null)
				commander.deleteMe();
		_commanders.clear();
	}

	private void spawnCommanders()
	{
		for(SiegeSpawn sp : FortressSiegeManager.getCommanderSpawnList(getSiegeUnit().getId()))
		{
			L2CommanderInstance commander = new L2CommanderInstance(IdFactory.getInstance().getNextId(), NpcTable.getTemplate(sp.getNpcId()));
			commander.setCurrentHpMp(commander.getMaxHp(), commander.getMaxMp(), true);
			commander.setXYZInvisible(sp.getLoc().correctGeoZ());
			commander.setSpawnedLoc(commander.getLoc());
			commander.setHeading(sp.getLoc().h);
			commander.spawnMe();
			_commanders.add(commander);
		}
	}

	public void operateGuardDoors(boolean open, int commanders_count)
	{
		for(Entry<Integer, Integer> entry : FortressSiegeManager.getGuardDoors(getSiegeUnit().getId()).entrySet())
		{
			if(entry.getValue() < commanders_count)
				continue;
			L2DoorInstance door = getSiegeUnit().getDoor(entry.getKey());
			if(door != null)
				if(open)
					door.openMe();
				else
					door.closeMe();
		}
	}

	public void operateCommandCenterDoors(boolean open)
	{
		for(Integer doorId : FortressSiegeManager.getCommandCenterDoors(getSiegeUnit().getId()))
		{
			L2DoorInstance door = getSiegeUnit().getDoor(doorId);
			if(door != null)
				if(open)
					door.openMe();
				else
					door.closeMe();
		}
	}

	public void addFlagPole(L2StaticObjectInstance art)
	{
		_flagPoles.add(art);
	}

	private void spawnFlags()
	{
		for(SiegeSpawn sp : FortressSiegeManager.getFlagsList(getSiegeUnit().getId()))
		{
			L2ItemInstance flag = ItemTemplates.getInstance().createItem(sp.getNpcId());
			flag.setCustomFlags(L2ItemInstance.FLAG_EQUIP_ON_PICKUP | L2ItemInstance.FLAG_NO_DESTROY | L2ItemInstance.FLAG_NO_TRADE | L2ItemInstance.FLAG_NO_UNEQUIP, false);
			flag.setXYZInvisible(sp.getLoc().correctGeoZ());
			flag.spawnMe();
			_flags.add(flag);
		}
	}

	private void unSpawnFlags()
	{
		for(L2ItemInstance flag : _flags)
			if(flag != null)
			{
				if(flag.getOwnerId() > 0)
				{
					L2Player owner = L2ObjectsStorage.getPlayer(flag.getOwnerId());
					if(owner != null)
						flag = owner.getInventory().dropItem(flag, flag.getCount(), true);
				}
				flag.deleteMe();
			}
		_flags.clear();
	}

	public GArray<L2CommanderInstance> getCommanders()
	{
		return _commanders;
	}
}