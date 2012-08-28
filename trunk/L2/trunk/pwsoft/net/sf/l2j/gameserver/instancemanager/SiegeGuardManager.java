package net.sf.l2j.gameserver.instancemanager;

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
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import net.sf.l2j.util.log.AbstractLogger;

public class SiegeGuardManager
{
  private static Logger _log = AbstractLogger.getLogger(SiegeGuardManager.class.getName());
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
    Connect con = null;
    PreparedStatement st = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      st = con.prepareStatement("DELETE FROM castle_siege_guards WHERE npcId = ? And x = ? AND y = ? AND z = ? AND isHired = 1");
      st.setInt(1, npcId);
      st.setInt(2, x);
      st.setInt(3, y);
      st.setInt(4, z);
      st.execute();
    }
    catch (Exception e1)
    {
      _log.warning("Error deleting hired siege guard at " + x + ',' + y + ',' + z + ":" + e1);
    }
    finally
    {
      Close.CS(con, st);
    }
  }

  public void removeMercs()
  {
    Connect con = null;
    PreparedStatement st = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      st = con.prepareStatement("DELETE FROM castle_siege_guards WHERE castleId = ? AND isHired = 1");
      st.setInt(1, getCastle().getCastleId());
      st.execute();
    }
    catch (Exception e1)
    {
      _log.warning("Error deleting hired siege guard for castle " + getCastle().getName() + ":" + e1);
    }
    finally
    {
      Close.CS(con, st);
    }
  }

  public void spawnSiegeGuard()
  {
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
    Connect con = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      st = con.prepareStatement("SELECT * FROM castle_siege_guards WHERE castleId = ? AND isHired = ?");
      st.setInt(1, getCastle().getCastleId());
      if (getCastle().getOwnerId() > 0)
        st.setInt(2, 1);
      else
        st.setInt(2, 0);
      rs = st.executeQuery();
      rs.setFetchSize(50);

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

      Close.S(st);
    }
    catch (Exception e1)
    {
      _log.warning("Error loading siege guard for castle " + getCastle().getName() + ":" + e1);
    }
    finally
    {
      Close.CSR(con, st, rs);
    }
  }

  private void saveSiegeGuard(int x, int y, int z, int heading, int npcId, int isHire)
  {
    Connect con = null;
    PreparedStatement st = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      st = con.prepareStatement("INSERT INTO castle_siege_guards (castleId, npcId, x, y, z, heading, respawnDelay, isHired) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
      st.setInt(1, getCastle().getCastleId());
      st.setInt(2, npcId);
      st.setInt(3, x);
      st.setInt(4, y);
      st.setInt(5, z);
      st.setInt(6, heading);
      if (isHire == 1)
        st.setInt(7, 0);
      else
        st.setInt(7, 600);
      st.setInt(8, isHire);
      st.execute();
    }
    catch (Exception e1)
    {
      _log.warning("Error adding siege guard for castle " + getCastle().getName() + ":" + e1);
    }
    finally
    {
      Close.CS(con, st);
    }
  }

  public final Castle getCastle()
  {
    return _castle;
  }

  public final List<L2Spawn> getSiegeGuardSpawn()
  {
    return _siegeGuardSpawn;
  }
}