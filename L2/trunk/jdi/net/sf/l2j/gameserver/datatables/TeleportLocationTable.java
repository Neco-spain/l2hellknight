package net.sf.l2j.gameserver.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.L2TeleportLocation;

public class TeleportLocationTable
{
  private static Logger _log = Logger.getLogger(TeleportLocationTable.class.getName());
  private static TeleportLocationTable _instance;
  private Map<Integer, L2TeleportLocation> _teleports;

  public static TeleportLocationTable getInstance()
  {
    if (_instance == null)
    {
      _instance = new TeleportLocationTable();
    }
    return _instance;
  }

  private TeleportLocationTable()
  {
    reloadAll();
  }

  public void reloadAll() {
    _teleports = new FastMap();

    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT Description, id, loc_x, loc_y, loc_z, price, fornoble FROM teleport");
      ResultSet rset = statement.executeQuery();

      while (rset.next())
      {
        L2TeleportLocation teleport = new L2TeleportLocation();

        teleport.setTeleId(rset.getInt("id"));
        teleport.setLocX(rset.getInt("loc_x"));
        teleport.setLocY(rset.getInt("loc_y"));
        teleport.setLocZ(rset.getInt("loc_z"));
        teleport.setPrice(rset.getInt("price"));
        teleport.setIsForNoble(rset.getInt("fornoble") == 1);

        _teleports.put(Integer.valueOf(teleport.getTeleId()), teleport);
      }

      rset.close();
      statement.close();

      _log.config("TeleportLocationTable: Loaded " + _teleports.size() + " Teleport Location Templates.");
    }
    catch (Exception e)
    {
      _log.warning("error while creating teleport table " + e);
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  public L2TeleportLocation getTemplate(int id) {
    return (L2TeleportLocation)_teleports.get(Integer.valueOf(id));
  }
}