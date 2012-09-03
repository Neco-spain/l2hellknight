package l2rt.gameserver.tables;

import l2rt.Config;
import l2rt.database.DatabaseUtils;
import l2rt.database.FiltredPreparedStatement;
import l2rt.database.L2DatabaseFactory;
import l2rt.database.ThreadConnection;
import l2rt.extensions.scripts.Scripts;
import l2rt.gameserver.instancemanager.DayNightSpawnManager;
import l2rt.gameserver.instancemanager.RaidBossSpawnManager;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Spawn;
import l2rt.gameserver.model.L2World;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.instances.L2SiegeGuardInstance;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.util.GArray;
import l2rt.util.Rnd;

import java.sql.ResultSet;
import java.util.logging.Logger;

public class SpawnTable
{
	private static final Logger _log = Logger.getLogger(SpawnTable.class.getName());

	private static SpawnTable _instance;

	private GArray<L2Spawn> _spawntable;
	private int _npcSpawnCount;
	private int _spawnCount;

	public static SpawnTable getInstance()
	{
		if(_instance == null)
			new SpawnTable();
		return _instance;
	}

	private SpawnTable()
	{
		_instance = this;
		NpcTable.getInstance().applyServerSideTitle();
		if(!Config.DONTLOADSPAWN)
			fillSpawnTable(true);
		else
		{
			_log.info("Spawn Correctly Disabled");
			Scripts.getInstance().callOnLoad();
		}
	}

	public GArray<L2Spawn> getSpawnTable()
	{
		return _spawntable;
	}

	private void fillSpawnTable(boolean scripts)
	{
		_spawntable = new GArray<L2Spawn>();
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM spawnlist ORDER by npc_templateid");
			//TODO возможно в будущем понадобится условие: WHERE npc_templateid NOT IN (SELECT bossId FROM epic_boss_spawn)
			rset = statement.executeQuery();

			L2Spawn spawnDat;
			L2NpcTemplate template1;
			_npcSpawnCount = 0;
			_spawnCount = 0;

			while(rset.next())
			{
				template1 = NpcTable.getTemplate(rset.getInt("npc_templateid"));
				if(template1 != null)
				{
					if(template1.isInstanceOf(L2SiegeGuardInstance.class))
					{
						// Don't spawn Siege Guard
					}
					else if(Config.ALLOW_CLASS_MASTERS_LIST.isEmpty() && template1.name.equalsIgnoreCase("L2ClassMaster"))
					{
						// Dont' spawn class masters
					}
					else
					{
						spawnDat = new L2Spawn(template1);
						spawnDat.setAmount(rset.getInt("count") * (Config.ALT_DOUBLE_SPAWN && !template1.isRaid ? 2 : 1));
						spawnDat.setLocx(rset.getInt("locx"));
						spawnDat.setLocy(rset.getInt("locy"));
						spawnDat.setLocz(rset.getInt("locz"));
						spawnDat.setHeading(rset.getInt("heading"));
						spawnDat.setRespawnDelay(rset.getInt("respawn_delay"), rset.getInt("respawn_delay_rnd"));
						spawnDat.setLocation(rset.getInt("loc_id"));
						spawnDat.setReflection(rset.getLong("reflection"));
						spawnDat.setRespawnTime(0);

						if(template1.isRaid)
							RaidBossSpawnManager.getInstance().addNewSpawn(spawnDat);

						switch(rset.getInt("periodOfDay"))
						{
							case 0: // default
								if(!Config.DELAYED_SPAWN)
									_npcSpawnCount += spawnDat.init();
								_spawntable.add(spawnDat);
								break;
							case 1: // Day
								DayNightSpawnManager.getInstance().addDayMob(spawnDat);
								break;
							case 2: // Night
								DayNightSpawnManager.getInstance().addNightMob(spawnDat);
								break;
						}
						_spawnCount++;

						if(_npcSpawnCount % 1000 == 0 && _npcSpawnCount != 0)
							_log.info("Spawned " + _npcSpawnCount + " npc");
					}
				}
				else
					_log.warning("mob data for id:" + rset.getInt("npc_templateid") + " missing in npc table");
			}
			DayNightSpawnManager.getInstance().notifyChangeMode();
		}
		catch(Exception e1)
		{
			_log.warning("spawn couldnt be initialized:" + e1);
			e1.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
		_log.config("SpawnTable: Loaded " + _spawnCount + " Npc Spawn Locations. Total NPCs: " + _npcSpawnCount);

		loadInventory();

		if(scripts)
			Scripts.getInstance().callOnLoad();
	}

