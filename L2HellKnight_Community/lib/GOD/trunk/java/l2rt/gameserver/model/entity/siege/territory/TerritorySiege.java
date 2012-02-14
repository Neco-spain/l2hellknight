package l2rt.gameserver.model.entity.siege.territory;

import javolution.util.FastMap;
import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.idfactory.IdFactory;
import l2rt.gameserver.instancemanager.CastleManager;
import l2rt.gameserver.instancemanager.FortressManager;
import l2rt.gameserver.instancemanager.ServerVariables;
import l2rt.gameserver.instancemanager.ZoneManager;
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.model.entity.residence.Castle;
import l2rt.gameserver.model.entity.residence.Fortress;
import l2rt.gameserver.model.entity.siege.Siege;
import l2rt.gameserver.model.entity.siege.SiegeClan;
import l2rt.gameserver.model.entity.siege.SiegeSpawn;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.instances.L2TerritoryFlagInstance;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.tables.ClanTable;
import l2rt.gameserver.tables.NpcTable;
import l2rt.util.GArray;
import l2rt.util.Location;

import java.util.Calendar;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;

public class TerritorySiege
{
	public static final int[] TERRITORY_SKILLS = new int[] { 0, 848, 849, 850, 851, 852, 853, 854, 855, 856 };
	private static int _siegeLength = 120; // mins
	private static int _controlTowerLosePenalty = 20000;
	private static int _defenderRespawnDelay = 20000;
	private static int SiegeDayOfWeek = 7;
	private static int SiegeHourOfDay = 20;

	private static boolean _isInProgress = false;
	private static boolean _isRegistrationOver = false;

	private static Calendar _siegeDate;
	private static Calendar _siegeEndDate;
	private static Calendar _siegeRegistrationEndDate;

	private static ScheduledFuture<?> _siegeStartTask;
	private static ScheduledFuture<?> _fameTask;

	private static FastMap<Integer, Integer> _players = new FastMap<Integer, Integer>().setShared(true);
	private static FastMap<SiegeClan, Integer> _clans = new FastMap<SiegeClan, Integer>().setShared(true);
	private static FastMap<Integer, Castle> _castles = new FastMap<Integer, Castle>().setShared(true);
	private static FastMap<Integer, Fortress> _fortress = new FastMap<Integer, Fortress>().setShared(true);
	private static GArray<L2TerritoryFlagInstance> _flags = new GArray<L2TerritoryFlagInstance>();
	private static FastMap<Integer, Location> _wardsLoc = new FastMap<Integer, Location>().setShared(true);

	private static FastMap<Integer, Integer> _defenderRespawnPenalty = new FastMap<Integer, Integer>().setShared(true);

	public static void load()
	{
		_siegeDate = Calendar.getInstance();
		_siegeDate.setTimeInMillis(ServerVariables.getLong("TerritorySiegeDate", 0));

		_castles.putAll(CastleManager.getInstance().getCastles());
		_fortress.putAll(FortressManager.getInstance().getFortresses());

		TerritorySiegeDatabase.loadSiegeMembers();
		TerritorySiegeDatabase.loadSiegeFlags();

		for(int unitId : _castles.keySet())
		{
			L2Zone zone = getZone(unitId);
			if(zone != null)
				zone.setActive(false);
		}

		for(int unitId : _fortress.keySet())
		{
			L2Zone zone = getZone(unitId);
			if(zone != null)
				zone.setActive(false);
		}

		for(int unitId : _castles.keySet())
			_defenderRespawnPenalty.put(unitId, 0);

		startAutoTask();
	}

	public static FastMap<Integer, Integer> getPlayers()
	{
		return _players;
	}

	public static int getPlayersForTerritory(int territoryId)
	{
		int counter = 0;
		for(Entry<Integer, Integer> entry : _players.entrySet())
			if(entry.getValue().intValue() == territoryId)
				counter++;
		return counter;
	}

	public static int getTerritoryForPlayer(int playerId)
	{
		Integer terrId = _players.get(new Integer(playerId));
		return terrId == null ? -1 : terrId;
	}

	public static FastMap<SiegeClan, Integer> getClans()
	{
		return _clans;
	}

