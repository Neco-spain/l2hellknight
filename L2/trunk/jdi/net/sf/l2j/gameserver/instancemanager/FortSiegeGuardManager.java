package net.sf.l2j.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Fort;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class FortSiegeGuardManager
{
  private static final Logger _log = Logger.getLogger(FortSiegeGuardManager.class.getName());
  private Fort _fort;
  private List<L2Spawn> _siegeGuardSpawn = new FastList();

  public FortSiegeGuardManager(Fort fort)
  {
    _fort = fort;
  }

  public void addSiegeGuard(L2PcInstance activeChar, int npcId)
  {
    if (activeChar == null) {
      return;
    }
    addSiegeGuard(activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getHeading(), npcId);
  }

  public void addSiegeGuard(int x, int y, int z, int heading, int npcId)
  {
    saveSiegeGuard(x, y, z, heading, npcId, 0);
  }

  public void hireMerc(L2PcInstance activeChar, int npcId)
  {
    if (activeChar == null) {
      return;
    }
    hireMerc(activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getHeading(), npcId);
  }

  public void hireMerc(int x, int y, int z, int heading, int npcId)
  {
    saveSiegeGuard(x, y, z, heading, npcId, 1);
  }

  public void removeMerc(int npcId, int x, int y, int z)
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("Delete From fort_siege_guards Where npcId = ? And x = ? AND y = ? AND z = ? AND isHired = 1");
      statement.setInt(1, npcId);
      statement.setInt(2, x);
      statement.setInt(3, y);
      statement.setInt(4, z);
      statement.execute();
      statement.close();
      statement = null;
    }
    catch (Exception e)
    {
      e1.printStackTrace();

      _log.warning("Error deleting hired siege guard at " + x + ',' + y + ',' + z + ":" + e1);
    }
    finally
    {
      try {
        con.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
      con = null;
    }
  }

  public void removeMercs()
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("Delete From fort_siege_guards Where fortId = ? And isHired = 1");
      statement.setInt(1, getFort().getFortId());
      statement.execute();
      statement.close();
      statement = null;
    }
    catch (Exception e)
    {
      e1.printStackTrace();
      _log.warning("Error deleting hired siege guard for fort " + getFort().getName() + ":" + e1);
    }
    finally
    {
      try {
        con.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
      con = null;
    }
  }

  public void spawnSiegeGuard()
  {
    try
    {
      hiredCount = 0;

      isHired = getFort().getOwnerId() > 0;
      loadSiegeGuard();
      for (L2Spawn spawn : getSiegeGuardSpawn())
      {
        if (spawn != null)
        {
          spawn.init();
          if (isHired)
          {
            hiredCount++;
          }
        }
      }
    }
    catch (Throwable t)
    {
      int hiredCount;
      boolean isHired;
      t.printStackTrace();

      _log.warning("Error spawning siege guards for fort " + getFort().getName() + ":" + t.toString());
    }
  }

  public void unspawnSiegeGuard()
  {
    for (L2Spawn spawn : getSiegeGuardSpawn())
    {
      if (spawn == null)
      {
        continue;
      }

      spawn.stopRespawn();
      spawn.getLastSpawn().doDie(spawn.getLastSpawn());
    }

    getSiegeGuardSpawn().clear();
  }

  private void loadSiegeGuard()
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT * FROM fort_siege_guards Where fortId = ? ");
      statement.setInt(1, getFort().getFortId());
      ResultSet rs = statement.executeQuery();

      while (rs.next())
      {
        L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(rs.getInt("npcId"));
        if (template1 != null)
        {
          L2Spawn spawn1 = new L2Spawn(template1);
          spawn1.setId(rs.getInt("id"));
          spawn1.setAmount(1);
          spawn1.setLocx(rs.getInt("x"));
          spawn1.setLocy(rs.getInt("y"));
          spawn1.setLocz(rs.getInt("z"));
          spawn1.setHeading(rs.getInt("heading"));
          spawn1.setRespawnDelay(rs.getInt("respawnDelay"));
          spawn1.setLocation(0);
          _siegeGuardSpawn.add(spawn1);
          spawn1 = null;
        }
        else
        {
          _log.warning("Missing npc data in npc table for id: " + rs.getInt("npcId"));
        }
        template1 = null;
      }
      rs.close();
      statement.close();
      statement = null;
      rs = null;
    }
    catch (Exception e)
    {
      e1.printStackTrace();

      _log.warning("Error loading siege guard for fort " + getFort().getName() + ":" + e1);
    }
    finally
    {
      try {
        con.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
      con = null;
    }
  }

  private void saveSiegeGuard(int x, int y, int z, int heading, int npcId, int isHire)
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("Insert Into fort_siege_guards (fortId, npcId, x, y, z, heading, respawnDelay, isHired) Values (?, ?, ?, ?, ?, ?, ?, ?)");
      statement.setInt(1, getFort().getFortId());
      statement.setInt(2, npcId);
      statement.setInt(3, x);
      statement.setInt(4, y);
      statement.setInt(5, z);
      statement.setInt(6, heading);
      if (isHire == 1)
      {
        statement.setInt(7, 0);
      }
      else
      {
        statement.setInt(7, 600);
      }
      statement.setInt(8, isHire);
      statement.execute();
      statement.close();
      statement = null;
    }
    catch (Exception e)
    {
      e1.printStackTrace();

      _log.warning("Error adding siege guard for fort " + getFort().getName() + ":" + e1);
    }
    finally
    {
      try {
        con.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
      con = null;
    }
  }

  public final Fort getFort()
  {
    return _fort;
  }

  public final List<L2Spawn> getSiegeGuardSpawn()
  {
    return _siegeGuardSpawn;
  }
}