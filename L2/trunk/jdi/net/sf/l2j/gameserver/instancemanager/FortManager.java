package net.sf.l2j.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.entity.Fort;

public class FortManager
{
  protected static final Logger _log = Logger.getLogger(FortManager.class.getName());
  private static FortManager _instance;
  private List<Fort> _forts;

  public static final FortManager getInstance()
  {
    if (_instance == null)
    {
      _log.info("Initializing FortManager");
      _instance = new FortManager();
      _instance.load();
    }
    return _instance;
  }

  public final int findNearestFortIndex(L2Object obj)
  {
    int index = getFortIndex(obj);
    Fort fort;
    if (index < 0)
    {
      double closestDistance = 99999999.0D;

      for (int i = 0; i < getForts().size(); i++)
      {
        Fort fort = (Fort)getForts().get(i);
        if (fort == null)
        {
          continue;
        }
        double distance = fort.getDistance(obj);
        if (closestDistance <= distance)
          continue;
        closestDistance = distance;
        index = i;
      }

      fort = null;
    }
    return index;
  }

  private final void load()
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("Select id from fort order by id");
      ResultSet rs = statement.executeQuery();

      while (rs.next())
      {
        getForts().add(new Fort(rs.getInt("id")));
      }

      rs.close();
      statement.close();

      _log.info("Loaded: " + getForts().size() + " fortress");
    }
    catch (Exception e)
    {
      _log.warning("Exception: loadFortData(): " + e.getMessage());
      e.printStackTrace();
    }
    finally
    {
      try
      {
        con.close();
      }
      catch (SQLException e)
      {
        e.printStackTrace();
      }
      con = null;
    }
  }

  public final Fort getFortById(int fortId)
  {
    for (Fort f : getForts())
    {
      if (f.getFortId() == fortId)
        return f;
    }
    return null;
  }

  public final Fort getFortByOwner(L2Clan clan)
  {
    for (Fort f : getForts())
    {
      if (f.getOwnerId() == clan.getClanId())
        return f;
    }
    return null;
  }

  public final Fort getFort(String name)
  {
    for (Fort f : getForts())
    {
      if (f.getName().equalsIgnoreCase(name.trim()))
        return f;
    }
    return null;
  }

  public final Fort getFort(int x, int y, int z)
  {
    for (Fort f : getForts())
    {
      if (f.checkIfInZone(x, y, z))
        return f;
    }
    return null;
  }

  public final Fort getFort(L2Object activeObject)
  {
    return getFort(activeObject.getX(), activeObject.getY(), activeObject.getZ());
  }

  public final int getFortIndex(int fortId)
  {
    for (int i = 0; i < getForts().size(); i++)
    {
      Fort fort = (Fort)getForts().get(i);
      if ((fort == null) || (fort.getFortId() != fortId))
        continue;
      fort = null;
      return i;
    }

    return -1;
  }

  public final int getFortIndex(L2Object activeObject)
  {
    return getFortIndex(activeObject.getX(), activeObject.getY(), activeObject.getZ());
  }

  public final int getFortIndex(int x, int y, int z)
  {
    for (int i = 0; i < getForts().size(); i++)
    {
      Fort fort = (Fort)getForts().get(i);
      if ((fort == null) || (!fort.checkIfInZone(x, y, z)))
        continue;
      fort = null;
      return i;
    }

    return -1;
  }

  public final List<Fort> getForts()
  {
    if (_forts == null)
    {
      _forts = new FastList();
    }
    return _forts;
  }
}