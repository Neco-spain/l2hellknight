package l2rt.gameserver.model.entity.siege.castle;

import javolution.util.FastMap;
import l2rt.common.ThreadPoolManager;
import l2rt.extensions.listeners.L2ZoneEnterLeaveListener;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.instancemanager.MercTicketManager;
import l2rt.gameserver.instancemanager.SiegeGuardManager;
import l2rt.gameserver.instancemanager.SiegeManager;
import l2rt.gameserver.model.L2Clan;
import l2rt.gameserver.model.L2Object;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Zone;
import l2rt.gameserver.model.entity.residence.Residence;
import l2rt.gameserver.model.entity.siege.*;
import l2rt.gameserver.model.instances.L2ArtefactInstance;
import l2rt.gameserver.model.instances.L2ControlTowerInstance;
import l2rt.gameserver.network.serverpackets.SiegeInfo;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.tables.ClanTable;
import l2rt.gameserver.tables.MapRegion;
import l2rt.util.GArray;

import java.util.Calendar;

public class CastleSiege extends Siege
{
	private final GArray<L2ControlTowerInstance> _controlTowers = new GArray<L2ControlTowerInstance>();
	private final GArray<L2ArtefactInstance> _artifacts = new GArray<L2ArtefactInstance>();
	private final FastMap<Integer, Integer> _engrave = new FastMap<Integer, Integer>().setShared(true);

	protected TrapPacketSender trapListener = new TrapPacketSender();

	public CastleSiege(Residence castle)
	{
		super(castle);
		_database = new CastleSiegeDatabase(this);
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
					announceToPlayer(new SystemMessage(SystemMessage.THE_SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST).addString(getSiegeUnit().getName()), false, false);
				else
					announceToPlayer(new SystemMessage(SystemMessage.S1S_SIEGE_WAS_CANCELED_BECAUSE_THERE_WERE_NO_CLANS_THAT_PARTICIPATED).addString(getSiegeUnit().getName()), false, false);
				return;
			}

			// Слушатель добавляется перед активацией т.к. зона выполнит вход
			getZone().getListenerEngine().addMethodInvokedListener(trapListener);
			getZone().setActive(true);
			getResidenseZone().setActive(true);

			_isInProgress = true; // Flag so that same siege instance cannot be started again
			_isMidVictory = false;
			_ownerBeforeStart = getSiegeUnit().getOwnerId();

			updateSiegeClans();
			updatePlayerSiegeStateFlags(false);

			teleportPlayer(TeleportWhoType.Attacker, MapRegion.TeleportWhereType.ClosestTown); // Teleport to the closest town
			teleportPlayer(TeleportWhoType.Spectator, MapRegion.TeleportWhereType.ClosestTown); // Teleport to the closest town
			respawnControlTowers(); // Respawn control towers
			getSiegeUnit().spawnDoor(); // Spawn door
			getSiegeGuardManager().spawnSiegeGuard(); // Spawn siege guard
			MercTicketManager.getInstance().deleteTickets(getSiegeUnit().getId()); // remove the tickets from the ground
			_defenderRespawnPenalty = 0; // Reset respawn delay

