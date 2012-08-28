package net.sf.l2j.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class SiegeGuardManager
{
  private static Logger _log = Logger.getLogger(SiegeGuardManager.class.getName());
  private Castle _castle;
  private List<L2Spawn> _siegeGuardSpawn = new FastList();

  public SiegeGuardManager(Castle castle)
  {
    _castle = castle;
  }

  public void addSiegeGuard(L2PcInstance activeChar, int npcId)
  {
    if (activeChar == null) return;
    addSiegeGuard(activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getHeading(), npcId);
  }

  public void addSiegeGuard(int x, int y, int z, int heading, int npcId)
  {
    saveSiegeGuard(x, y, z, heading, npcId, 0);
  }

  public void hireMerc(L2PcInstance activeChar, int npcId)
  {
    if (activeChar == null) return;
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
      PreparedStatement statement = con.prepareStatement("Delete From castle_siege_guards Where npcId = ? And x = ? AND y = ? AND z = ? AND isHired = 1");
      statement.setInt(1, npcId);
      statement.setInt(2, x);
      statement.setInt(3, y);
      statement.setInt(4, z);
      statement.execute();
      statement.close();
    }
    catch (Exception e)
    {
      _log.warning("Error deleting hired siege guard at " + x + ',' + y + ',' + z + ":" + e1);
    }
    finally {
      try {
        con.close();
      }
      catch (Exception e)
      {
      }
    }
  }

  public void removeMercs() {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("Delete From castle_siege_guards Where castleId = ? And isHired = 1");
      statement.setInt(1, getCastle().getCastleId());
      statement.execute();
      statement.close();
    }
    catch (Exception e)
    {
      _log.warning("Error deleting hired siege guard for castle " + getCastle().getName() + ":" + e1);
    }
    finally {
      try {
        con.close();
      }
      catch (Exception e)
      {
      }
    }
  }

  public void spawnSiegeGuard() {
    loadSiegeGuard();
    for (L2Spawn spawn : getSiegeGuardSpawn())
      if (spawn != null) spawn.init();
  }

  public void unspawnSiegeGuard()
  {
    for (L2Spawn spawn : getSiegeGuardSpawn())
    {
      if (spawn == null) {
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
      PreparedStatement statement = con.prepareStatement("SELECT * FROM castle_siege_guards Where castleId = ? And isHired = ?");
      statement.setInt(1, getCastle().getCastleId());
      if (getCastle().getOwnerId() > 0)
        statement.setInt(2, 1);
      else
        statement.setInt(2, 0);
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

          _siegeGuardSpawn.add(spawn1); continue;
        }

        _log.warning("Missing npc data in npc table for id: " + rs.getInt("npcId"));
      }

      statement.close();
    }
    catch (Exception e)
    {
      _log.warning("Error loading siege guard for castle " + getCastle().getName() + ":" + e1);
    }
    finally {
      try {
        con.close();
      }
      catch (Exception e)
      {
      }
    }
  }

  private void saveSiegeGuard(int x, int y, int z, int heading, int npcId, int isHire) {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("Insert Into castle_siege_guards (castleId, npcId, x, y, z, heading, respawnDelay, isHired) Values (?, ?, ?, ?, ?, ?, ?, ?)");
      statement.setInt(1, getCastle().getCastleId());
      statement.setInt(2, npcId);
      statement.setInt(3, x);
      statement.setInt(4, y);
      statement.setInt(5, z);
      statement.setInt(6, heading);
      if (isHire == 1)
        statement.setInt(7, 0);
      else
        statement.setInt(7, 600);
      statement.setInt(8, isHire);
      statement.execute();
      statement.close();
    }
    catch (Exception e)
    {
      _log.warning("Error adding siege guard for castle " + getCastle().getName() + ":" + e1);
    }
    finally {
      try {
        con.close();
      }
      catch (Exception e)
      {
      }
    }
  }

  public final Castle getCastle() {
    return _castle;
  }

  public final List<L2Spawn> getSiegeGuardSpawn()
  {
    return _siegeGuardSpawn;
  }
}