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
package l2.hellknight.gameserver.model.entity;

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.procedure.TObjectProcedure;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;
import javolution.util.FastMap;

import l2.hellknight.Config;
import l2.hellknight.L2DatabaseFactory;
import l2.hellknight.gameserver.FortUpdater;
import l2.hellknight.gameserver.FortUpdater.UpdaterType;
import l2.hellknight.gameserver.ThreadPoolManager;
import l2.hellknight.gameserver.datatables.ClanTable;
import l2.hellknight.gameserver.datatables.DoorTable;
import l2.hellknight.gameserver.datatables.NpcTable;
import l2.hellknight.gameserver.datatables.SkillTable;
import l2.hellknight.gameserver.datatables.SkillTreesData;
import l2.hellknight.gameserver.datatables.SpawnTable;
import l2.hellknight.gameserver.datatables.StaticObjects;
import l2.hellknight.gameserver.instancemanager.FortManager;
import l2.hellknight.gameserver.instancemanager.ZoneManager;
import l2.hellknight.gameserver.model.L2Clan;
import l2.hellknight.gameserver.model.L2Object;
import l2.hellknight.gameserver.model.L2SkillLearn;
import l2.hellknight.gameserver.model.L2Spawn;
import l2.hellknight.gameserver.model.L2World;
import l2.hellknight.gameserver.model.actor.instance.L2DoorInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.actor.instance.L2StaticObjectInstance;
import l2.hellknight.gameserver.model.actor.templates.L2NpcTemplate;
import l2.hellknight.gameserver.model.itemcontainer.PcInventory;
import l2.hellknight.gameserver.model.skills.L2Skill;
import l2.hellknight.gameserver.model.zone.type.L2FortZone;
import l2.hellknight.gameserver.model.zone.type.L2SiegeZone;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.PlaySound;
import l2.hellknight.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;

public class Fort
{
	protected static final Logger _log = Logger.getLogger(Fort.class.getName());
	
	private int _fortId = 0;
	private List<L2DoorInstance> _doors = new ArrayList<>();
	private L2StaticObjectInstance _flagPole = null;
	private String _name = "";
	private FortSiege _siege = null;
	private Calendar _siegeDate;
	private Calendar _lastOwnedTime;
	private L2FortZone _fortZone;
	private L2SiegeZone _zone;
	private L2Clan _fortOwner = null;
	private int _fortType = 0;
	private int _state = 0;
	private int _castleId = 0;
	private int _blood = 0;
	private int _supplyLvL = 0;
	private FastMap<Integer, FortFunction> _function;
	private FastList<L2Skill> _residentialSkills = new FastList<>();
	private ScheduledFuture<?>[] _FortUpdater = new ScheduledFuture<?>[2];
	
	// Spawn Data
	private boolean _isSuspiciousMerchantSpawned = false;
	private FastList<L2Spawn> _siegeNpcs = new FastList<>();
	private FastList<L2Spawn> _npcCommanders = new FastList<>();
	private FastList<L2Spawn> _specialEnvoys = new FastList<>();
	
	private TIntIntHashMap _envoyCastles = new TIntIntHashMap(2);
	
	/** Fortress Functions */
	public static final int FUNC_TELEPORT = 1;
	public static final int FUNC_RESTORE_HP = 2;
	public static final int FUNC_RESTORE_MP = 3;
	public static final int FUNC_RESTORE_EXP = 4;
	public static final int FUNC_SUPPORT = 5;
	
	public class FortFunction
	{
		private int _type;
		private int _lvl;
		protected int _fee;
		protected int _tempFee;
		private long _rate;
		private long _endDate;
		protected boolean _inDebt;
		public boolean _cwh;
		
		public FortFunction(int type, int lvl, int lease, int tempLease, long rate, long time, boolean cwh)
		{
			_type = type;
			_lvl = lvl;
			_fee = lease;
			_tempFee = tempLease;
			_rate = rate;
			_endDate = time;
			initializeTask(cwh);
		}
		
		public int getType()
		{
			return _type;
		}
		
		public int getLvl()
		{
			return _lvl;
		}
		
		public int getLease()
		{
			return _fee;
		}
		
		public long getRate()
		{
			return _rate;
		}
		
		public long getEndTime()
		{
			return _endDate;
		}
		
		public void setLvl(int lvl)
		{
			_lvl = lvl;
		}
		
		public void setLease(int lease)
		{
			_fee = lease;
		}
		
		public void setEndTime(long time)
		{
			_endDate = time;
		}
		
