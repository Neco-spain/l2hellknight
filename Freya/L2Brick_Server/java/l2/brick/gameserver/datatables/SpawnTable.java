package l2.brick.gameserver.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.logging.Logger;


import javolution.util.FastSet;
import l2.brick.Config;
import l2.brick.L2DatabaseFactory;
import l2.brick.gameserver.instancemanager.DayNightSpawnManager;
import l2.brick.gameserver.model.L2Spawn;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.templates.L2NpcTemplate;

/**
 * This class ...
 *
 * @author Nightmare
 * @version $Revision: 1.5.2.6.2.7 $ $Date: 2005/03/27 15:29:18 $
 */
public class SpawnTable
{
	private static Logger _log = Logger.getLogger(SpawnTable.class.getName());
	
	private FastSet<L2Spawn> _spawntable = new FastSet<L2Spawn>().shared();
	private int _npcSpawnCount;
	private int _customSpawnCount;
	
	public static SpawnTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private SpawnTable()
	{
		if (!Config.ALT_DEV_NO_SPAWNS)
			fillSpawnTable();
	}
	
	public FastSet<L2Spawn> getSpawnTable()
	{
		return _spawntable;
	}
	
	private void fillSpawnTable()
	{
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement("SELECT count, npc_templateid, locx, locy, locz, heading, respawn_delay, loc_id, periodOfDay FROM spawnlist");
			ResultSet rset = statement.executeQuery();
			
			L2Spawn spawnDat;
			L2NpcTemplate template1;
			
			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					if (template1.type.equalsIgnoreCase("L2SiegeGuard"))
					{
						// Don't spawn
					}
					else if (template1.type.equalsIgnoreCase("L2RaidBoss"))
					{
						// Don't spawn raidboss
					}
					else if (!Config.ALLOW_CLASS_MASTERS && template1.type.equals("L2ClassMaster"))
					{
						// Dont' spawn class masters
					}
					else
					{
						spawnDat = new L2Spawn(template1);
						spawnDat.setAmount(rset.getInt("count"));
						spawnDat.setLocx(rset.getInt("locx"));
						spawnDat.setLocy(rset.getInt("locy"));
						spawnDat.setLocz(rset.getInt("locz"));
						spawnDat.setHeading(rset.getInt("heading"));
						spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
						int loc_id = rset.getInt("loc_id");
						spawnDat.setLocation(loc_id);
						
						switch (rset.getInt("periodOfDay"))
						{
							case 0: // default
								_npcSpawnCount += spawnDat.init();
								break;
							case 1: // Day
								DayNightSpawnManager.getInstance().addDayCreature(spawnDat);
								_npcSpawnCount++;
								break;
							case 2: // Night
								DayNightSpawnManager.getInstance().addNightCreature(spawnDat);
								_npcSpawnCount++;
								break;
						}
						
						_spawntable.add(spawnDat);
					}
				}
				else
				{
					_log.warning("SpawnTable: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			_log.warning("SpawnTable: Spawn could not be initialized: " + e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		
		_log.info("SpawnTable: Loaded " + _spawntable.size() + " Npc Spawn Locations.");
		
		if (Config.CUSTOM_SPAWNLIST_TABLE)
		{
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement;
				statement = con.prepareStatement("SELECT count, npc_templateid, locx, locy, locz, heading, respawn_delay, loc_id, periodOfDay FROM custom_spawnlist");
				ResultSet rset = statement.executeQuery();
				
				L2Spawn spawnDat;
				L2NpcTemplate template1;
				
				while (rset.next())
				{
					template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
					if (template1 != null)
					{
						if (template1.type.equalsIgnoreCase("L2SiegeGuard"))
						{
							// Don't spawn
						}
						else if (template1.type.equalsIgnoreCase("L2RaidBoss"))
						{
							// Don't spawn raidboss
						}
						else if (!Config.ALLOW_CLASS_MASTERS && template1.type.equals("L2ClassMaster"))
						{
							// Dont' spawn class masters
						}
						else
						{
							spawnDat = new L2Spawn(template1);
							spawnDat.setAmount(rset.getInt("count"));
							spawnDat.setLocx(rset.getInt("locx"));
							spawnDat.setLocy(rset.getInt("locy"));
							spawnDat.setLocz(rset.getInt("locz"));
							spawnDat.setHeading(rset.getInt("heading"));
							spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
							spawnDat.setCustom(true);
							int loc_id = rset.getInt("loc_id");
							spawnDat.setLocation(loc_id);
							
							switch (rset.getInt("periodOfDay"))
							{
								case 0: // default
									_customSpawnCount += spawnDat.init();
									break;
								case 1: // Day
									DayNightSpawnManager.getInstance().addDayCreature(spawnDat);
									_customSpawnCount++;
									break;
								case 2: // Night
									DayNightSpawnManager.getInstance().addNightCreature(spawnDat);
									_customSpawnCount++;
									break;
							}
							
							_spawntable.add(spawnDat);
						}
					}
					else
					{
						_log.warning("CustomSpawnTable: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
					}
				}
				rset.close();
				statement.close();
			}
			catch (Exception e)
			{
				// problem with initializing spawn, go to next one
				_log.warning("CustomSpawnTable: Spawn could not be initialized: " + e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
			_log.info("CustomSpawnTable: Loaded " + _customSpawnCount + " Npc Spawn Locations.");
			
		}
		
		if (Config.DEBUG)
			_log.info("SpawnTable: Spawning completed, total number of NPCs in the world: " + (_npcSpawnCount + _customSpawnCount));
		
	}
	
	public void addNewSpawn(L2Spawn spawn, boolean storeInDb)
	{
		_spawntable.add(spawn);
		
		if (storeInDb)
		{
			Connection con = null;
			String spawnTable;
			if (spawn.isCustom() && Config.CUSTOM_SPAWNLIST_TABLE)
				spawnTable = "custom_spawnlist";
			else
				spawnTable = "spawnlist";
			
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("INSERT INTO " + spawnTable
						+ "(count,npc_templateid,locx,locy,locz,heading,respawn_delay,loc_id) values(?,?,?,?,?,?,?,?)");
				statement.setInt(1, spawn.getAmount());
				statement.setInt(2, spawn.getNpcid());
				statement.setInt(3, spawn.getLocx());
				statement.setInt(4, spawn.getLocy());
				statement.setInt(5, spawn.getLocz());
				statement.setInt(6, spawn.getHeading());
				statement.setInt(7, spawn.getRespawnDelay() / 1000);
				statement.setInt(8, spawn.getLocation());
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				// problem with storing spawn
				_log.warning("SpawnTable: Could not store spawn in the DB:" + e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}
	}
	
	public void deleteSpawn(L2Spawn spawn, boolean updateDb)
	{
		
		if (!_spawntable.remove(spawn))
			return;
		
		if (updateDb)
		{
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("DELETE FROM "
						+ (spawn.isCustom() ? "custom_spawnlist" : "spawnlist") + " WHERE locx=? AND locy=? AND locz=? AND npc_templateid=? AND heading=?");
				statement.setInt(1, spawn.getLocx());
				statement.setInt(2, spawn.getLocy());
				statement.setInt(3, spawn.getLocz());
				statement.setInt(4, spawn.getNpcid());
				statement.setInt(5, spawn.getHeading());
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				// problem with deleting spawn
				_log.warning("SpawnTable: Spawn " + spawn + " could not be removed from DB: " + e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
			
		}
	}
	
	//just wrapper
	public void reloadAll()
	{
		fillSpawnTable();
	}
	
	/**
	 * Get all the spawn of a NPC<BR><BR>
	 *
	 * @param npcId : ID of the NPC to find.
	 * @return
	 */
	public void findNPCInstances(L2PcInstance activeChar, int npcId, int teleportIndex, boolean showposition)
	{
		int index = 0;
		for (L2Spawn spawn : _spawntable)
		{
			if (npcId == spawn.getNpcid())
			{
				index++;
				L2Npc _npc = spawn.getLastSpawn();
				if (teleportIndex > -1)
				{
					if (teleportIndex == index)
					{
						if(showposition && _npc != null)
							activeChar.teleToLocation(_npc.getX(), _npc.getY(), _npc.getZ(), true);
						else
							activeChar.teleToLocation(spawn.getLocx(), spawn.getLocy(), spawn.getLocz(), true);
					}
				}
				else
				{
					if(showposition && _npc != null)
						activeChar.sendMessage(index + " - " + spawn.getTemplate().name + " (" + spawn + "): " + _npc.getX() + " "+ _npc.getY() + " " + _npc.getZ());
					else
						activeChar.sendMessage(index + " - " + spawn.getTemplate().name + " (" + spawn + "): " + spawn.getLocx() + " "+ spawn.getLocy() + " " + spawn.getLocz());
				}
			}
		}
		if (index == 0)
			activeChar.sendMessage("No current spawns found.");
	}

    	public L2Npc findNpc(int npcId)
	{
		for (L2Spawn spawn : _spawntable)
			if (spawn != null && spawn.getTemplate() != null && npcId == spawn.getNpcid() && spawn.getLastSpawn() != null)
				return spawn.getLastSpawn();
		return null;
	}
	
    	public ArrayList<L2Npc> findNpces(int npcId)
	{
		ArrayList<L2Npc> npces = new ArrayList<L2Npc>();

		for (L2Spawn spawn : _spawntable)
		{
			if (npcId == spawn.getNpcid() && spawn.getLastSpawn() != null)
			{
				npces.add(spawn.getLastSpawn());
			}
		}

		return npces;
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final SpawnTable _instance = new SpawnTable();
	}
}