	public static int getClansForTerritory(int territoryId)
	{
		int counter = 0;
		for(Entry<SiegeClan, Integer> entry : _clans.entrySet())
			if(entry.getValue().intValue() == territoryId)
				counter++;
		return counter;
	}

	public static int getTerritoryForClan(int clanId)
	{
		if(clanId == 0)
			return 0;
		L2Clan clan = ClanTable.getInstance().getClan(clanId);
		if(clan == null)
			return 0;
		if(clan.getHasCastle() > 0)
			return clan.getHasCastle();
		for(Entry<SiegeClan, Integer> entry : TerritorySiege.getClans().entrySet())
			if(entry.getKey().getClanId() == clanId)
				return entry.getValue();
		return 0;
	}

	public static L2Zone getZone(int unitId)
	{
		return ZoneManager.getInstance().getZoneByIndex(ZoneType.Siege, unitId, false);
	}

	public static L2Zone getResidenseZone(int unitId)
	{
		return ZoneManager.getInstance().getZoneByIndex(ZoneType.siege_residense, unitId, false);
	}

	public static void startSiege()
	{
		if(!_isInProgress)
		{
			for(int unitId : _castles.keySet())
			{
				L2Zone zone = getZone(unitId);
				if(zone != null)
					zone.setActive(true);
				getResidenseZone(unitId).setActive(true);
			}

			for(int unitId : _fortress.keySet())
			{
				L2Zone zone = getZone(unitId);
				if(zone != null)
					zone.setActive(true);
			}

			// Кланы, владеющие замком, автоматически регистрируются за свои земли.
			for(Castle castle : _castles.values())
				if(castle.getOwner() != null)
					getClans().put(new SiegeClan(castle.getOwner().getClanId(), null), castle.getId());

			_isInProgress = true;

			playersUpdate(false);

			clearSiegeFields();

			for(Castle castle : _castles.values())
				castle.spawnDoor();

			for(Fortress fortress : _fortress.values())
				fortress.spawnDoor();

			spawnFlags(-1);

			// TODO спавн гвардов, баллисты

			// Таймер окончания осады
			_siegeEndDate = Calendar.getInstance();
			_siegeEndDate.add(Calendar.MINUTE, getSiegeLength());
			ThreadPoolManager.getInstance().scheduleGeneral(new TerritorySiegeEndTask(), 1000);

			_fameTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new TerritorySiegeFameTask(), 5 * 60 * 1000L, 5 * 60 * 1000L);

			announceToPlayer(Msg.TERRITORY_WAR_HAS_BEGUN, false);
		}
	}

	public static void endSiege()
	{
		for(int unitId : _castles.keySet())
		{
			L2Zone zone = getZone(unitId);
			if(zone != null)
				zone.setActive(false);
			getResidenseZone(unitId).setActive(false);
		}

		for(int unitId : _fortress.keySet())
		{
			L2Zone zone = getZone(unitId);
			if(zone != null)
				zone.setActive(false);
		}

		if(_isInProgress)
		{
			announceToPlayer(Msg.TERRITORY_WAR_HAS_ENDED, false);

			// Награда участников в виде exp/sp 
			for(L2Player player : L2ObjectsStorage.getAllPlayersForIterate())
				if(player != null && player.getTerritorySiege() > -1)
					player.addExpAndSp(270000, 27000, true, false);

			// Следующее сообщение должно выводиться через 10 мин после окончания осады.
			// Но поскольку территориальный чат у нас работат только во время осады, выводим сразу 
			announceToPlayer(Msg.THE_TERRITORY_WAR_CHANNEL_AND_FUNCTIONS_WILL_NOW_BE_DEACTIVATED, true);

			removeHeadquarters();

			clearSiegeFields();

			removeSiegeSummons();

			playersUpdate(true);

			saveSiege(); // Save castle specific data

			TerritorySiegeDatabase.clearSiegeMembers(); // Clear siege clan from db
			getPlayers().clear();
			getClans().clear();

			unSpawnFlags();

			// TODO деспавн гвардов, баллисты

			for(Castle castle : _castles.values())
				castle.saveFlags();

			for(Castle castle : _castles.values())
				castle.spawnDoor();

			for(Fortress fortress : _fortress.values())
				fortress.spawnDoor();

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

			_isInProgress = false;
		}
	}

	private static void startAutoTask()
	{
		if(_siegeStartTask != null)
			return;
		correctSiegeDateTime();

		System.out.println("Territory Siege: " + _siegeDate.getTime());

		_siegeRegistrationEndDate = Calendar.getInstance();
		_siegeRegistrationEndDate.setTimeInMillis(_siegeDate.getTimeInMillis());
		_siegeRegistrationEndDate.add(Calendar.DAY_OF_MONTH, -1);

		_siegeStartTask = ThreadPoolManager.getInstance().scheduleGeneral(new TerritorySiegeStartTask(), 1000);
	}

	private static void setNextSiegeDate()
	{
		if(_siegeDate.getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
		{
			_siegeDate.add(Calendar.DAY_OF_MONTH, 14); // Schedule to happen in 14 days
			if(_siegeDate.getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
				setNextSiegeDate(); // Re-run again if still in the pass
		}
	}

	private static void correctSiegeDateTime()
	{
		boolean corrected = false;
		if(_siegeDate.getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
		{
			// Since siege has past reschedule it to the next one (14 days)
			// This is usually caused by server being down
			corrected = true;
			setNextSiegeDate();
		}
		if(_siegeDate.get(Calendar.DAY_OF_WEEK) != SiegeDayOfWeek)
		{
			corrected = true;
			_siegeDate.set(Calendar.DAY_OF_WEEK, SiegeDayOfWeek);
		}
		if(_siegeDate.get(Calendar.HOUR_OF_DAY) != SiegeHourOfDay)
		{
			corrected = true;
			_siegeDate.set(Calendar.HOUR_OF_DAY, SiegeHourOfDay);
		}
		_siegeDate.set(Calendar.MINUTE, 0);
		if(corrected)
			ServerVariables.set("TerritorySiegeDate", _siegeDate.getTimeInMillis());
	}

	private static void saveSiege()
	{
		setNextSiegeDate(); // Выставляем дату следующей осады
		ServerVariables.set("TerritorySiegeDate", _siegeDate.getTimeInMillis()); // Сохраняем дату следующей осады
		startAutoTask(); // Запускаем таск для следующей осады
	}

	private static void removeSiegeSummons()
	{
		for(L2Player player : getPlayersInZone())
			for(int id : Siege.SIEGE_SUMMONS)
				if(player.getPet() != null && id == player.getPet().getNpcId())
					player.getPet().unSummon();
	}

	/**
	 * Рассылка бродкастом сообщений всем или только участникам ТВ.<br>
	 * Нельза рассылать только участникам если территориальная война не стартовала. 
	 */
	public static void announceToPlayer(SystemMessage message, boolean participantsOnly)
	{
		GArray<L2Player> players = new GArray<L2Player>();
		if(participantsOnly)
		{
			// Нет смысла перебирать всех игроков, если терриориальная война не началась
			if(!isInProgress())
				return;
			for(L2Player player : L2ObjectsStorage.getAllPlayersForIterate())
				if(player != null && player.getTerritorySiege() > -1)
					players.add(player);
		}
		else
		{
			for(L2Player player : L2ObjectsStorage.getAllPlayersForIterate())
				if(player != null)
					players.add(player);
		}
		for(L2Player player : players)
			player.sendPacket(message);
	}

	public static GArray<L2Player> getPlayersInZone()
	{
		GArray<L2Player> players = new GArray<L2Player>();
		if(!isInProgress())
			return players;
		for(int unitId : _castles.keySet())
			players.addAll(getZone(unitId).getInsidePlayers());
		for(int unitId : _fortress.keySet())
			players.addAll(getZone(unitId).getInsidePlayers());
		return players;
	}

	public static boolean checkIfInZone(L2Object object)
	{
		if(!isInProgress())
			return false;
		for(int unitId : _castles.keySet())
			for(L2Object obj : getZone(unitId).getObjects())
				if(obj == object)
					return true;
		for(int unitId : _fortress.keySet())
			for(L2Object obj : getZone(unitId).getObjects())
				if(obj == object)
					return true;
		return false;
	}

	public static SiegeClan getSiegeClan(L2Clan clan)
	{
		if(clan == null)
			return null;
		for(SiegeClan siegeClan : _clans.keySet())
			if(siegeClan.getClan() == clan)
				return siegeClan;
		return null;
	}

	private static void clearSiegeFields()
	{
		for(L2Player player : getPlayersInZone())
			if(player != null && !player.isGM())
				player.teleToClosestTown();
	}

	private static void playersUpdate(boolean end)
	{
		L2Clan clan;
		for(Entry<SiegeClan, Integer> entry : _clans.entrySet())
		{
			SiegeClan siegeClan = entry.getKey();
			clan = ClanTable.getInstance().getClan(siegeClan.getClanId());
			if(clan == null)
			{
				System.out.println("Siege clan is null!!! id: " + siegeClan.getClanId());
				continue;
			}
			if(end)
				clan.setTerritorySiege(-1);
			else
				clan.setTerritorySiege(entry.getValue());
			for(L2Player member : clan.getOnlineMembers(0))
			{
				member.broadcastUserInfo(true);
				member.broadcastRelationChanged();
			}
		}
		for(Entry<Integer, Integer> entry : _players.entrySet())
		{
			L2Player player = L2ObjectsStorage.getPlayer(entry.getKey());
			if(player != null)
			{
				if(end)
					player.setTerritorySiege(-1);
				else
					player.setTerritorySiege(entry.getValue());
				player.broadcastUserInfo(true);
				player.broadcastRelationChanged();
			}
		}
	}

	private static void removeHeadquarters()
	{
		for(SiegeClan sc : getClans().keySet())
			if(sc != null)
				sc.removeHeadquarter();
	}

	public static L2NpcInstance getHeadquarter(L2Clan clan)
	{
		if(clan != null)
		{
			SiegeClan sc = getSiegeClan(clan);
			if(sc != null)
				return sc.getHeadquarter();
		}
		return null;
	}

	public static void spawnFlags(int onlyOne)
	{
		for(Castle castle : _castles.values())
		{
			GArray<SiegeSpawn> points = TerritorySiegeDatabase.getSiegeFlags().get(castle.getId());
			int i = 0;
			for(int flagCastleId : castle.getFlags())
			{
				if(onlyOne == -1 || flagCastleId == onlyOne)
				{
					SiegeSpawn info = TerritorySiegeDatabase.getSiegeFlags().get(flagCastleId).get(0);
					Location loc = points.get(i).getLoc();

					L2TerritoryFlagInstance flag = new L2TerritoryFlagInstance(IdFactory.getInstance().getNextId(), NpcTable.getTemplate(info.getNpcId()));
					flag.setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp(), true);
					flag.setXYZInvisible(loc.correctGeoZ());
					flag.setSpawnedLoc(flag.getLoc());
					flag.setHeading(loc.h);
					flag.setItemId(info.getValue());
					flag.setBaseTerritoryId(flagCastleId);
					flag.setCurrentTerritoryId(castle.getId());
					flag.spawnMe();
					_flags.add(flag);
					setWardLoc(flagCastleId, flag.getLoc());
				}

				i++;
			}
		}
	}

	private static void unSpawnFlags()
	{
		for(L2TerritoryFlagInstance flag : _flags)
			if(flag != null)
			{
				L2ItemInstance item = flag.getItem();
				if(item != null)
				{
					if(item.getOwnerId() > 0)
					{
						L2Player owner = L2ObjectsStorage.getPlayer(item.getOwnerId());
						if(owner != null)
							item = owner.getInventory().dropItem(item, item.getCount(), true);
					}
					item.deleteMe();
				}
				flag.deleteMe();
			}
		_flags.clear();
		_wardsLoc.clear();
	}

	public static void removeFlag(L2TerritoryFlagInstance flag)
	{
		_flags.remove(flag);
	}

	public static L2TerritoryFlagInstance getNpcFlagByItemId(int itemId)
	{
		for(L2TerritoryFlagInstance flag : _flags)
			if(flag.getItemId() == itemId)
				return flag;
		return null;
	}

	public static int getSiegeLength()
	{
		return _siegeLength;
	}

	public static Calendar getSiegeDate()
	{
		return _siegeDate;
	}

	public static Calendar getSiegeEndDate()
	{
		return _siegeEndDate;
	}

	public static boolean isInProgress()
	{
		return _isInProgress;
	}

	public static void setSiegeLength(int siegeLength)
	{
		_siegeLength = siegeLength;
	}

	public static void setRegistrationOver(boolean value)
	{
		_isRegistrationOver = value;
	}

	public static boolean isRegistrationOver()
	{
		return _isRegistrationOver;
	}

	public static Calendar getSiegeRegistrationEndDate()
	{
		return _siegeRegistrationEndDate;
	}

	public static int getDefenderRespawnTotal(int unitId)
	{
		return _defenderRespawnDelay + _defenderRespawnPenalty.get(unitId);
	}

	/**
	 * Control Tower was killed
	 * Add respawn penalty to defenders for each control tower lose
	 */
	public static void killedCT(int unitId)
	{
		_defenderRespawnPenalty.put(unitId, _defenderRespawnPenalty.get(unitId) + _controlTowerLosePenalty);
	}

	/**
	 * Чат доступен за 10 мин до старта и еще 10 мин по окончании ТВ
	 */
	public static boolean isTerritoryChatAccessible()
	{
		return getSiegeDate().getTimeInMillis() - 10 * 60 * 1000 > System.currentTimeMillis() && getSiegeEndDate().getTimeInMillis() + 10 * 60 * 1000 > System.currentTimeMillis();
	}

	/**
	 * Производит обновление скилов территорий для кланов-владельцев замков
	 */
	public static void refreshTerritorySkills()
	{
		for(Castle c : CastleManager.getInstance().getCastles().values())
		{
			L2Clan owner = c.getOwner();
			if(owner == null)
				continue;

			// Удаляем лишние
			L2Skill[] clanSkills = owner.getAllSkills();
			for(L2Skill cs : clanSkills)
			{
				if(!isTerritoriSkill(cs))
					continue;
				if(!c.getTerritorySkills().contains(cs))
					owner.removeSkill(cs);
			}

			// Добавляем недостающие
			clanSkills = owner.getAllSkills();
			boolean exist;
			for(L2Skill cs : c.getTerritorySkills())
			{
				exist = false;
				for(L2Skill clanSkill : clanSkills)
				{
					if(!isTerritoriSkill(clanSkill))
						continue;
					if(clanSkill.getId() == cs.getId())
					{
						exist = true;
						break;
					}
				}
				if(!exist)
					owner.addNewSkill(cs, false);
			}
		}
	}

	/**
	 * Проверяет, является ли данный скилл скилом территорий
	 */
	private static boolean isTerritoriSkill(L2Skill skill)
	{
		for(int id : TERRITORY_SKILLS)
			if(id == skill.getId())
				return true;
		return false;
	}

	public static FastMap<Integer, Location> getWardsLoc()
	{
		return _wardsLoc;
	}

	public static void setWardLoc(Integer id, Location loc)
	{
		if(_wardsLoc.get(id) != null)
			_wardsLoc.get(id).set(loc);
		else
			_wardsLoc.put(id, loc);
	}

	public static class TerritorySiegeFameTask implements Runnable
	{
		@Override
		public void run()
		{
			if(!isInProgress())
				return;
			int bonus = 0;
			for(L2Player player : getPlayersInZone())
				if(player != null && !player.isDead() && !player.isInOfflineMode() && player.getTerritorySiege() > -1)
				{
					if(player.getClan() != null)
						player.getClan().incReputation(1, true, "TerritoryWars:" + player);
					if(player.isInZone(ZoneType.Fortress))
						bonus = 310;
					else if(player.isInZone(ZoneType.Castle))
						bonus = 1250;
					player.setFame(player.getFame() + bonus, "TerritoryWars");
					if(player.isSitting())
						continue;
					double badgesCount = 0.5;
					if(player.isInCombat())
						badgesCount += 0.5;
					L2Object target = player.getTarget();
					if(target != null && target.isPlayable())
					{
						badgesCount += 0.5;
						L2Player ptarget = target.getPlayer();
						if(ptarget != null && player.getTerritorySiege() != ptarget.getTerritorySiege() && ptarget.getTerritorySiege() > -1)
							badgesCount += 0.5;
					}
					String var = player.getVar("badges" + player.getTerritorySiege());
					int badges = 0;
					if(var != null)
						badges = Integer.parseInt(var);
					badges += badgesCount;
					player.setVar("badges" + player.getTerritorySiege(), "" + badges);
				}
		}
	}
}