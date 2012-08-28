package net.sf.l2j.gameserver.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.instancemanager.DayNightSpawnManager;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class SpawnTable
{
  private static Logger _log = Logger.getLogger(SpawnTable.class.getName());

  private static final SpawnTable _instance = new SpawnTable();

  private Map<Integer, L2Spawn> _spawntable = new FastMap().setShared(true);
  private int _npcSpawnCount;
  private int _highestId;

  public static SpawnTable getInstance()
  {
    return _instance;
  }

  private SpawnTable()
  {
    fillSpawnTable();
  }

  public Map<Integer, L2Spawn> getSpawnTable()
  {
    return _spawntable;
  }

  private void fillSpawnTable()
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, loc_id, periodOfDay FROM spawnlist ORDER BY id");
      ResultSet rset = statement.executeQuery();

      while (rset.next())
      {
        L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
        if (template1 != null)
        {
          if ((template1.type.equalsIgnoreCase("L2SiegeGuard")) || 
            (template1.type.equalsIgnoreCase("L2RaidBoss")) || (
            (!Config.ALLOW_CLASS_MASTERS) && (template1.type.equals("L2ClassMaster"))))
          {
            continue;
          }

          L2Spawn spawnDat = new L2Spawn(template1);
          spawnDat.setId(rset.getInt("id"));
          spawnDat.setAmount(rset.getInt("count"));
          spawnDat.setLocx(rset.getInt("locx"));
          spawnDat.setLocy(rset.getInt("locy"));
          spawnDat.setLocz(rset.getInt("locz"));
          spawnDat.setHeading(rset.getInt("heading"));
          spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
          int loc_id = rset.getInt("loc_id");
          spawnDat.setLocation(loc_id);

          switch (rset.getInt("periodOfDay")) {
          case 0:
            _npcSpawnCount += spawnDat.init();
            break;
          case 1:
            DayNightSpawnManager.getInstance().addDayCreature(spawnDat);
            _npcSpawnCount += 1;
            break;
          case 2:
            DayNightSpawnManager.getInstance().addNightCreature(spawnDat);
            _npcSpawnCount += 1;
          }

          _spawntable.put(Integer.valueOf(spawnDat.getId()), spawnDat);
          if (spawnDat.getId() > _highestId) _highestId = spawnDat.getId();
          continue;
        }

        _log.warning("SpawnTable: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
      }

      rset.close();
      statement.close();
    }
    catch (Exception e)
    {
      _log.warning("SpawnTable: Spawn could not be initialized: " + e);
    }
    finally
    {
      try
      {
        con.close();
      }
      catch (Exception e)
      {
      }
    }

    _log.config("SpawnTable: Loaded " + _spawntable.size() + " Npc Spawn Locations.");

    if (Config.DEBUG)
      _log.fine("SpawnTable: Spawning completed, total number of NPCs in the world: " + _npcSpawnCount);
  }

  public L2Spawn getTemplate(int id)
  {
    return (L2Spawn)_spawntable.get(Integer.valueOf(id));
  }

  public void addNewSpawn(L2Spawn spawn, boolean storeInDb)
  {
    _highestId += 1;
    spawn.setId(_highestId);
    _spawntable.put(Integer.valueOf(_highestId), spawn);

    if (storeInDb)
    {
      Connection con = null;
      try
      {
        con = L2DatabaseFactory.getInstance().getConnection();
        PreparedStatement statement = con.prepareStatement("INSERT INTO spawnlist (id,count,npc_templateid,locx,locy,locz,heading,respawn_delay,loc_id) values(?,?,?,?,?,?,?,?,?)");
        statement.setInt(1, spawn.getId());
        statement.setInt(2, spawn.getAmount());
        statement.setInt(3, spawn.getNpcid());
        statement.setInt(4, spawn.getLocx());
        statement.setInt(5, spawn.getLocy());
        statement.setInt(6, spawn.getLocz());
        statement.setInt(7, spawn.getHeading());
        statement.setInt(8, spawn.getRespawnDelay() / 1000);
        statement.setInt(9, spawn.getLocation());
        statement.execute();
        statement.close();
      }
      catch (Exception e)
      {
        _log.warning("SpawnTable: Could not store spawn in the DB:" + e);
      }
      finally
      {
        try
        {
          con.close();
        }
        catch (Exception e)
        {
        }
      }
    }
  }

  public void deleteSpawn(L2Spawn spawn, boolean updateDb)
  {
    if (_spawntable.remove(Integer.valueOf(spawn.getId())) == null) return;

    if (updateDb)
    {
      Connection con = null;
      try
      {
        con = L2DatabaseFactory.getInstance().getConnection();
        PreparedStatement statement = con.prepareStatement("DELETE FROM spawnlist WHERE id=?");
        statement.setInt(1, spawn.getId());
        statement.execute();
        statement.close();
      }
      catch (Exception e)
      {
        _log.warning("SpawnTable: Spawn " + spawn.getId() + " could not be removed from DB: " + e);
      }
      finally
      {
        try
        {
          con.close();
        }
        catch (Exception e)
        {
        }
      }
    }
  }

  public void reloadAll()
  {
    fillSpawnTable();
  }

  public void findNPCInstances(L2PcInstance activeChar, int npcId, int teleportIndex)
  {
    int index = 0;
    for (L2Spawn spawn : _spawntable.values())
    {
      if (npcId == spawn.getNpcid())
      {
        index++;

        if (teleportIndex > -1)
        {
          if (teleportIndex == index) {
            activeChar.teleToLocation(spawn.getLocx(), spawn.getLocy(), spawn.getLocz(), true);
          }
        }
        else {
          activeChar.sendMessage(index + " - " + spawn.getTemplate().name + " (" + spawn.getId() + "): " + spawn.getLocx() + " " + spawn.getLocy() + " " + spawn.getLocz());
        }

      }

    }

    if (index == 0) activeChar.sendMessage("No current spawns found."); 
  }

  public final Map<Integer, L2Spawn> getAllTemplates()
  {
    return _spawntable;
  }
}