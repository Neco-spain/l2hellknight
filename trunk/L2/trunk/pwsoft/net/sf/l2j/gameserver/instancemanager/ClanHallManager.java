package net.sf.l2j.gameserver.instancemanager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import net.sf.l2j.util.log.AbstractLogger;
import scripts.zone.type.L2ClanHallZone;

public class ClanHallManager
{
  private static final Logger _log = AbstractLogger.getLogger(ClanHallManager.class.getName());
  private static ClanHallManager _instance;
  private Map<Integer, ClanHall> _clanHall;
  private Map<Integer, ClanHall> _freeClanHall;
  private boolean _loaded = false;

  public static ClanHallManager getInstance()
  {
    return _instance;
  }

  public static void init()
  {
    _instance = new ClanHallManager();
    _instance.load();
  }

  public boolean loaded()
  {
    return _loaded;
  }

  private final void load()
  {
    _clanHall = new FastMap();
    _freeClanHall = new FastMap();

    Connect con = null;
    PreparedStatement statement = null;
    ResultSet rs = null;
    try
    {
      L2Clan clan = null;
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      statement = con.prepareStatement("SELECT * FROM clanhall ORDER BY id");
      rs = statement.executeQuery();
      rs.setFetchSize(50);
      while (rs.next())
      {
        int id = rs.getInt("id");
        if (rs.getInt("ownerId") == 0) {
          _freeClanHall.put(Integer.valueOf(id), new ClanHall(id, rs.getString("name"), rs.getInt("ownerId"), rs.getInt("lease"), rs.getString("desc"), rs.getString("location"), 0L, rs.getInt("Grade"), rs.getBoolean("paid"))); continue;
        }

        clan = ClanTable.getInstance().getClan(rs.getInt("ownerId"));
        if ((clan != null) && (clan.isActive()))
        {
          _clanHall.put(Integer.valueOf(id), new ClanHall(id, rs.getString("name"), rs.getInt("ownerId"), rs.getInt("lease"), rs.getString("desc"), rs.getString("location"), rs.getLong("paidUntil"), rs.getInt("Grade"), rs.getBoolean("paid")));
          clan.setHasHideout(id); continue;
        }

        _freeClanHall.put(Integer.valueOf(id), new ClanHall(id, rs.getString("name"), rs.getInt("ownerId"), rs.getInt("lease"), rs.getString("desc"), rs.getString("location"), rs.getLong("paidUntil"), rs.getInt("Grade"), rs.getBoolean("paid")));
        ((ClanHall)_freeClanHall.get(Integer.valueOf(id))).free();
        AuctionManager.getInstance().initNPC(id);
      }

      _loaded = true;
    }
    catch (Exception e)
    {
      _log.warning("Exception: ClanHallManager.load(): " + e.getMessage());
      e.printStackTrace();
    }
    finally
    {
      Close.CSR(con, statement, rs);
    }
    _log.info("ClanHallManager: Loaded " + getClanHalls().size() + " owned and " + getFreeClanHalls().size() + " free clanhalls.");
  }

  public final Map<Integer, ClanHall> getFreeClanHalls()
  {
    return _freeClanHall;
  }

  public final Map<Integer, ClanHall> getClanHalls()
  {
    return _clanHall;
  }

  public final boolean isFree(int chId)
  {
    return _freeClanHall.containsKey(Integer.valueOf(chId));
  }

  public final synchronized void setFree(int chId)
  {
    _freeClanHall.put(Integer.valueOf(chId), _clanHall.get(Integer.valueOf(chId)));
    ClanTable.getInstance().getClan(((ClanHall)_freeClanHall.get(Integer.valueOf(chId))).getOwnerId()).setHasHideout(0);
    ((ClanHall)_freeClanHall.get(Integer.valueOf(chId))).free();
    _clanHall.remove(Integer.valueOf(chId));
  }

  public final synchronized void setOwner(int chId, L2Clan clan)
  {
    if (!_clanHall.containsKey(Integer.valueOf(chId)))
    {
      _clanHall.put(Integer.valueOf(chId), _freeClanHall.get(Integer.valueOf(chId)));
      _freeClanHall.remove(Integer.valueOf(chId));
    }
    else {
      ((ClanHall)_clanHall.get(Integer.valueOf(chId))).free();
    }ClanTable.getInstance().getClan(clan.getClanId()).setHasHideout(chId);
    ((ClanHall)_clanHall.get(Integer.valueOf(chId))).setOwner(clan);
  }

  public final ClanHall getClanHallById(int clanHallId)
  {
    if (_clanHall.containsKey(Integer.valueOf(clanHallId)))
      return (ClanHall)_clanHall.get(Integer.valueOf(clanHallId));
    if (_freeClanHall.containsKey(Integer.valueOf(clanHallId)))
      return (ClanHall)_freeClanHall.get(Integer.valueOf(clanHallId));
    return null;
  }

  public final ClanHall getNearbyClanHall(int x, int y, int maxDist)
  {
    L2ClanHallZone zone = null;

    for (Map.Entry ch : _clanHall.entrySet())
    {
      zone = ((ClanHall)ch.getValue()).getZone();
      if ((zone != null) && (zone.getDistanceToZone(x, y) < maxDist))
        return (ClanHall)ch.getValue();
    }
    for (Map.Entry ch : _freeClanHall.entrySet())
    {
      zone = ((ClanHall)ch.getValue()).getZone();
      if ((zone != null) && (zone.getDistanceToZone(x, y) < maxDist))
        return (ClanHall)ch.getValue();
    }
    return null;
  }

  public final ClanHall getClanHallByOwner(L2Clan clan)
  {
    for (Map.Entry ch : _clanHall.entrySet())
    {
      if (clan.getClanId() == ((ClanHall)ch.getValue()).getOwnerId())
        return (ClanHall)ch.getValue();
    }
    return null;
  }
}