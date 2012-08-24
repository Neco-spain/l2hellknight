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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;

import l2.hellknight.Config;
import l2.hellknight.L2DatabaseFactory;
import l2.hellknight.gameserver.ThreadPoolManager;
import l2.hellknight.gameserver.datatables.ClanTable;
import l2.hellknight.gameserver.datatables.NpcTable;
import l2.hellknight.gameserver.instancemanager.FortManager;
import l2.hellknight.gameserver.instancemanager.FortSiegeGuardManager;
import l2.hellknight.gameserver.instancemanager.FortSiegeManager;
import l2.hellknight.gameserver.instancemanager.FortSiegeManager.SiegeSpawn;
import l2.hellknight.gameserver.instancemanager.MapRegionManager;
import l2.hellknight.gameserver.model.CombatFlag;
import l2.hellknight.gameserver.model.L2Clan;
import l2.hellknight.gameserver.model.L2Object;
import l2.hellknight.gameserver.model.L2SiegeClan;
import l2.hellknight.gameserver.model.L2SiegeClan.SiegeClanType;
import l2.hellknight.gameserver.model.L2Spawn;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2DoorInstance;
import l2.hellknight.gameserver.model.actor.instance.L2FortCommanderInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.actor.templates.L2NpcTemplate;
import l2.hellknight.gameserver.network.NpcStringId;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.NpcSay;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;
import l2.hellknight.gameserver.scripting.scriptengine.events.FortSiegeEvent;
import l2.hellknight.gameserver.scripting.scriptengine.impl.L2Script.EventStage;
import l2.hellknight.gameserver.scripting.scriptengine.listeners.events.FortSiegeListener;

public class FortSiege implements Siegable
{
	protected static final Logger _log = Logger.getLogger(FortSiege.class.getName());
	
	private static FastList<FortSiegeListener> fortSiegeListeners = new FastList<FortSiegeListener>().shared();

	public static enum TeleportWhoType
	{
		All, Attacker, Owner,
	}
	
	// SQL
	private static final String DELETE_FORT_SIEGECLANS_BY_CLAN_ID = "DELETE FROM fortsiege_clans WHERE fort_id = ? AND clan_id = ?";
	private static final String DELETE_FORT_SIEGECLANS = "DELETE FROM fortsiege_clans WHERE fort_id = ?";
	
	public class ScheduleEndSiegeTask implements Runnable
	{
		@Override
		public void run()
		{
			if (!getIsInProgress())
				return;
			
			try
			{
				_siegeEnd = null;
				FortSiege.this.endSiege();
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Exception: ScheduleEndSiegeTask() for Fort: " + FortSiege.this._fort.getName() + " " + e.getMessage(), e);
			}
		}
	}
	
	public class ScheduleStartSiegeTask implements Runnable
	{
		private final Fort _fortInst;
		private final int _time;
		
		public ScheduleStartSiegeTask(int time)
		{
			_fortInst = FortSiege.this._fort;
			_time = time;
		}
		