			// Schedule a task to prepare auto siege end
			_siegeEndDate = Calendar.getInstance();
			_siegeEndDate.add(Calendar.MINUTE, getSiegeLength());
			ThreadPoolManager.getInstance().scheduleGeneral(new SiegeEndTask(this), 1000); // Prepare auto end task
			_fameTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new SiegeFameTask(), 5 * 60 * 1000L, 5 * 60 * 1000L);

			announceToPlayer(Msg.THE_TEMPORARY_ALLIANCE_OF_THE_CASTLE_ATTACKER_TEAM_IS_IN_EFFECT_IT_WILL_BE_DISSOLVED_WHEN_THE_CASTLE_LORD_IS_REPLACED, false, true);
			announceToPlayer(new SystemMessage(SystemMessage.THE_SIEGE_OF_S1_HAS_STARTED).addString(getSiegeUnit().getName()), false, false);
		}
	}

	@Override
	public void midVictory()
	{
		// Если осада закончилась
		if(!isInProgress() || getSiegeUnit().getOwnerId() <= 0)
			return;

		// Если атакуется замок, принадлежащий NPC, и только 1 атакующий - закончить осаду
		if(getDefenderClans().isEmpty() && getAttackerClans().size() == 1)
		{
			SiegeClan sc_newowner = getAttackerClan(getSiegeUnit().getOwner());
			removeSiegeClan(sc_newowner, SiegeClanType.ATTACKER);
			addSiegeClan(sc_newowner, SiegeClanType.OWNER);
			endSiege();
			return;
		}

		int allyId = ClanTable.getInstance().getClan(getSiegeUnit().getOwnerId()).getAllyId();

		// Если атакуется замок, принадлежащий NPC, и все атакующие в одном альянсе - закончить осаду
		if(allyId != 0 && getDefenderClans().isEmpty())
		{
			boolean allinsamealliance = true;
			for(SiegeClan sc : getDefenderClans().values())
				if(sc != null && sc.getClan().getAllyId() != allyId)
					allinsamealliance = false;
			if(allinsamealliance)
			{
				SiegeClan sc_newowner = getAttackerClan(getSiegeUnit().getOwner());
				removeSiegeClan(sc_newowner, SiegeClanType.ATTACKER);
				addSiegeClan(sc_newowner, SiegeClanType.OWNER);
				endSiege();
				return;
			}
		}

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

		_isMidVictory = true;

		clearSiegeClans();
		updateSiegeClans();
		updatePlayerSiegeStateFlags(false);

		announceToPlayer(Msg.THE_TEMPORARY_ALLIANCE_OF_THE_CASTLE_ATTACKER_TEAM_HAS_BEEN_DISSOLVED, false, true);

		teleportPlayer(TeleportWhoType.Attacker, MapRegion.TeleportWhereType.ClosestTown);
		teleportPlayer(TeleportWhoType.Spectator, MapRegion.TeleportWhereType.ClosestTown);

		_defenderRespawnPenalty = 0; // Reset respawn delay

		getSiegeGuardManager().unspawnSiegeGuard(); // Remove all spawned siege guard from this castle
		getSiegeUnit().removeUpgrade(); // Remove all castle upgrade
		respawnControlTowers(); // Respawn control towers
		getSiegeUnit().spawnDoor(true); // Respawn door to castle but make them weaker (50% hp)
	}

	@Override
	public void endSiege()
	{
		getZone().setActive(false);
		getZone().getListenerEngine().removeMethodInvokedListener(trapListener); // Слушаетель убирается после деактивации т.к. зона выполнит выход
		getResidenseZone().setActive(false);

		if(isInProgress())
		{
			announceToPlayer(new SystemMessage(SystemMessage.THE_SIEGE_OF_S1_HAS_FINISHED).addString(getSiegeUnit().getName()), false, false);

			if(getSiegeUnit().getOwnerId() <= 0)
				announceToPlayer(new SystemMessage(SystemMessage.THE_SIEGE_OF_S1_HAS_ENDED_IN_A_DRAW).addString(getSiegeUnit().getName()), false, false);
			else
			{
				L2Clan oldOwner = null;
				if(_ownerBeforeStart != 0)
					oldOwner = ClanTable.getInstance().getClan(_ownerBeforeStart);
				L2Clan newOwner = ClanTable.getInstance().getClan(getSiegeUnit().getOwnerId());

				if(oldOwner == null)
				{ // castle was taken over from scratch
					if(newOwner.getLevel() >= 5)
						newOwner.broadcastToOnlineMembers(new SystemMessage(SystemMessage.SINCE_YOUR_CLAN_EMERGED_VICTORIOUS_FROM_THE_SIEGE_S1_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLAN_REPUTATION_SCORE).addNumber(newOwner.incReputation(1500, true, "CastleSiege")));
					SiegeManager.clearCastleRegistrations(newOwner);
					SiegeManager.clearFortressRegistrations(newOwner); // TODO убрать
				}
				else if(newOwner.equals(oldOwner))
				{ // castle was defended
					if(newOwner.getLevel() >= 5)
						newOwner.broadcastToOnlineMembers(new SystemMessage(SystemMessage.SINCE_YOUR_CLAN_EMERGED_VICTORIOUS_FROM_THE_SIEGE_S1_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLAN_REPUTATION_SCORE).addNumber(newOwner.incReputation(1500, true, "CastleSiege")));
					SiegeManager.clearCastleRegistrations(newOwner);
					SiegeManager.clearFortressRegistrations(newOwner); // TODO убрать
				}
				else
				{ // castle was taken over by another clan
					announceToPlayer(new SystemMessage(SystemMessage.CLAN_S1_IS_VICTORIOUS_OVER_S2S_CASTLE_SIEGE).addString(newOwner.getName()).addString(getSiegeUnit().getName()), false, false);
					if(newOwner.getLevel() >= 5)
						newOwner.broadcastToOnlineMembers(new SystemMessage(SystemMessage.SINCE_YOUR_CLAN_EMERGED_VICTORIOUS_FROM_THE_SIEGE_S1_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLAN_REPUTATION_SCORE).addNumber(newOwner.incReputation(3000, true, "CastleSiege")));
					if(oldOwner.getLevel() >= 5)
						oldOwner.broadcastToOnlineMembers(new SystemMessage(SystemMessage.YOUR_CLAN_HAS_FAILED_TO_DEFEND_THE_CASTLE_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_CLAN_REPUTATION_SCORE).addNumber(-oldOwner.incReputation(-3000, true, "CastleSiege")));
					SiegeManager.clearCastleRegistrations(newOwner);
					SiegeManager.clearFortressRegistrations(newOwner); // TODO убрать
				}
			}

			removeHeadquarters();
			teleportPlayer(TeleportWhoType.Attacker, MapRegion.TeleportWhereType.ClosestTown); // Teleport to the closest town
			teleportPlayer(TeleportWhoType.Spectator, MapRegion.TeleportWhereType.ClosestTown); // Teleport to the closest town
			removeSiegeSummons();
			_isInProgress = false; // Flag so that siege instance can be started
			updatePlayerSiegeStateFlags(true);
			saveSiege(); // Save castle specific data
			_database.clearSiegeClan(); // Clear siege clan from db
			respawnControlTowers(); // Remove all control tower from this castle
			getSiegeGuardManager().unspawnSiegeGuard(); // Remove all spawned siege guard from this castle
			SiegeGuardManager.removeMercsFromDb(getSiegeUnit().getId());
			getSiegeUnit().spawnDoor(); // Respawn door to castle
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
		_engrave.put(objId, clan.getClanId());
		if(_engrave.size() >= _artifacts.size())
		{
			boolean rst = true;
			for(int id : _engrave.values())
				if(id != clan.getClanId())
					rst = false;
			if(rst)
			{
				_engrave.clear();
				getSiegeUnit().changeOwner(clan);
			}
		}
	}

	/**
	 * Start the auto tasks<BR><BR>
	 */
	@Override
	public void startAutoTask(boolean isServerStarted)
	{
		if(_siegeStartTask != null)
			return;
		
		correctSiegeDateTime();

		_siegeRegistrationEndDate = Calendar.getInstance();
		_siegeRegistrationEndDate.setTimeInMillis(_siegeDate.getTimeInMillis());
		_siegeRegistrationEndDate.add(Calendar.DAY_OF_MONTH, -1);
		
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
		
		System.out.println("Siege of " + getSiegeUnit().getName() + ": " + _siegeDate.getTime());

		_siegeStartTask = ThreadPoolManager.getInstance().scheduleGeneral(new SiegeStartTask(this), 1000);
	}

	/** Set the date for the next siege. */
	@Override
	protected void setNextSiegeDate()
	{
		if(_siegeDate.getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
		{
			// Set next siege date if siege has passed
			_siegeDate.add(Calendar.DAY_OF_MONTH, 14); // Schedule to happen in 14 days
			if(_siegeDate.getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
				setNextSiegeDate(); // Re-run again if still in the pass
		}
	}

	@Override
	protected void correctSiegeDateTime()
	{
		boolean corrected = false;
		if(_siegeDate.getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
		{
			// Since siege has past reschedule it to the next one (14 days)
			// This is usually caused by server being down
			corrected = true;
			setNextSiegeDate();
		}
		if(_siegeDate.get(Calendar.DAY_OF_WEEK) != getSiegeUnit().getSiegeDayOfWeek())
		{
			corrected = true;
			_siegeDate.set(Calendar.DAY_OF_WEEK, getSiegeUnit().getSiegeDayOfWeek());
		}
		if(_siegeDate.get(Calendar.HOUR_OF_DAY) != getSiegeUnit().getSiegeHourOfDay())
		{
			corrected = true;
			_siegeDate.set(Calendar.HOUR_OF_DAY, getSiegeUnit().getSiegeHourOfDay());
		}
		_siegeDate.set(Calendar.MINUTE, 0);
		if(corrected)
			_database.saveSiegeDate();
	}

	@Override
	protected void saveSiege()
	{
		// Выставляем дату следующей осады
		setNextSiegeDate();
		// Сохраняем дату следующей осады
		_database.saveSiegeDate();
		// Запускаем таск для следующей осады
		startAutoTask(false);
	}

	/** Display list of registered clans */
	@Override
	public void listRegisterClan(L2Player player)
	{
		player.sendPacket(new SiegeInfo(getSiegeUnit()));
	}

	/** Remove all control tower spawned. */
	private void respawnControlTowers()
	{
		// Remove all instance of control tower for this castle
		for(L2ControlTowerInstance ct : _controlTowers)
			if(ct != null)
			{
				ct.decayMe();
				ct.spawnMe();
			}
	}

	public void addControlTower(L2ControlTowerInstance tower)
	{
		_controlTowers.add(tower);
	}

	public boolean isAllTowersDead()
	{
		for(L2ControlTowerInstance ct : _controlTowers)
			if(ct != null && !ct.isFakeTower())
				return false;
		return true;
	}

	public void addArtifact(L2ArtefactInstance art)
	{
		_artifacts.add(art);
	}

	/**
	 * Обновляет статус ловушек у текущей осады.
	 * Если игрок входит(enter == true), то будет отослано состояние трэпов.
	 * Если выходит, то трэпы будут простро выключены
	 * Если осада не запущена, то трепы выключатся.
	 * @param player игрок
	 * @param enter вход или выход игрока
	 * <p>
	 * TODO: обработка
	 */
	@Override
	public void sendTrapStatus(L2Player player, boolean enter)
	{
		if(enter)
		{
			// TODO: player.sendPacket(new EventTrigger(...));
		}
		else
		{
			// TODO: player.sendPacket(new EventTrigger(...));
		}
	}

	/**
	 * Осадной зоне добавляется слушатель для входа/выхода объекта
	 */
	private class TrapPacketSender extends L2ZoneEnterLeaveListener
	{
		@Override
		public void objectEntered(L2Zone zone, L2Object object)
		{
			if(object.isPlayer())
			{
				L2Player player = (L2Player) object;
				sendTrapStatus(player, true);
				//System.out.println(player.getName() + " -> enter; " + zone);
			}
		}

		@Override
		public void objectLeaved(L2Zone zone, L2Object object)
		{
			if(object.isPlayer())
			{
				L2Player player = (L2Player) object;
				sendTrapStatus(player, false);
				//System.out.println(player.getName() + " -> exit; " + zone);
			}
		}
	}
}