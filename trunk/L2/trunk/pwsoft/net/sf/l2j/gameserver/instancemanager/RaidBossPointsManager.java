package net.sf.l2j.gameserver.instancemanager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastList.Node;
import javolution.util.FastMap;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import net.sf.l2j.util.log.AbstractLogger;

public class RaidBossPointsManager
{
  private static final Logger _log = AbstractLogger.getLogger(RaidBossPointsManager.class.getName());
  protected static FastMap<Integer, Map<Integer, Integer>> _points;
  protected static FastMap<Integer, Map<Integer, Integer>> _list;

  public static final void init()
  {
    _list = new FastMap();
    FastList _chars = new FastList();
    Connect con = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      st = con.prepareStatement("SELECT * FROM `character_raid_points`");
      rs = st.executeQuery();
      rs.setFetchSize(50);
      while (rs.next())
      {
        _chars.add(Integer.valueOf(rs.getInt("charId")));
      }
      Close.SR(st, rs);
      n = _chars.head(); for (FastList.Node end = _chars.tail(); (n = n.getNext()) != end; )
      {
        int charId = ((Integer)n.getValue()).intValue();
        FastMap values = new FastMap();
        st = con.prepareStatement("SELECT * FROM `character_raid_points` WHERE `charId`=?");
        st.setInt(1, charId);
        rs = st.executeQuery();
        rs.setFetchSize(50);
        while (rs.next())
        {
          values.put(Integer.valueOf(rs.getInt("boss_id")), Integer.valueOf(rs.getInt("points")));
        }
        Close.SR(st, rs);
        _list.put(Integer.valueOf(charId), values);
      }
    }
    catch (SQLException e)
    {
      FastList.Node n;
      _log.warning("RaidPointsManager: Couldnt load raid points ");
    }
    catch (Exception e)
    {
      _log.warning(e.getMessage());
    }
    finally
    {
      _chars.clear();
      Close.CSR(con, st, rs);
    }
  }

  public static final void loadPoints(L2PcInstance player) {
    if (_points == null)
      _points = new FastMap();
    Connect con = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    try
    {
      FastMap tmpScore = new FastMap();
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      st = con.prepareStatement("SELECT boss_id,points FROM character_raid_points WHERE charId=?");
      st.setInt(1, player.getObjectId());
      rs = st.executeQuery();
      rs.setFetchSize(50);
      while (rs.next())
      {
        int raidId = rs.getInt("boss_id");
        int points = rs.getInt("points");
        tmpScore.put(Integer.valueOf(raidId), Integer.valueOf(points));
      }
      Close.SR(st, rs);
      _points.put(Integer.valueOf(player.getObjectId()), tmpScore);
      tmpScore.clear();
    }
    catch (SQLException e)
    {
      _log.warning("RaidPointsManager: Couldnt load raid points for character :" + player.getName());
    }
    catch (Exception e)
    {
      _log.warning(e.getMessage());
    }
    finally
    {
      Close.CSR(con, st, rs);
    }
  }

  public static final void updatePointsInDB(L2PcInstance player, int raidId, int points)
  {
    Connect con = null;
    PreparedStatement st = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      st = con.prepareStatement("REPLACE INTO character_raid_points (`charId`,`boss_id`,`points`) VALUES (?,?,?)");
      st.setInt(1, player.getObjectId());
      st.setInt(2, raidId);
      st.setInt(3, points);
      st.executeUpdate();
    }
    catch (Exception e)
    {
      _log.log(Level.WARNING, "could not update char raid points:", e);
    }
    finally
    {
      Close.CS(con, st);
    }
  }

  public static final void addPoints(L2PcInstance player, int bossId, int points)
  {
    int ownerId = player.getObjectId();
    Map tmpPoint = new FastMap();
    if (_points == null)
      _points = new FastMap();
    tmpPoint = (Map)_points.get(Integer.valueOf(ownerId));
    if ((tmpPoint == null) || (tmpPoint.isEmpty()))
    {
      tmpPoint = new FastMap();
      tmpPoint.put(Integer.valueOf(bossId), Integer.valueOf(points));
      updatePointsInDB(player, bossId, points);
    }
    else
    {
      int currentPoins = tmpPoint.containsKey(Integer.valueOf(bossId)) ? ((Integer)tmpPoint.get(Integer.valueOf(bossId))).intValue() : 0;
      tmpPoint.remove(Integer.valueOf(bossId));
      tmpPoint.put(Integer.valueOf(bossId), Integer.valueOf(currentPoins == 0 ? points : currentPoins + points));
      updatePointsInDB(player, bossId, currentPoins == 0 ? points : currentPoins + points);
    }
    _points.remove(Integer.valueOf(ownerId));
    _points.put(Integer.valueOf(ownerId), tmpPoint);
    _list.remove(Integer.valueOf(ownerId));
    _list.put(Integer.valueOf(ownerId), tmpPoint);
    tmpPoint.clear();
  }

  public static final int getPointsByOwnerId(int ownerId)
  {
    Map tmpPoint = new FastMap();
    if (_points == null)
      _points = new FastMap();
    tmpPoint = (Map)_points.get(Integer.valueOf(ownerId));
    int totalPoints = 0;

    if ((tmpPoint == null) || (tmpPoint.isEmpty())) {
      return 0;
    }
    for (Iterator i$ = tmpPoint.keySet().iterator(); i$.hasNext(); ) { int bossId = ((Integer)i$.next()).intValue();

      totalPoints += ((Integer)tmpPoint.get(Integer.valueOf(bossId))).intValue();
    }
    return totalPoints;
  }

  public static final Map<Integer, Integer> getList(L2PcInstance player)
  {
    return (Map)_list.get(Integer.valueOf(player.getObjectId()));
  }

  public static final void cleanUp()
  {
    Connect con = null;
    PreparedStatement st = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      st = con.prepareStatement("DELETE from character_raid_points WHERE charId > 0");
      st.executeUpdate();
      _points.clear();
      _points = new FastMap();
      _list.clear();
      _list = new FastMap();
    }
    catch (Exception e) {
      _log.log(Level.WARNING, "could not clean raid points: ", e);
    }
    finally
    {
      Close.CS(con, st);
    }
  }

  public static final int calculateRanking(L2PcInstance player)
  {
    Map tmpRanking = new FastMap();
    Map tmpPoints = new FastMap();

    for (Iterator i$ = _list.keySet().iterator(); i$.hasNext(); ) { int ownerId = ((Integer)i$.next()).intValue();

      int totalPoints = getPointsByOwnerId(ownerId);
      if (totalPoints != 0)
      {
        tmpRanking.put(Integer.valueOf(ownerId), Integer.valueOf(totalPoints));
      }
    }
    Vector list = new Vector(tmpRanking.entrySet());
    tmpRanking.clear();

    Collections.sort(list, new Comparator()
    {
      public int compare(Map.Entry<Integer, Integer> entry, Map.Entry<Integer, Integer> entry1) {
        return ((Integer)entry.getValue()).intValue() < ((Integer)entry1.getValue()).intValue() ? 1 : ((Integer)entry.getValue()).equals(entry1.getValue()) ? 0 : -1;
      }
    });
    int ranking = 0;
    for (Map.Entry entry : list)
    {
      Map tmpPoint = new FastMap();

      if (tmpPoints.get(entry.getKey()) != null) {
        tmpPoint = (Map)tmpPoints.get(entry.getKey());
      }
      tmpPoint.put(Integer.valueOf(-1), Integer.valueOf(ranking++));

      tmpPoints.put(entry.getKey(), tmpPoint);
    }
    Map rank = (Map)tmpPoints.get(Integer.valueOf(player.getObjectId()));
    if (rank != null)
      return ((Integer)rank.get(Integer.valueOf(-1))).intValue();
    return 0;
  }
}