		@Override
		public void run()
		{
			if (getIsInProgress())
				return;
			
			try
			{
				final SystemMessage sm;
				if (_time == 3600) // 1hr remains
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(600), 3000000); // Prepare task for 10 minutes left.
				}
				else if (_time == 600) // 10min remains
				{
					getFort().despawnSuspiciousMerchant();
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_THE_FORTRESS_BATTLE_STARTS);
					sm.addNumber(10);
					announceToPlayer(sm);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(300), 300000); // Prepare task for 5 minutes left.
				}
				else if (_time == 300) // 5min remains
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_THE_FORTRESS_BATTLE_STARTS);
					sm.addNumber(5);
					announceToPlayer(sm);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(60), 240000); // Prepare task for 1 minute left.
				}
				else if (_time == 60) // 1min remains
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_THE_FORTRESS_BATTLE_STARTS);
					sm.addNumber(1);
					announceToPlayer(sm);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(30), 30000); // Prepare task for 30 seconds left.
				}
				else if (_time == 30) // 30seconds remains
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_SECONDS_UNTIL_THE_FORTRESS_BATTLE_STARTS);
					sm.addNumber(30);
					announceToPlayer(sm);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(10), 20000); // Prepare task for 10 seconds left.
				}
				else if (_time == 10) // 10seconds remains
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_SECONDS_UNTIL_THE_FORTRESS_BATTLE_STARTS);
					sm.addNumber(10);
					announceToPlayer(sm);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(5), 5000); // Prepare task for 5 seconds left.
				}
				else if (_time == 5) // 5seconds remains
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_SECONDS_UNTIL_THE_FORTRESS_BATTLE_STARTS);
					sm.addNumber(5);
					announceToPlayer(sm);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(1), 4000); // Prepare task for 1 seconds left.
				}
				else if (_time == 1) // 1seconds remains
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_SECONDS_UNTIL_THE_FORTRESS_BATTLE_STARTS);
					sm.addNumber(1);
					announceToPlayer(sm);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(0), 1000); // Prepare task start siege.
				}
				else if (_time == 0)// start siege
				{
					_fortInst.getSiege().startSiege();
				}
				else
					_log.warning("Exception: ScheduleStartSiegeTask(): unknown siege time: " + String.valueOf(_time));
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Exception: ScheduleStartSiegeTask() for Fort: " + _fortInst.getName() + " " + e.getMessage(), e);
			}
		}
	}
	
	public class ScheduleSuspiciousMerchantSpawn implements Runnable
	{
		@Override
		public void run()
		{
			if (getIsInProgress())
				return;
			
			try
			{
				FortSiege.this._fort.spawnSuspiciousMerchant();
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Exception: ScheduleSuspicoiusMerchantSpawn() for Fort: " + FortSiege.this._fort.getName() + " " + e.getMessage(), e);
			}
		}
	}
	
	public class ScheduleSiegeRestore implements Runnable
	{
		@Override
		public void run()
		{
			if (!getIsInProgress())
				return;
			
			try
			{
				_siegeRestore = null;
				FortSiege.this.resetSiege();
				announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.BARRACKS_FUNCTION_RESTORED));
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Exception: ScheduleSiegeRestore() for Fort: " + FortSiege.this._fort.getName() + " " + e.getMessage(), e);
			}
		}
	}
	
	private List<L2SiegeClan> _attackerClans = new FastList<>();
	
	// Fort setting
	protected FastList<L2Spawn> _commanders = new FastList<>();
	protected final Fort _fort;
	private boolean _isInProgress = false;
	private FortSiegeGuardManager _siegeGuardManager;
	ScheduledFuture<?> _siegeEnd = null;
	ScheduledFuture<?> _siegeRestore = null;
	ScheduledFuture<?> _siegeStartTask = null;
	
	public FortSiege(Fort fort)
	{
		_fort = fort;
		
		checkAutoTask();
		FortSiegeManager.getInstance().addSiege(this);
	}
	
	/**
	 * When siege ends<BR><BR>
	 */
	@Override
	public void endSiege()
	{
		if (getIsInProgress())
		{
			_isInProgress = false; // Flag so that siege instance can be started
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_FORTRESS_BATTLE_OF_S1_HAS_FINISHED);
			sm.addFortId(getFort().getFortId());
			announceToPlayer(sm);
			
			removeFlags(); // Removes all flags. Note: Remove flag before teleporting players
			unSpawnFlags();
			
			updatePlayerSiegeStateFlags(true);
			
			int ownerId = -1;
			if (getFort().getOwnerClan() != null)
				ownerId = getFort().getOwnerClan().getClanId();
			getFort().getZone().banishForeigners(ownerId);
			getFort().getZone().setIsActive(false);
			getFort().getZone().updateZoneStatusForCharactersInside();
			getFort().getZone().setSiegeInstance(null);
			
			saveFortSiege(); // Save fort specific data
			clearSiegeClan(); // Clear siege clan from db
			removeCommanders(); // Remove commander from this fort
			
			getFort().spawnNpcCommanders(); // Spawn NPC commanders
			getSiegeGuardManager().unspawnSiegeGuard(); // Remove all spawned siege guard from this fort
			getFort().resetDoors(); // Respawn door to fort
			
			ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleSuspiciousMerchantSpawn(), FortSiegeManager.getInstance().getSuspiciousMerchantRespawnDelay() * 60 * 1000L); // Prepare 3hr task for suspicious merchant respawn
			setSiegeDateTime(true); // store suspicious merchant spawn in DB
			
			if (_siegeEnd != null)
			{
				_siegeEnd.cancel(true);
				_siegeEnd = null;
			}
			if (_siegeRestore != null)
			{
				_siegeRestore.cancel(true);
				_siegeRestore = null;
			}
			
			if (getFort().getOwnerClan() != null && getFort().getFlagPole().getMeshIndex() == 0)
				getFort().setVisibleFlag(true);
			
			_log.info("Siege of " + getFort().getName() + " fort finished.");
			fireFortSiegeEventListeners(EventStage.END);
		}
	}
	
	/**
	 * When siege starts<BR><BR>
	 */
	@Override
	public void startSiege()
	{
		if (!getIsInProgress())
		{
			if (!fireFortSiegeEventListeners(EventStage.START))
			{
				return;
			}
			if (_siegeStartTask != null) // used admin command "admin_startfortsiege"
			{
				_siegeStartTask.cancel(true);
				getFort().despawnSuspiciousMerchant();
			}
			_siegeStartTask = null;
			
			if (getAttackerClans().isEmpty())
				return;
			
			_isInProgress = true; // Flag so that same siege instance cannot be started again
			
			loadSiegeClan(); // Load siege clan from db
			updatePlayerSiegeStateFlags(false);
			teleportPlayer(FortSiege.TeleportWhoType.Attacker, MapRegionManager.TeleportWhereType.Town); // Teleport to the closest town
			
			getFort().despawnNpcCommanders(); // Despawn NPC commanders
			spawnCommanders(); // Spawn commanders
			getFort().resetDoors(); // Spawn door
			spawnSiegeGuard(); // Spawn siege guard
			getFort().setVisibleFlag(false);
			getFort().getZone().setSiegeInstance(this);
			getFort().getZone().setIsActive(true);
			getFort().getZone().updateZoneStatusForCharactersInside();
			
			// Schedule a task to prepare auto siege end
			_siegeEnd = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(), FortSiegeManager.getInstance().getSiegeLength() * 60 * 1000L); // Prepare auto end task
			
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_FORTRESS_BATTLE_S1_HAS_BEGUN);
			sm.addFortId(getFort().getFortId());
			announceToPlayer(sm);
			saveFortSiege();
			
			_log.info("Siege of " + getFort().getName() + " fort started.");
		}
	}
	
	/**
	 * Announce to player.<BR><BR>
	 * @param sm the system message to send to player
	 */
	public void announceToPlayer(SystemMessage sm)
	{
		// announce messages only for participants
		L2Clan clan;
		for (L2SiegeClan siegeclan : getAttackerClans())
		{
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			for (L2PcInstance member : clan.getOnlineMembers(0))
			{
				if (member != null)
					member.sendPacket(sm);
			}
		}
		if (getFort().getOwnerClan() != null)
		{
			clan = ClanTable.getInstance().getClan(getFort().getOwnerClan().getClanId());
			for (L2PcInstance member : clan.getOnlineMembers(0))
			{
				if (member != null)
					member.sendPacket(sm);
			}
		}
	}
	
	public void announceToPlayer(SystemMessage sm, String s)
	{
		sm.addString(s);
		announceToPlayer(sm);
	}
	
	public void updatePlayerSiegeStateFlags(boolean clear)
	{
		L2Clan clan;
		for (L2SiegeClan siegeclan : getAttackerClans())
		{
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			for (L2PcInstance member : clan.getOnlineMembers(0))
			{
				if (member == null)
					continue;
				
				if (clear)
				{
					member.setSiegeState((byte) 0);
					member.setSiegeSide(0);
					member.setIsInSiege(false);
					member.stopFameTask();
				}
				else
				{
					member.setSiegeState((byte) 1);
					member.setSiegeSide(getFort().getFortId());
					if (checkIfInZone(member))
					{
						member.setIsInSiege(true);
						member.startFameTask(Config.FORTRESS_ZONE_FAME_TASK_FREQUENCY * 1000, Config.FORTRESS_ZONE_FAME_AQUIRE_POINTS);
					}
				}
				member.broadcastUserInfo();
			}
		}
		if (getFort().getOwnerClan() != null)
		{
			clan = ClanTable.getInstance().getClan(getFort().getOwnerClan().getClanId());
			for (L2PcInstance member : clan.getOnlineMembers(0))
			{
				if (member == null)
					continue;
				
				if (clear)
				{
					member.setSiegeState((byte) 0);
					member.setSiegeSide(0);
					member.setIsInSiege(false);
					member.stopFameTask();
				}
				else
				{
					member.setSiegeState((byte) 2);
					member.setSiegeSide(getFort().getFortId());
					if (checkIfInZone(member))
					{
						member.setIsInSiege(true);
						member.startFameTask(Config.FORTRESS_ZONE_FAME_TASK_FREQUENCY * 1000, Config.FORTRESS_ZONE_FAME_AQUIRE_POINTS);
					}
				}
				member.broadcastUserInfo();
			}
		}
	}
	
	/**
	 * @param object 
	 * @return true if object is inside the zone 
	 */
	public boolean checkIfInZone(L2Object object)
	{
		return checkIfInZone(object.getX(), object.getY(), object.getZ());
	}
	
	/**
	 * @param x 
	 * @param y 
	 * @param z 
	 * @return true if object is inside the zone
	 */
	public boolean checkIfInZone(int x, int y, int z)
	{
		return (getIsInProgress() && (getFort().checkIfInZone(x, y, z))); // Fort zone during siege
	}
	
	/**
	 * @param clan The L2Clan of the player
	 * @return true if clan is attacker
	 */
	@Override
	public boolean checkIsAttacker(L2Clan clan)
	{
		return (getAttackerClan(clan) != null);
	}
	
	/**
	 * @param clan The L2Clan of the player
	 * @return true if clan is defender
	 */
	@Override
	public boolean checkIsDefender(L2Clan clan)
	{
		if (clan != null && getFort().getOwnerClan() == clan)
			return true;
		
		return false;
	}
	
	/** Clear all registered siege clans from database for fort */
	public void clearSiegeClan()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM fortsiege_clans WHERE fort_id=?"))
		{
			ps.setInt(1, getFort().getFortId());
			ps.execute();
			
			if (getFort().getOwnerClan() != null)
			{
				try (PreparedStatement delete = con.prepareStatement("DELETE FROM fortsiege_clans WHERE clan_id=?"))
				{
					delete.setInt(1, getFort().getOwnerClan().getClanId());
					delete.execute();
				}
			}
			
			getAttackerClans().clear();
			
			// if siege is in progress, end siege
			if (getIsInProgress())
				endSiege();
			
			// if siege isn't in progress (1hr waiting time till siege starts), cancel waiting time
			if (_siegeStartTask != null)
			{
				_siegeStartTask.cancel(true);
				_siegeStartTask = null;
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception: clearSiegeClan(): " + e.getMessage(), e);
		}
	}
	
	/** Set the date for the next siege. */
	private void clearSiegeDate()
	{
		getFort().getSiegeDate().setTimeInMillis(0);
	}
	
	/**
	 * @return list of L2PcInstance registered as attacker in the zone.
	 */
	@Override
	public List<L2PcInstance> getAttackersInZone()
	{
		List<L2PcInstance> players = new FastList<>();
		L2Clan clan;
		for (L2SiegeClan siegeclan : getAttackerClans())
		{
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			for (L2PcInstance player : clan.getOnlineMembers(0))
			{
				if (player == null)
					continue;
				
				if (player.isInSiege())
					players.add(player);
			}
		}
		return players;
	}
	
	/**
	 * @return list of L2PcInstance in the zone.
	 */
	public List<L2PcInstance> getPlayersInZone()
	{
		return getFort().getZone().getPlayersInside();
	}
	
	/**
	 * @return list of L2PcInstance owning the fort in the zone.
	 */
	public List<L2PcInstance> getOwnersInZone()
	{
		List<L2PcInstance> players = new FastList<>();
		L2Clan clan;
		if (getFort().getOwnerClan() != null)
		{
			clan = ClanTable.getInstance().getClan(getFort().getOwnerClan().getClanId());
			if (clan != getFort().getOwnerClan())
				return null;
			
			for (L2PcInstance player : clan.getOnlineMembers(0))
			{
				if (player == null)
					continue;
				
				if (player.isInSiege())
					players.add(player);
			}
		}
		return players;
	}
	
	/**
	 * Commander was killed 
	 * @param instance
	 */
	public void killedCommander(L2FortCommanderInstance instance)
	{
		if (_commanders != null && getFort() != null && _commanders.size() != 0)
		{
			L2Spawn spawn = instance.getSpawn();
			if (spawn != null)
			{
				FastList<SiegeSpawn> commanders = FortSiegeManager.getInstance().getCommanderSpawnList(getFort().getFortId());
				for (SiegeSpawn spawn2 : commanders)
				{
					if (spawn2.getNpcId() == spawn.getNpcid())
					{
						NpcStringId npcString = null;
						switch (spawn2.getId())
						{
							case 1:
								npcString = NpcStringId.YOU_MAY_HAVE_BROKEN_OUR_ARROWS_BUT_YOU_WILL_NEVER_BREAK_OUR_WILL_ARCHERS_RETREAT;
								break;
							case 2:
								npcString = NpcStringId.AIIEEEE_COMMAND_CENTER_THIS_IS_GUARD_UNIT_WE_NEED_BACKUP_RIGHT_AWAY;
								break;
							case 3:
								npcString = NpcStringId.AT_LAST_THE_MAGIC_FIELD_THAT_PROTECTS_THE_FORTRESS_HAS_WEAKENED_VOLUNTEERS_STAND_BACK;
								break;
							case 4:
								npcString = NpcStringId.I_FEEL_SO_MUCH_GRIEF_THAT_I_CANT_EVEN_TAKE_CARE_OF_MYSELF_THERE_ISNT_ANY_REASON_FOR_ME_TO_STAY_HERE_ANY_LONGER;
								break;
						}
						if (npcString != null)
							instance.broadcastPacket(new NpcSay(instance.getObjectId(), 1, instance.getNpcId(), npcString));
					}
				}
				_commanders.remove(spawn);
				if (_commanders.isEmpty())
				{
					// spawn fort flags
					spawnFlag(getFort().getFortId());
					// cancel door/commanders respawn
					if (_siegeRestore != null)
					{
						_siegeRestore.cancel(true);
					}
					// open doors in main building
					for (L2DoorInstance door : getFort().getDoors())
					{
						if (door.getIsShowHp())
							continue;
						
						//TODO this also opens control room door at big fort
						door.openMe();
					}
					getFort().getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.ALL_BARRACKS_OCCUPIED));
				}
				// schedule restoring doors/commanders respawn
				else if (_siegeRestore == null)
				{
					getFort().getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.SEIZED_BARRACKS));
					_siegeRestore = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleSiegeRestore(), FortSiegeManager.getInstance().getCountDownLength() * 60 * 1000L);
				}
				else
					getFort().getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.SEIZED_BARRACKS));
			}
			else
				_log.warning("FortSiege.killedCommander(): killed commander, but commander not registered for fortress. NpcId: " + instance.getNpcId() + " FortId: " + getFort().getFortId());
		}
	}
	
	/**
	 * Remove the flag that was killed 
	 * @param flag
	 */
	public void killedFlag(L2Npc flag)
	{
		if (flag == null)
			return;
		
		for (L2SiegeClan clan : getAttackerClans())
		{
			if (clan.removeFlag(flag))
				return;
		}
	}
	
	/**
	 * Register clan as attacker<BR><BR>
	 * @param player The L2PcInstance of the player trying to register
	 * @param force 
	 * @return 
	 */
	public boolean registerAttacker(L2PcInstance player, boolean force)
	{
		if (player.getClan() == null)
			return false;
		
		if (force || checkIfCanRegister(player))
		{
			saveSiegeClan(player.getClan()); // Save to database
			// if the first registering we start the timer
			if (getAttackerClans().size() == 1)
			{
				if (!force)
					player.reduceAdena("siege", 250000, null, true);
				startAutoTask(true);
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Remove clan from siege<BR><BR>
	 * This function does not do any checks and should not be called from bypass !
	 * @param clanId The int of player's clan id
	 */
	private void removeSiegeClan(int clanId)
	{
		final String query = (clanId != 0) ? DELETE_FORT_SIEGECLANS_BY_CLAN_ID : DELETE_FORT_SIEGECLANS;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(query))
		{
			statement.setInt(1, getFort().getFortId());
			if (clanId != 0)
			{
				statement.setInt(2, clanId);
			}
			statement.execute();
			
			loadSiegeClan();
			if (getAttackerClans().isEmpty())
			{
				if (getIsInProgress())
					endSiege();
				else
					saveFortSiege(); // Clear siege time in DB
				
				if (_siegeStartTask != null)
				{
					_siegeStartTask.cancel(true);
					_siegeStartTask = null;
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception on removeSiegeClan: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Remove clan from siege<BR><BR>
	 * @param clan The clan being removed
	 */
	public void removeSiegeClan(L2Clan clan)
	{
		if (clan == null || clan.getFortId() == getFort().getFortId() || !FortSiegeManager.getInstance().checkIsRegistered(clan, getFort().getFortId()))
			return;
		
		removeSiegeClan(clan.getClanId());
	}
	
	/**
	 * Start the auto tasks<BR>
	 * <BR>
	 */
	public void checkAutoTask()
	{
		if (_siegeStartTask != null) //safety check
			return;
		
		final long delay = getFort().getSiegeDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
		
		if (delay < 0)
		{
			// siege time in past
			saveFortSiege();
			clearSiegeClan(); // remove all clans
			// spawn suspicious merchant immediately
			ThreadPoolManager.getInstance().executeTask(new ScheduleSuspiciousMerchantSpawn());
		}
		else
		{
			loadSiegeClan();
			if (getAttackerClans().isEmpty())
				// no attackers - waiting for suspicious merchant spawn
				ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleSuspiciousMerchantSpawn(), delay);
			else
			{
				// preparing start siege task
				if (delay > 3600000) // more than hour, how this can happens ? spawn suspicious merchant
				{
					ThreadPoolManager.getInstance().executeTask(new ScheduleSuspiciousMerchantSpawn());
					_siegeStartTask = ThreadPoolManager.getInstance().scheduleGeneral(new FortSiege.ScheduleStartSiegeTask(3600), delay - 3600000);
				}
				if (delay > 600000) // more than 10 min, spawn suspicious merchant
				{
					ThreadPoolManager.getInstance().executeTask(new ScheduleSuspiciousMerchantSpawn());
					_siegeStartTask = ThreadPoolManager.getInstance().scheduleGeneral(new FortSiege.ScheduleStartSiegeTask(600), delay - 600000);
				}
				else if (delay > 300000) // more than 5 min
					_siegeStartTask = ThreadPoolManager.getInstance().scheduleGeneral(new FortSiege.ScheduleStartSiegeTask(300), delay - 300000);
				else if (delay > 60000) //more than 1 min
					_siegeStartTask = ThreadPoolManager.getInstance().scheduleGeneral(new FortSiege.ScheduleStartSiegeTask(60), delay - 60000);
				else
					// lower than 1 min, set to 1 min
					_siegeStartTask = ThreadPoolManager.getInstance().scheduleGeneral(new FortSiege.ScheduleStartSiegeTask(60), 0);
				
				_log.info("Siege of " + getFort().getName() + " fort: " + getFort().getSiegeDate().getTime());
			}
		}
	}
	
	/**
	 * Start the auto tasks<BR><BR>
	 * @param setTime 
	 */
	public void startAutoTask(boolean setTime)
	{
		if (_siegeStartTask != null)
			return;
		
		if (setTime)
			setSiegeDateTime(false);
		
		if (getFort().getOwnerClan() != null)
			getFort().getOwnerClan().broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.A_FORTRESS_IS_UNDER_ATTACK));
		
		// Execute siege auto start
		_siegeStartTask = ThreadPoolManager.getInstance().scheduleGeneral(new FortSiege.ScheduleStartSiegeTask(3600), 0);
	}
	
	/**
	 * Teleport players
	 * @param teleportWho 
	 * @param teleportWhere 
	 */
	public void teleportPlayer(TeleportWhoType teleportWho, MapRegionManager.TeleportWhereType teleportWhere)
	{
		List<L2PcInstance> players;
		switch (teleportWho)
		{
			case Owner:
				players = getOwnersInZone();
				break;
			case Attacker:
				players = getAttackersInZone();
				break;
			default:
				players = getPlayersInZone();
		}
		
		for (L2PcInstance player : players)
		{
			if (player.isGM() || player.isInJail())
				continue;
			
			player.teleToLocation(teleportWhere);
		}
	}
	
	/**
	 * Add clan as attacker<BR><BR>
	 * @param clanId The int of clan's id
	 */
	private void addAttacker(int clanId)
	{
		getAttackerClans().add(new L2SiegeClan(clanId, SiegeClanType.ATTACKER)); // Add registered attacker to attacker list
	}
	
	/**
	 * @param player The L2PcInstance of the player trying to register
	 * @return true if the player can register.
	 */
	public boolean checkIfCanRegister(L2PcInstance player)
	{
		boolean b = true;
		if (player.getClan() == null || player.getClan().getLevel() < FortSiegeManager.getInstance().getSiegeClanMinLevel())
		{
			b = false;
			player.sendMessage("Only clans with Level " + FortSiegeManager.getInstance().getSiegeClanMinLevel() + " and higher may register for a fortress siege.");
		}
		else if ((player.getClanPrivileges() & L2Clan.CP_CS_MANAGE_SIEGE) != L2Clan.CP_CS_MANAGE_SIEGE)
		{
			b = false;
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
		}
		else if (player.getClan() == getFort().getOwnerClan())
		{
			b = false;
			player.sendPacket(SystemMessageId.CLAN_THAT_OWNS_CASTLE_IS_AUTOMATICALLY_REGISTERED_DEFENDING);
		}
		else if (getFort().getOwnerClan() != null && player.getClan().getCastleId() > 0 && player.getClan().getCastleId() == getFort().getCastleId())
		{
			b = false;
			player.sendPacket(SystemMessageId.CANT_REGISTER_TO_SIEGE_DUE_TO_CONTRACT);
		}
		else if (getFort().getTimeTillRebelArmy() > 0 && getFort().getTimeTillRebelArmy() <= 7200)
		{
			b = false;
			player.sendMessage("You cannot register for the fortress siege 2 hours prior to rebel army attack.");
		}
		else if (getFort().getSiege().getAttackerClans().isEmpty() && player.getInventory().getAdena() < 250000)
		{
			b = false;
			player.sendMessage("You need 250,000 adena to register"); // replace me with html
		}
		else
		{
			for (Fort fort : FortManager.getInstance().getForts())
			{
				if (fort.getSiege().getAttackerClan(player.getClanId()) != null)
				{
					b = false;
					player.sendPacket(SystemMessageId.ALREADY_REQUESTED_SIEGE_BATTLE);
					break;
				}
				if (fort.getOwnerClan() == player.getClan() && (fort.getSiege().getIsInProgress() || fort.getSiege()._siegeStartTask != null))
				{
					b = false;
					player.sendPacket(SystemMessageId.ALREADY_REQUESTED_SIEGE_BATTLE);
					break;
				}
			}
		}
		return b;
	}
	
	/**
	 * @param clan The L2Clan of the player trying to register
	 * @return true if the clan has already registered to a siege for the same day.
	 */
	public boolean checkIfAlreadyRegisteredForSameDay(L2Clan clan)
	{
		for (FortSiege siege : FortSiegeManager.getInstance().getSieges())
		{
			if (siege == this)
				continue;
			
			if (siege.getSiegeDate().get(Calendar.DAY_OF_WEEK) == getSiegeDate().get(Calendar.DAY_OF_WEEK))
			{
				if (siege.checkIsAttacker(clan))
					return true;
				if (siege.checkIsDefender(clan))
					return true;
			}
		}
		
		return false;
	}
	
	private void setSiegeDateTime(boolean merchant)
	{
		Calendar newDate = Calendar.getInstance();
		if (merchant)
			newDate.add(Calendar.MINUTE, FortSiegeManager.getInstance().getSuspiciousMerchantRespawnDelay());
		else
			newDate.add(Calendar.MINUTE, 60);
		getFort().setSiegeDate(newDate);
		saveSiegeDate();
	}
	
	/** Load siege clans. */
	private void loadSiegeClan()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			getAttackerClans().clear();
			PreparedStatement statement = con.prepareStatement("SELECT clan_id FROM fortsiege_clans WHERE fort_id=?");
			statement.setInt(1, getFort().getFortId());
			ResultSet rs = statement.executeQuery();
			while (rs.next())
			{
				addAttacker(rs.getInt("clan_id"));
			}
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception: loadSiegeClan(): " + e.getMessage(), e);
		}
	}
	
	/** Remove commanders. */
	private void removeCommanders()
	{
		if (_commanders != null && !_commanders.isEmpty())
		{
			// Remove all instance of commanders for this fort
			for (L2Spawn spawn : _commanders)
			{
				if (spawn != null)
				{
					spawn.stopRespawn();
					if (spawn.getLastSpawn() != null)
						spawn.getLastSpawn().deleteMe();
				}
			}
			_commanders.clear();
		}
	}
	
	/** Remove all flags. */
	private void removeFlags()
	{
		for (L2SiegeClan sc : getAttackerClans())
		{
			if (sc != null)
				sc.removeFlags();
		}
	}
	
	/** Save fort siege related to database. */
	private void saveFortSiege()
	{
		clearSiegeDate(); // clear siege date
		saveSiegeDate(); // Save the new date
	}
	
	/** Save siege date to database. */
	private void saveSiegeDate()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE fort SET siegeDate = ? WHERE id = ?"))
		{
			ps.setLong(1, getSiegeDate().getTimeInMillis());
			ps.setInt(2, getFort().getFortId());
			ps.execute();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception: saveSiegeDate(): " + e.getMessage(), e);
		}
	}
	
	/**
	 * Save registration to database.<BR><BR>
	 * @param clan The L2Clan of player
	 */
	private void saveSiegeClan(L2Clan clan)
	{
		if (getAttackerClans().size() >= FortSiegeManager.getInstance().getAttackerMaxClans())
		{
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO fortsiege_clans (clan_id,fort_id) values (?,?)"))
		{
			statement.setInt(1, clan.getClanId());
			statement.setInt(2, getFort().getFortId());
			statement.execute();
			
			addAttacker(clan.getClanId());
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception: saveSiegeClan(L2Clan clan): " + e.getMessage(), e);
		}
	}
	
	/** Spawn commanders. */
	private void spawnCommanders()
	{
		//Set commanders array size if one does not exist
		try
		{
			_commanders.clear();
			L2Spawn spawnDat;
			L2NpcTemplate template1;
			for (SiegeSpawn _sp : FortSiegeManager.getInstance().getCommanderSpawnList(getFort().getFortId()))
			{
				template1 = NpcTable.getInstance().getTemplate(_sp.getNpcId());
				if (template1 != null)
				{
					spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(1);
					spawnDat.setLocx(_sp.getLocation().getX());
					spawnDat.setLocy(_sp.getLocation().getY());
					spawnDat.setLocz(_sp.getLocation().getZ());
					spawnDat.setHeading(_sp.getLocation().getHeading());
					spawnDat.setRespawnDelay(60);
					spawnDat.doSpawn();
					spawnDat.stopRespawn();
					_commanders.add(spawnDat);
				}
				else
				{
					_log.warning("FortSiege.spawnCommander: Data missing in NPC table for ID: " + _sp.getNpcId() + ".");
				}
			}
		}
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			_log.log(Level.WARNING, "FortSiege.spawnCommander: Spawn could not be initialized: " + e.getMessage(), e);
		}
	}
	
	private void spawnFlag(int Id)
	{
		for (CombatFlag cf : FortSiegeManager.getInstance().getFlagList(Id))
			cf.spawnMe();
	}
	
	private void unSpawnFlags()
	{
		if (FortSiegeManager.getInstance().getFlagList(getFort().getFortId()) == null)
			return;
		
		for (CombatFlag cf : FortSiegeManager.getInstance().getFlagList(getFort().getFortId()))
			cf.unSpawnMe();
	}
	
	/**
	 * Spawn siege guard.<BR><BR>
	 */
	private void spawnSiegeGuard()
	{
		getSiegeGuardManager().spawnSiegeGuard();
	}
	
	@Override
	public final L2SiegeClan getAttackerClan(L2Clan clan)
	{
		if (clan == null)
			return null;
		
		return getAttackerClan(clan.getClanId());
	}
	
	@Override
	public final L2SiegeClan getAttackerClan(int clanId)
	{
		for (L2SiegeClan sc : getAttackerClans())
			if (sc != null && sc.getClanId() == clanId)
				return sc;
		
		return null;
	}
	
	@Override
	public final List<L2SiegeClan> getAttackerClans()
	{
		return _attackerClans;
	}
	
	public final Fort getFort()
	{
		return _fort;
	}
	
	public final boolean getIsInProgress()
	{
		return _isInProgress;
	}
	
	@Override
	public final Calendar getSiegeDate()
	{
		return getFort().getSiegeDate();
	}
	
	@Override
	public List<L2Npc> getFlag(L2Clan clan)
	{
		if (clan != null)
		{
			L2SiegeClan sc = getAttackerClan(clan);
			if (sc != null)
				return sc.getFlag();
		}
		
		return null;
	}
	
	public final FortSiegeGuardManager getSiegeGuardManager()
	{
		if (_siegeGuardManager == null)
			_siegeGuardManager = new FortSiegeGuardManager(getFort());
		
		return _siegeGuardManager;
	}
	
	public void resetSiege()
	{
		// reload commanders and repair doors
		removeCommanders();
		spawnCommanders();
		getFort().resetDoors();
	}
	
	public List<L2Spawn> getCommanders()
	{
		return _commanders;
	}
	
	@Override
	public L2SiegeClan getDefenderClan(int clanId)
	{
		return null;
	}
	
	@Override
	public L2SiegeClan getDefenderClan(L2Clan clan)
	{
		return null;
	}
	
	@Override
	public List<L2SiegeClan> getDefenderClans()
	{
		return null;
	}
	
	@Override
	public boolean giveFame()
	{
		return true;
	}
	
	@Override
	public int getFameFrequency()
	{
		return Config.FORTRESS_ZONE_FAME_TASK_FREQUENCY;
	}
	
	@Override
	public int getFameAmount()
	{
		return Config.FORTRESS_ZONE_FAME_AQUIRE_POINTS;
	}
	
	@Override
	public void updateSiege()
	{
	}
	
	// Listeners
	/**
	 * Fires all the FortSiegeListener.onStart() or onEnd() methods<br>
	 * depending on the EventStage<br>
	 * @param stage
	 * @return if onStart() returns false, the siege is cancelled
	 */
	private boolean fireFortSiegeEventListeners(EventStage stage)
	{
		if (!fortSiegeListeners.isEmpty())
		{
			FortSiegeEvent event = new FortSiegeEvent();
			event.setSiege(this);
			event.setStage(stage);
			switch (stage)
			{
				case START:
				{
					for (FortSiegeListener listener : fortSiegeListeners)
					{
						if (!listener.onStart(event))
						{
							return false;
						}
					}
					break;
				}
				case END:
				{
					for (FortSiegeListener listener : fortSiegeListeners)
					{
						listener.onEnd(event);
					}
					break;
				}
				default:
					break;
			}
		}
		return true;
	}
	
	/**
	 * Adds a fort siege listener
	 * @param listener
	 */
	public static void addFortSiegeListener(FortSiegeListener listener)
	{
		if (!fortSiegeListeners.contains(listener))
		{
			fortSiegeListeners.add(listener);
		}
	}
	
	/**
	 * Removes a fort siege listener
	 * @param listener
	 */
	public static void removeFortSiegeListener(FortSiegeListener listener)
	{
		fortSiegeListeners.remove(listener);
	}
}