		private void initializeTask(boolean cwh)
		{
			if (getOwnerClan() == null)
				return;
			long currentTime = System.currentTimeMillis();
			if (_endDate > currentTime)
				ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(cwh), _endDate - currentTime);
			else
				ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(cwh), 0);
		}
		
		private class FunctionTask implements Runnable
		{
			public FunctionTask(boolean cwh)
			{
				_cwh = cwh;
			}
			
			@Override
			public void run()
			{
				try
				{
					if (getOwnerClan() == null)
						return;
					if (getOwnerClan().getWarehouse().getAdena() >= _fee || !_cwh)
					{
						int fee = _fee;
						if (getEndTime() == -1)
							fee = _tempFee;
						
						setEndTime(System.currentTimeMillis() + getRate());
						dbSave();
						if (_cwh)
						{
							getOwnerClan().getWarehouse().destroyItemByItemId("CS_function_fee", PcInventory.ADENA_ID, fee, null, null);
							if (Config.DEBUG)
								_log.warning("Deducted " + fee + " adena from " + getName() + " owner's cwh for function id : " + getType());
						}
						ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(true), getRate());
					}
					else
						removeFunction(getType());
				}
				catch (Throwable t)
				{
				}
			}
		}
		
		public void dbSave()
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				PreparedStatement statement = con.prepareStatement("REPLACE INTO fort_functions (fort_id, type, lvl, lease, rate, endTime) VALUES (?,?,?,?,?,?)");
				statement.setInt(1, getFortId());
				statement.setInt(2, getType());
				statement.setInt(3, getLvl());
				statement.setInt(4, getLease());
				statement.setLong(5, getRate());
				statement.setLong(6, getEndTime());
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "Exception: Fort.updateFunctions(int type, int lvl, int lease, long rate, long time, boolean addNew): " + e.getMessage(), e);
			}
		}
	}
	
	public Fort(int fortId)
	{
		_fortId = fortId;
		load();
		loadFlagPoles();
		_function = new FastMap<>();
		final List<L2SkillLearn> residentialSkills = SkillTreesData.getInstance().getAvailableResidentialSkills(fortId);
		for (L2SkillLearn s : residentialSkills)
		{
			L2Skill sk = SkillTable.getInstance().getInfo(s.getSkillId(), s.getSkillLevel());
			if (sk != null)
			{
				_residentialSkills.add(sk);
			}
			else
			{
				_log.warning("Fort Id: " + fortId + " has a null residential skill Id: " + s.getSkillId() + " level: " + s.getSkillLevel() + "!");
			}
		}
		if (getOwnerClan() != null)
		{
			setVisibleFlag(true);
			loadFunctions();
		}
		initNpcs(); // load and spawn npcs (Always spawned)
		initSiegeNpcs(); // load suspicious merchants (Despawned 10mins before siege)
		//spawnSuspiciousMerchant();// spawn suspicious merchants
		initNpcCommanders(); // npc Commanders (not monsters) (Spawned during siege)
		spawnNpcCommanders(); // spawn npc Commanders
		initSpecialEnvoys(); // envoys from castles  (Spawned after fort taken)
		if (getOwnerClan() != null && getFortState() == 0)
		{
			spawnSpecialEnvoys();
			ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleSpecialEnvoysDeSpawn(this), 1 * 60 * 60 * 1000); // Prepare 1hr task for special envoys despawn
		}
	}
	
	/**
	 * Return function with id 
	 * @param type 
	 * @return
	 */
	public FortFunction getFunction(int type)
	{
		if (_function.get(type) != null)
			return _function.get(type);
		return null;
	}
	
	public static class ScheduleSpecialEnvoysDeSpawn implements Runnable
	{
		private Fort _fortInst;
		
		public ScheduleSpecialEnvoysDeSpawn(Fort pFort)
		{
			_fortInst = pFort;
		}
		
		@Override
		public void run()
		{
			try
			{
				// if state not decided, change state to indenpendent
				if (_fortInst.getFortState() == 0)
					_fortInst.setFortState(1, 0);
				_fortInst.despawnSpecialEnvoys();
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Exception: ScheduleSpecialEnvoysSpawn() for Fort " + _fortInst.getName() + ": " + e.getMessage(), e);
			}
		}
	}
	
	public void endOfSiege(L2Clan clan)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new endFortressSiege(this, clan), 1000);
	}
	
	public void engrave(L2Clan clan)
	{
		setOwner(clan, true);
	}
	
	/**
	 * Move non clan members off fort area and to nearest town.<BR><BR>
	 */
	public void banishForeigners()
	{
		getFortZone().banishForeigners(getOwnerClan().getClanId());
	}
	
	/**
	 * @param x 
	 * @param y 
	 * @param z 
	 * @return true if object is inside the zone
	 */
	public boolean checkIfInZone(int x, int y, int z)
	{
		return getZone().isInsideZone(x, y, z);
	}
	
	public L2SiegeZone getZone()
	{
		if (_zone == null)
		{
			for (L2SiegeZone zone : ZoneManager.getInstance().getAllZones(L2SiegeZone.class))
			{
				if (zone.getSiegeObjectId() == getFortId())
				{
					_zone = zone;
					break;
				}
			}
		}
		return _zone;
	}
	
	public L2FortZone getFortZone()
	{
		if (_fortZone == null)
		{
			for (L2FortZone zone : ZoneManager.getInstance().getAllZones(L2FortZone.class))
			{
				if (zone.getFortId() == getFortId())
				{
					_fortZone = zone;
					break;
				}
			}
		}
		return _fortZone;
	}
	
	/**
	 * Get the objects distance to this fort
	 * @param obj
	 * @return
	 */
	public double getDistance(L2Object obj)
	{
		return getZone().getDistanceToZone(obj);
	}
	
	public void closeDoor(L2PcInstance activeChar, int doorId)
	{
		openCloseDoor(activeChar, doorId, false);
	}
	
	public void openDoor(L2PcInstance activeChar, int doorId)
	{
		openCloseDoor(activeChar, doorId, true);
	}
	
	public void openCloseDoor(L2PcInstance activeChar, int doorId, boolean open)
	{
		if (activeChar.getClan() != getOwnerClan())
			return;
		
		L2DoorInstance door = getDoor(doorId);
		if (door != null)
		{
			if (open)
				door.openMe();
			else
				door.closeMe();
		}
	}
	
	// This method is used to begin removing all fort upgrades
	public void removeUpgrade()
	{
		removeDoorUpgrade();
	}
	
	/**
	 * This method will set owner for Fort
	 * @param clan
	 * @param updateClansReputation
	 * @return 
	 */
	public boolean setOwner(L2Clan clan, boolean updateClansReputation)
	{
		if (clan == null)
		{
			_log.warning(getClass().getSimpleName() + ": Updating Fort owner with null clan!!!");
			return false;
		}
		final L2Clan oldowner = getOwnerClan();
		if (oldowner != null && clan != oldowner)
		{
			// Remove points from old owner
			updateClansReputation(oldowner, true);
			try
			{
				L2PcInstance oldleader = oldowner.getLeader().getPlayerInstance();
				if (oldleader != null)
				{
					if (oldleader.getMountType() == 2)
						oldleader.dismount();
				}
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Exception in setOwner: " + e.getMessage(), e);
			}
			removeOwner(true);
		}
		setFortState(0, 0); // initialize fort state
		
		//	if clan already have castle, don't store him in fortress
		if (clan.getCastleId() > 0)
		{
			getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.NPCS_RECAPTURED_FORTRESS));
			return false;
		}
		
		// Give points to new owner
		if (updateClansReputation)
			updateClansReputation(clan, false);
		
		spawnSpecialEnvoys();
		ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleSpecialEnvoysDeSpawn(this), 1 * 60 * 60 * 1000); // Prepare 1hr task for special envoys despawn
		// if clan have already fortress, remove it
		if (clan.getFortId() > 0)
			FortManager.getInstance().getFortByOwner(clan).removeOwner(true);
		
		setBloodOathReward(0);
		setSupplyLvL(0);
		setOwnerClan(clan);
		updateOwnerInDB(); // Update in database
		saveFortVariables();
		
		if (getSiege().getIsInProgress()) // If siege in progress
			getSiege().endSiege();
		
		for (L2PcInstance member : clan.getOnlineMembers(0))
		{
			giveResidentialSkills(member);
			member.sendSkillList();
		}
		return true;
	}
	
	public void removeOwner(boolean updateDB)
	{
		L2Clan clan = getOwnerClan();
		if (clan != null)
		{
			for (L2PcInstance member : clan.getOnlineMembers(0))
			{
				removeResidentialSkills(member);
				member.sendSkillList();
			}
			clan.setFortId(0);
			clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
			setOwnerClan(null);
			setBloodOathReward(0);
			setSupplyLvL(0);
			saveFortVariables();
			removeAllFunctions();
			if (updateDB)
				updateOwnerInDB();
		}
	}
	
	public void setBloodOathReward(int val)
	{
		_blood = val;
	}
	
	public int getBloodOathReward()
	{
		return _blood;
	}
	
	public void raiseSupplyLvL()
	{
		_supplyLvL++;
		if (_supplyLvL > Config.FS_MAX_SUPPLY_LEVEL)
			_supplyLvL = Config.FS_MAX_SUPPLY_LEVEL;
	}
	
	public void setSupplyLvL(int val)
	{
		if (val <= Config.FS_MAX_SUPPLY_LEVEL)
			_supplyLvL = val;
	}
	
	public int getSupplyLvL()
	{
		return _supplyLvL;
	}
	
	public void saveFortVariables()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE fort SET blood=?, supplyLvL=? WHERE id = ?");
			statement.setInt(1, _blood);
			statement.setInt(2, _supplyLvL);
			statement.setInt(3, getFortId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception: saveFortVariables(): " + e.getMessage(), e);
		}
	}
	
	/**
	 * Show or hide flag inside flagpole<BR><BR>
	 * @param val 
	 */
	public void setVisibleFlag(boolean val)
	{
		L2StaticObjectInstance flagPole = getFlagPole();
		if (flagPole != null)
			flagPole.setMeshIndex(val ? 1 : 0);
	}
	
	/**
	 * Respawn all doors on fort grounds<BR><BR>
	 */
	public void resetDoors()
	{
		for (L2DoorInstance door : _doors)
		{
			if (door.getOpen())
				door.closeMe();
			if (door.isDead())
				door.doRevive();
			if (door.getCurrentHp() < door.getMaxHp())
				door.setCurrentHp(door.getMaxHp());
		}
		loadDoorUpgrade(); // Check for any upgrade the doors may have
	}
	
	// This method upgrade door
	public void upgradeDoor(int doorId, int hp, int pDef, int mDef)
	{
		L2DoorInstance door = getDoor(doorId);
		if (door != null && door.getDoorId() == doorId)
		{
			door.setCurrentHp(door.getMaxHp() + hp);
			
			saveDoorUpgrade(doorId, hp, pDef, mDef);
			return;
		}
	}
	
	// This method loads fort
	private void load()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM fort WHERE id = ?");
			statement.setInt(1, getFortId());
			ResultSet rs = statement.executeQuery();
			int ownerId = 0;
			
			while (rs.next())
			{
				_name = rs.getString("name");
				
				_siegeDate = Calendar.getInstance();
				_lastOwnedTime = Calendar.getInstance();
				_siegeDate.setTimeInMillis(rs.getLong("siegeDate"));
				_lastOwnedTime.setTimeInMillis(rs.getLong("lastOwnedTime"));
				ownerId = rs.getInt("owner");
				_fortType = rs.getInt("fortType");
				_state = rs.getInt("state");
				_castleId = rs.getInt("castleId");
				_blood = rs.getInt("blood");
				_supplyLvL = rs.getInt("supplyLvL");
			}
			
			rs.close();
			statement.close();
			
			if (ownerId > 0)
			{
				L2Clan clan = ClanTable.getInstance().getClan(ownerId); // Try to find clan instance
				clan.setFortId(getFortId());
				setOwnerClan(clan);
				int runCount = getOwnedTime() / (Config.FS_UPDATE_FRQ * 60);
				long initial = System.currentTimeMillis() - _lastOwnedTime.getTimeInMillis();
				while (initial > (Config.FS_UPDATE_FRQ * 60000L))
					initial -= (Config.FS_UPDATE_FRQ * 60000L);
				initial = (Config.FS_UPDATE_FRQ * 60000L) - initial;
				if (Config.FS_MAX_OWN_TIME <= 0 || getOwnedTime() < Config.FS_MAX_OWN_TIME * 3600)
				{
					_FortUpdater[0] = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new FortUpdater(this, clan, runCount, UpdaterType.PERIODIC_UPDATE), initial, Config.FS_UPDATE_FRQ * 60000L); // Schedule owner tasks to start running
					if (Config.FS_MAX_OWN_TIME > 0)
						_FortUpdater[1] = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new FortUpdater(this, clan, runCount, UpdaterType.MAX_OWN_TIME), 3600000, 3600000); // Schedule owner tasks to remove owener
				}
				else
					_FortUpdater[1] = ThreadPoolManager.getInstance().scheduleGeneral(new FortUpdater(this, clan, 0, UpdaterType.MAX_OWN_TIME), 60000); // Schedule owner tasks to remove owner
			}
			else
				setOwnerClan(null);
			
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception: loadFortData(): " + e.getMessage(), e);
		}
	}
	
	/** Load All Functions */
	private void loadFunctions()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement;
			ResultSet rs;
			statement = con.prepareStatement("SELECT * FROM fort_functions WHERE fort_id = ?");
			statement.setInt(1, getFortId());
			rs = statement.executeQuery();
			while (rs.next())
			{
				_function.put(rs.getInt("type"), new FortFunction(rs.getInt("type"), rs.getInt("lvl"), rs.getInt("lease"), 0, rs.getLong("rate"), rs.getLong("endTime"), true));
			}
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Exception: Fort.loadFunctions(): " + e.getMessage(), e);
		}
	}
	
	/**
	 * Remove function In List and in DB 
	 * @param functionType
	 */
	public void removeFunction(int functionType)
	{
		_function.remove(functionType);
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("DELETE FROM fort_functions WHERE fort_id=? AND type=?");
			statement.setInt(1, getFortId());
			statement.setInt(2, functionType);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Exception: Fort.removeFunctions(int functionType): " + e.getMessage(), e);
		}
	}
	
	/**
	 * Remove all fort functions.
	 */
	private void removeAllFunctions()
	{
		for (int id : _function.keySet())
		{
			removeFunction(id);
		}
	}
	
	public boolean updateFunctions(L2PcInstance player, int type, int lvl, int lease, long rate, boolean addNew)
	{
		if (player == null)
			return false;
		if (Config.DEBUG)
			_log.warning("Called Fort.updateFunctions(int type, int lvl, int lease, long rate, boolean addNew) Owner : " + getOwnerClan());
		if (lease > 0)
			if (!player.destroyItemByItemId("Consume", PcInventory.ADENA_ID, lease, null, true))
				return false;
		if (addNew)
		{
			_function.put(type, new FortFunction(type, lvl, lease, 0, rate, 0, false));
		}
		else
		{
			if (lvl == 0 && lease == 0)
				removeFunction(type);
			else
			{
				int diffLease = lease - _function.get(type).getLease();
				if (Config.DEBUG)
					_log.warning("Called Fort.updateFunctions diffLease : " + diffLease);
				if (diffLease > 0)
				{
					_function.remove(type);
					_function.put(type, new FortFunction(type, lvl, lease, 0, rate, -1, false));
				}
				else
				{
					_function.get(type).setLease(lease);
					_function.get(type).setLvl(lvl);
					_function.get(type).dbSave();
				}
			}
		}
		return true;
	}
	
	public void activateInstance()
	{
		loadDoor();
	}
	
	// This method loads fort door data from database
	private void loadDoor()
	{
		for (L2DoorInstance door : DoorTable.getInstance().getDoors())
		{
			if (door.getFort() != null && door.getFort().getFortId() == getFortId())
				_doors.add(door);
		}
		if (Config.DEBUG)
			_log.info("Fort "+this+" loaded "+_doors.size()+" doors.");
	}
	
	private void loadFlagPoles()
	{
		for(L2StaticObjectInstance obj : StaticObjects.getInstance().getStaticObjects())
		{
			if (obj.getType() == 3 && obj.getName().startsWith(_name))
			{
				_flagPole = obj;
				break;
			}
		}
		if (_flagPole == null)
		{
			throw new NullPointerException("Can't find flagpole for Fort " + this);
		}
	}
	
	// This method loads fort door upgrade data from database
	private void loadDoorUpgrade()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM fort_doorupgrade WHERE fortId = ?");
			statement.setInt(1, getFortId());
			ResultSet rs = statement.executeQuery();
			
			while (rs.next())
			{
				upgradeDoor(rs.getInt("id"), rs.getInt("hp"), rs.getInt("pDef"), rs.getInt("mDef"));
			}
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception: loadFortDoorUpgrade(): " + e.getMessage(), e);
		}
	}
	
	private void removeDoorUpgrade()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("DELETE FROM fort_doorupgrade WHERE WHERE fortId = ?");
			statement.setInt(1, getFortId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception: removeDoorUpgrade(): " + e.getMessage(), e);
		}
	}
	
	private void saveDoorUpgrade(int doorId, int hp, int pDef, int mDef)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("INSERT INTO fort_doorupgrade (doorId, hp, pDef, mDef) VALUES (?,?,?,?)");
			statement.setInt(1, doorId);
			statement.setInt(2, hp);
			statement.setInt(3, pDef);
			statement.setInt(4, mDef);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception: saveDoorUpgrade(int doorId, int hp, int pDef, int mDef): " + e.getMessage(), e);
		}
	}
	
	private void updateOwnerInDB()
	{
		L2Clan clan = getOwnerClan();
		int clanId = 0;
		if (clan != null)
		{
			clanId = clan.getClanId();
			_lastOwnedTime.setTimeInMillis(System.currentTimeMillis());
		}
		else
			_lastOwnedTime.setTimeInMillis(0);
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE fort SET owner=?,lastOwnedTime=?,state=?,castleId=?,blood=? WHERE id = ?");
			statement.setInt(1, clanId);
			statement.setLong(2, _lastOwnedTime.getTimeInMillis());
			statement.setInt(3, 0);
			statement.setInt(4, 0);
			statement.setInt(5, getBloodOathReward());
			statement.setInt(6, getFortId());
			statement.execute();
			statement.close();
			
			// Announce to clan members
			if (clan != null)
			{
				clan.setFortId(getFortId()); // Set has fort flag for new owner
				SystemMessage sm;
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CLAN_IS_VICTORIOUS_IN_THE_FORTRESS_BATTLE_OF_S2);
				sm.addString(clan.getName());
				sm.addFortId(getFortId());
				L2World.getInstance().forEachPlayer(new ForEachPlayerSendMessage(sm));
				clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
				clan.broadcastToOnlineMembers(new PlaySound(1, "Siege_Victory", 0, 0, 0, 0, 0));
				if (_FortUpdater[0] != null)
					_FortUpdater[0].cancel(false);
				if (_FortUpdater[1] != null)
					_FortUpdater[1].cancel(false);
				_FortUpdater[0] = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new FortUpdater(this, clan, 0, UpdaterType.PERIODIC_UPDATE), Config.FS_UPDATE_FRQ * 60000L, Config.FS_UPDATE_FRQ * 60000L); // Schedule owner tasks to start running
				if (Config.FS_MAX_OWN_TIME > 0)
					_FortUpdater[1] = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new FortUpdater(this, clan, 0, UpdaterType.MAX_OWN_TIME), 3600000, 3600000); // Schedule owner tasks to remove owener
			}
			else
			{
				if (_FortUpdater[0] != null)
					_FortUpdater[0].cancel(false);
				_FortUpdater[0] = null;
				if (_FortUpdater[1] != null)
					_FortUpdater[1].cancel(false);
				_FortUpdater[1] = null;
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception: updateOwnerInDB(L2Clan clan): " + e.getMessage(), e);
		}
	}
	
	public final int getFortId()
	{
		return _fortId;
	}
	
	public final L2Clan getOwnerClan()
	{
		return _fortOwner;
	}
	
	public final void setOwnerClan(L2Clan clan)
	{
		setVisibleFlag(clan != null ? true : false);
		_fortOwner = clan;
	}
	
	public final L2DoorInstance getDoor(int doorId)
	{
		if (doorId <= 0)
			return null;
		
		for (L2DoorInstance door : getDoors())
		{
			if (door.getDoorId() == doorId)
				return door;
		}
		return null;
	}
	
	public final List<L2DoorInstance> getDoors()
	{
		return _doors;
	}
	
	public final L2StaticObjectInstance getFlagPole()
	{
		return _flagPole;
	}
	
	public final FortSiege getSiege()
	{
		if (_siege == null)
			_siege = new FortSiege(this);
		return _siege;
	}
	
	public final Calendar getSiegeDate()
	{
		return _siegeDate;
	}
	
	public final void setSiegeDate(Calendar siegeDate)
	{
		_siegeDate = siegeDate;
	}
	
	public final int getOwnedTime()
	{
		if (_lastOwnedTime.getTimeInMillis() == 0)
			return 0;
		
		return (int) ((System.currentTimeMillis() - _lastOwnedTime.getTimeInMillis()) / 1000);
	}
	
	public final int getTimeTillRebelArmy()
	{
		if (_lastOwnedTime.getTimeInMillis() == 0)
			return 0;
		
		return (int) ((_lastOwnedTime.getTimeInMillis() + Config.FS_MAX_OWN_TIME * 3600000L - System.currentTimeMillis()) / 1000L);
	}
	
	public final long getTimeTillNextFortUpdate()
	{
		if (_FortUpdater[0] == null)
			return 0;
		return _FortUpdater[0].getDelay(TimeUnit.SECONDS);
	}
	
	public final String getName()
	{
		return _name;
	}
	
	public void updateClansReputation(L2Clan owner, boolean removePoints)
	{
		if (owner != null)
		{
			if (removePoints)
				owner.takeReputationScore(Config.LOOSE_FORT_POINTS, true);
			else
				owner.addReputationScore(Config.TAKE_FORT_POINTS, true);
		}
	}
	
	private static class endFortressSiege implements Runnable
	{
		private Fort _f;
		private L2Clan _clan;
		
		public endFortressSiege(Fort f, L2Clan clan)
		{
			_f = f;
			_clan = clan;
		}
		
		@Override
		public void run()
		{
			try
			{
				_f.engrave(_clan);
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Exception in endFortressSiege " + e.getMessage(), e);
			}
		}
		
	}
	
	/**
	 * @return Returns state of fortress.<BR><BR>
	 * 0 - not decided yet<BR>
	 * 1 - independent<BR>
	 * 2 - contracted with castle<BR>
	 */
	public final int getFortState()
	{
		return _state;
	}
	
	/**
	 * @param state
	 * <ul>
	 * 	<li>0 - not decided yet</li>
	 * 	<li>1 - independent</li>
	 * 	<li>2 - contracted with castle</li>
	 * </ul>
	 * @param castleId set Castle Id for contracted fort
	 */
	public final void setFortState(int state, int castleId)
	{
		_state = state;
		_castleId = castleId;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE fort SET state=?,castleId=? WHERE id = ?");
			statement.setInt(1, getFortState());
			statement.setInt(2, getCastleId());
			statement.setInt(3, getFortId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception: setFortState(int state, int castleId): " + e.getMessage(), e);
		}
	}
	
	/**
	 * @return Returns Castle Id of fortress contracted with castle.
	 */
	public final int getCastleId()
	{
		return _castleId;
	}
	
	/**
	 * @return Returns fortress type.<BR><BR>
	 * 0 - small (3 commanders) <BR>
	 * 1 - big (4 commanders + control room)
	 */
	public final int getFortType()
	{
		return _fortType;
	}
	
	public final int getCastleIdFromEnvoy(int npcId)
	{
		return _envoyCastles.get(npcId);
	}
	
	/**
	 * @return Returns amount of barracks.
	 */
	public final int getFortSize()
	{
		return getFortType() == 0 ? 3 : 5;
	}
	
	public void spawnSuspiciousMerchant()
	{
		if (_isSuspiciousMerchantSpawned)
			return;
		_isSuspiciousMerchantSpawned = true;
		
		for (L2Spawn spawnDat : _siegeNpcs)
		{
			spawnDat.doSpawn();
			spawnDat.startRespawn();
		}
	}
	
	public void despawnSuspiciousMerchant()
	{
		if (!_isSuspiciousMerchantSpawned)
			return;
		_isSuspiciousMerchantSpawned = false;
		
		for (L2Spawn spawnDat : _siegeNpcs)
		{
			spawnDat.stopRespawn();
			spawnDat.getLastSpawn().deleteMe();
		}
	}
	
	public void spawnNpcCommanders()
	{
		for (L2Spawn spawnDat : _npcCommanders)
		{
			spawnDat.doSpawn();
			spawnDat.startRespawn();
		}
	}
	
	public void despawnNpcCommanders()
	{
		for (L2Spawn spawnDat : _npcCommanders)
		{
			spawnDat.stopRespawn();
			spawnDat.getLastSpawn().deleteMe();
		}
	}
	
	public void spawnSpecialEnvoys()
	{
		for (L2Spawn spawnDat : _specialEnvoys)
		{
			spawnDat.doSpawn();
			spawnDat.startRespawn();
		}
	}
	
	public void despawnSpecialEnvoys()
	{
		for (L2Spawn spawnDat : _specialEnvoys)
		{
			spawnDat.stopRespawn();
			spawnDat.getLastSpawn().deleteMe();
		}
	}
	
	private void initNpcs()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM fort_spawnlist WHERE fortId = ? AND spawnType = ? ");
			statement.setInt(1, getFortId());
			statement.setInt(2, 0);
			ResultSet rset = statement.executeQuery();
			
			L2Spawn spawnDat;
			L2NpcTemplate template;
			
			while (rset.next())
			{
				template = NpcTable.getInstance().getTemplate(rset.getInt("npcId"));
				if (template != null)
				{
					spawnDat = new L2Spawn(template);
					spawnDat.setAmount(1);
					spawnDat.setLocx(rset.getInt("x"));
					spawnDat.setLocy(rset.getInt("y"));
					spawnDat.setLocz(rset.getInt("z"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(60);
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					spawnDat.doSpawn();
					spawnDat.startRespawn();
				}
				else
				{
					_log.warning("Fort " + getFortId() + " initNpcs: Data missing in NPC table for ID: " + rset.getInt("npcId") + ".");
				}
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			_log.log(Level.WARNING, "Fort " + getFortId() + " initNpcs: Spawn could not be initialized: " + e.getMessage(), e);
		}
	}
	
	private void initSiegeNpcs()
	{
		_siegeNpcs.clear();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT id, npcId, x, y, z, heading FROM fort_spawnlist WHERE fortId = ? AND spawnType = ? ORDER BY id");
			statement.setInt(1, getFortId());
			statement.setInt(2, 2);
			ResultSet rset = statement.executeQuery();
			
			L2Spawn spawnDat;
			L2NpcTemplate template;
			while (rset.next())
			{
				template = NpcTable.getInstance().getTemplate(rset.getInt("npcId"));
				if (template != null)
				{
					spawnDat = new L2Spawn(template);
					spawnDat.setAmount(1);
					spawnDat.setLocx(rset.getInt("x"));
					spawnDat.setLocy(rset.getInt("y"));
					spawnDat.setLocz(rset.getInt("z"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(60);
					_siegeNpcs.add(spawnDat);
				}
				else
				{
					_log.warning("Fort " + getFortId() + " initSiegeNpcs: Data missing in NPC table for ID: " + rset.getInt("npcId") + ".");
				}
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			_log.log(Level.WARNING, "Fort " + getFortId() + " initSiegeNpcs: Spawn could not be initialized: " + e.getMessage(), e);
		}
	}
	
	private void initNpcCommanders()
	{
		_npcCommanders.clear();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT id, npcId, x, y, z, heading FROM fort_spawnlist WHERE fortId = ? AND spawnType = ? ORDER BY id");
			statement.setInt(1, getFortId());
			statement.setInt(2, 1);
			ResultSet rset = statement.executeQuery();
			
			L2Spawn spawnDat;
			L2NpcTemplate template;
			while (rset.next())
			{
				template = NpcTable.getInstance().getTemplate(rset.getInt("npcId"));
				if (template != null)
				{
					spawnDat = new L2Spawn(template);
					spawnDat.setAmount(1);
					spawnDat.setLocx(rset.getInt("x"));
					spawnDat.setLocy(rset.getInt("y"));
					spawnDat.setLocz(rset.getInt("z"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(60);
					_npcCommanders.add(spawnDat);
				}
				else
				{
					_log.warning("Fort " + getFortId() + " initNpcCommanders: Data missing in NPC table for ID: " + rset.getInt("npcId")
							+ ".");
				}
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			_log.log(Level.WARNING, "Fort " + getFortId() + " initNpcCommanders: Spawn could not be initialized: " + e.getMessage(), e);
		}
	}
	
	private void initSpecialEnvoys()
	{
		_specialEnvoys.clear();
		_envoyCastles.clear();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT id, npcId, x, y, z, heading, castleId FROM fort_spawnlist WHERE fortId = ? AND spawnType = ? ORDER BY id");
			statement.setInt(1, getFortId());
			statement.setInt(2, 3);
			ResultSet rset = statement.executeQuery();
			
			L2Spawn spawnDat;
			L2NpcTemplate template;
			while (rset.next())
			{
				int castleId = rset.getInt("castleId");
				int npcId = rset.getInt("npcId");
				template = NpcTable.getInstance().getTemplate(npcId);
				if (template != null)
				{
					spawnDat = new L2Spawn(template);
					spawnDat.setAmount(1);
					spawnDat.setLocx(rset.getInt("x"));
					spawnDat.setLocy(rset.getInt("y"));
					spawnDat.setLocz(rset.getInt("z"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(60);
					_specialEnvoys.add(spawnDat);
					_envoyCastles.put(npcId, castleId);
				}
				else
				{
					_log.warning("Fort " + getFortId() + " initSpecialEnvoys: Data missing in NPC table for ID: " + rset.getInt("npcId")
							+ ".");
				}
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			_log.log(Level.WARNING, "Fort " + getFortId() + " initSpecialEnvoys: Spawn could not be initialized: " + e.getMessage(), e);
		}
	}
	
	public FastList<L2Skill> getResidentialSkills()
	{
		return _residentialSkills;
	}
	
	public void giveResidentialSkills(L2PcInstance player)
	{
		if (_residentialSkills != null && !_residentialSkills.isEmpty())
		{
			for (L2Skill sk : _residentialSkills)
				player.addSkill(sk, false);
		}
	}
	
	public void removeResidentialSkills(L2PcInstance player)
	{
		if (_residentialSkills != null && !_residentialSkills.isEmpty())
		{
			for (L2Skill sk : _residentialSkills)
				player.removeSkill(sk, false, true);
		}
	}
	
	private final class ForEachPlayerSendMessage implements TObjectProcedure<L2PcInstance>
	{
		SystemMessage _sm;
		
		protected ForEachPlayerSendMessage(SystemMessage sm)
		{
			_sm = sm;
		}
		
		@Override
		public final boolean execute(final L2PcInstance character)
		{
			character.sendPacket(_sm);
			return true;
		}
	}
	
	@Override
	public String toString()
	{
		return _name + "(" + _fortId + ")";
	}
}