	public void deleteSpawn(L2Spawn spawn, boolean updateDb)
	{
		//if (!_spawntable.remove(spawn))
		//	return;
		
		if (updateDb)
		{
			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("DELETE FROM spawnlist WHERE locx=? AND locy=? AND locz=? AND npc_templateid=? AND heading=?");
				statement.setInt(1, spawn.getLocx());
				statement.setInt(2, spawn.getLocy());
				statement.setInt(3, spawn.getLocz());
				statement.setInt(4, spawn.getNpcId());
				statement.setInt(5, spawn.getHeading());
				statement.execute();
			}
			catch (Exception e)
			{
				// problem with deleting spawn
				_log.warning("SpawnTable: Spawn " + spawn + " could not be removed from DB: " +  e);
			}
			finally
			{
				DatabaseUtils.closeDatabaseCS(con, statement);
			}
			
		}

	}
	
    public void deleteSpawn(L2Spawn spawn)
    {
        _spawntable.remove(spawn);
    }

	public void reloadAll()
	{
		L2World.deleteVisibleNpcSpawns();
		fillSpawnTable(false);
		RaidBossSpawnManager.getInstance().reloadBosses();
	}

	public void addNewSpawn(L2Spawn spawn, boolean storeInDb)
	{
		if (storeInDb)
		{

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO `spawnlist` (location,count,npc_templateid,locx,locy,locz,heading,respawn_delay,loc_id) values(?,?,?,?,?,?,?,?,?)");
			statement.setString(1, "");
			statement.setInt(2, spawn.getAmount());
			statement.setInt(3, spawn.getNpcId());
			statement.setInt(4, spawn.getLocx());
			statement.setInt(5, spawn.getLocy());
			statement.setInt(6, spawn.getLocz());
			statement.setInt(7, spawn.getHeading());
			statement.setInt(8, spawn.getRespawnDelay());
			statement.setInt(9, spawn.getLocation());
			statement.execute();
		}
		catch(Exception e1)
		{
			_log.warning("spawn couldnt be stored in db:" + e1);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
		}
	}
	
	public void loadInventory()
	{
		int count = 0;
		GArray<L2NpcInstance> temp = null;
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT owner_id, object_id, item_id, count, enchant_level FROM items WHERE loc = 'MONSTER'");
			rset = statement.executeQuery();

			while(rset.next())
			{
				count++;
				temp = L2ObjectsStorage.getAllByNpcId(rset.getInt("owner_id"), false);
				try
				{
					L2ItemInstance item = L2ItemInstance.restoreFromDb(rset.getInt("object_id"), true);
					if(temp.size() > 0)
					{
						L2MonsterInstance monster = (L2MonsterInstance) temp.toArray()[Rnd.get(temp.size())];
						monster.giveItem(item, false);
					}
					else
						NpcTable.getTemplate(rset.getInt("owner_id")).giveItem(item, false);
				}
				catch(Exception e)
				{
					_log.warning("Unable to restore inventory for " + temp.get(0).getNpcId());
				}
			}
		}
		catch(Exception e1)
		{
			e1.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
		_log.info("Monsters inventory loaded, items: " + count);
	}

	public static void unload()
	{
		if(_instance != null)
			_instance = null;
	}
}