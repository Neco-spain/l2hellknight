package net.sf.l2j.gameserver.datatables;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.L2TeleportLocation;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import net.sf.l2j.util.log.AbstractLogger;

public class TeleportLocationTable
{
  private static Logger _log = AbstractLogger.getLogger(TeleportLocationTable.class.getName());
  private static TeleportLocationTable _instance;
  private static FastMap<Integer, L2TeleportLocation> _teleports = new FastMap().shared("TeleportLocationTable._teleports");

  public static TeleportLocationTable getInstance()
  {
    return _instance;
  }

  public static void init()
  {
    _instance = new TeleportLocationTable();
  }

  public TeleportLocationTable()
  {
    load();
  }

  public void reloadAll()
  {
    load();
  }

  public void load()
  {
    Connect con = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      st = con.prepareStatement("SELECT Description, id, loc_x, loc_y, loc_z, price, fornoble FROM teleport");
      rs = st.executeQuery();
      rs.setFetchSize(50);

      while (rs.next())
      {
        L2TeleportLocation teleport = new L2TeleportLocation();

        teleport.setTeleId(rs.getInt("id"));
        teleport.setLocX(rs.getInt("loc_x"));
        teleport.setLocY(rs.getInt("loc_y"));
        teleport.setLocZ(rs.getInt("loc_z"));
        teleport.setPrice(rs.getInt("price"));
        teleport.setIsForNoble(rs.getInt("fornoble") == 1);

        _teleports.put(Integer.valueOf(teleport.getTeleId()), teleport);
      }
    }
    catch (Exception e)
    {
      _log.warning("error while creating teleport table " + e);
    }
    finally
    {
      Close.CSR(con, st, rs);
    }
    _log.config("Loading TeleportLocationTable... total " + _teleports.size() + " Teleports.");
  }

  public L2TeleportLocation getTemplate(int id)
  {
    return (L2TeleportLocation)_teleports.get(Integer.valueOf(id));
  }
}