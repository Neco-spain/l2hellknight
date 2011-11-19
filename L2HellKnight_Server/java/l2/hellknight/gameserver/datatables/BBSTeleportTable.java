package l2.hellknight.gameserver.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;
import javolution.util.FastMap;
import l2.hellknight.L2DatabaseFactory;

public class BBSTeleportTable
{
  private static Logger _log = Logger.getLogger(BBSTeleportTable.class.getName());

  private FastMap<Integer, String> TeleportGroups = null;
  private FastMap<Integer, String> TeleportNames = null;
  private FastMap<Integer, Integer> TeleportAssociates = null;
  private FastMap<Integer, teleCoord> TeleportCoords = null;

  private BBSTeleportTable()
  {
    this.TeleportGroups = new FastMap<Integer, String>();
    this.TeleportCoords = new FastMap<Integer, teleCoord>();
    this.TeleportNames = new FastMap<Integer, String>();
    this.TeleportAssociates = new FastMap<Integer, Integer>();
    load();
  }

  public static BBSTeleportTable getInstance()
  {
    return SingletonHolder._instance;
  }

  private void load()
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement stmt = con.prepareStatement("SELECT * FROM bbs_teleport");
      ResultSet rs = stmt.executeQuery();
      while (rs.next())
      {
        switch (rs.getInt("type"))
        {
        case 0:
          this.TeleportGroups.put(Integer.valueOf(rs.getInt("id")), rs.getString("desription"));
          break;
        case 1:
          teleCoord tc = new teleCoord();
          tc.x = rs.getInt("x");
          tc.y = rs.getInt("y");
          tc.z = rs.getInt("z");
          this.TeleportCoords.put(Integer.valueOf(rs.getInt("id")), tc);
          this.TeleportNames.put(Integer.valueOf(rs.getInt("id")), rs.getString("desription"));
        }

        this.TeleportAssociates.put(Integer.valueOf(rs.getInt("id")), Integer.valueOf(rs.getInt("sub_id")));
      }
      rs.close();
      stmt.close();
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
    finally
    {
      L2DatabaseFactory.close(con);
      _log.info("BBSTeleportTable: Loaded " + this.TeleportGroups.size() + " teleport groups.");
      _log.info("BBSTeleportTable: Loaded " + this.TeleportCoords.size() + " teleport coordinates.");
    }
  }

  public teleCoord getTeleportXYZ(int id)
  {
    teleCoord tc = new teleCoord();
    tc.x = this.TeleportCoords.get(Integer.valueOf(id)).x;
    tc.y = this.TeleportCoords.get(Integer.valueOf(id)).y;
    tc.z = this.TeleportCoords.get(Integer.valueOf(id)).z;
    return tc;
  }

  public int getTeleportSubId(int main_id)
  {
    return this.TeleportAssociates.get(Integer.valueOf(main_id)).intValue();
  }

  public void reload()
  {
    this.TeleportGroups.clear();
    this.TeleportCoords.clear();
    this.TeleportNames.clear();
    this.TeleportAssociates.clear();
    load();
  }

  public String getTeleportsName(int id)
  {
    return this.TeleportNames.get(Integer.valueOf(id));
  }

  public int getTeleportX(int id)
  {
    return this.TeleportCoords.get(Integer.valueOf(id)).x;
  }

  public int getTeleportY(int id)
  {
    return this.TeleportCoords.get(Integer.valueOf(id)).y;
  }

  public int getTeleportZ(int id)
  {
    return this.TeleportCoords.get(Integer.valueOf(id)).z;
  }

  public String getTeleportGrpName(int id)
  {
    return this.TeleportGroups.get(Integer.valueOf(id));
  }

  public FastMap<Integer, String> getTeleportNames()
  {
    return this.TeleportNames;
  }

  public FastMap<Integer, String> getTeleportGroups()
  {
    return this.TeleportGroups;
  }

  public FastMap<Integer, teleCoord> getTeleportCoords()
  {
    return this.TeleportCoords;
  }

  private static class SingletonHolder
  {
    protected static final BBSTeleportTable _instance = new BBSTeleportTable();
  }

  public static class teleCoord
  {
    int x;
    int y;
    int z;
  }
}