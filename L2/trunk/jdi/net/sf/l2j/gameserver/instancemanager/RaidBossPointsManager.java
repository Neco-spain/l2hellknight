package net.sf.l2j.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastList.Node;
import javolution.util.FastMap;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class RaidBossPointsManager
{
  private static final Logger _log = Logger.getLogger(RaidBossPointsManager.class.getName());
  protected static FastMap<Integer, Map<Integer, Integer>> _list;
  private static final Comparator<Map.Entry<Integer, Integer>> _comparator = new Comparator()
  {
    public int compare(Map.Entry<Integer, Integer> entry, Map.Entry<Integer, Integer> entry1)
    {
      return ((Integer)entry.getValue()).intValue() < ((Integer)entry1.getValue()).intValue() ? 1 : ((Integer)entry.getValue()).equals(entry1.getValue()) ? 0 : -1;
    }
  };

  public static final void init()
  {
    _list = new FastMap();
    FastList _chars = new FastList();
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT * FROM `character_raid_points`");
      rset = statement.executeQuery();
      while (rset.next())
      {
        _chars.add(Integer.valueOf(rset.getInt("charId")));
      }
      rset.close();
      statement.close();
      n = _chars.head(); for (FastList.Node end = _chars.tail(); (n = n.getNext()) != end; )
      {
        int charId = ((Integer)n.getValue()).intValue();
        FastMap values = new FastMap();
        statement = con.prepareStatement("SELECT * FROM `character_raid_points` WHERE `charId`=?");
        statement.setInt(1, charId);
        rset = statement.executeQuery();
        while (rset.next())
        {
          values.put(Integer.valueOf(rset.getInt("boss_id")), Integer.valueOf(rset.getInt("points")));
        }
        rset.close();
        statement.close();
        _list.put(Integer.valueOf(charId), values);
      }
    }
    catch (SQLException e)
    {
      PreparedStatement statement;
      ResultSet rset;
      FastList.Node n;
      e.printStackTrace();
      _log.warning("RaidPointsManager: Couldnt load raid points ");
    }
    catch (Exception e)
    {
      e.printStackTrace();
      _log.warning(e.getMessage());
    }
    finally
    {
      try
      {
        con.close();
      }
      catch (SQLException e) {
        e.printStackTrace();
      }
      con = null;
    }
  }

  public static final void updatePointsInDB(L2PcInstance player, int raidId, int points)
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("REPLACE INTO character_raid_points (`charId`,`boss_id`,`points`) VALUES (?,?,?)");
      statement.setInt(1, player.getObjectId());
      statement.setInt(2, raidId);
      statement.setInt(3, points);
      statement.executeUpdate();
      statement.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      _log.log(Level.WARNING, "could not update char raid points:", e);
    }
    finally
    {
      try
      {
        con.close();
      }
      catch (SQLException e) {
        e.printStackTrace();
      }
      con = null;
    }
  }

  public static final void addPoints(L2PcInstance player, int bossId, int points)
  {
    int ownerId = player.getObjectId();
    Map tmpPoint = new FastMap();
    if (_list == null)
      _list = new FastMap();
    tmpPoint = (Map)_list.get(Integer.valueOf(ownerId));
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
    _list.remove(Integer.valueOf(ownerId));
    _list.put(Integer.valueOf(ownerId), tmpPoint);
  }

  public static final int getPointsByOwnerId(int ownerId)
  {
    Map tmpPoint = new FastMap();
    if (_list == null)
      _list = new FastMap();
    tmpPoint = (Map)_list.get(Integer.valueOf(ownerId));
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
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("DELETE from character_raid_points WHERE charId > 0");
      statement.executeUpdate();
      statement.close();
      _list.clear();
      _list = new FastMap();
    }
    catch (Exception e)
    {
      e.printStackTrace();

      _log.log(Level.WARNING, "could not clean raid points: ", e);
    }
    finally
    {
      try
      {
        con.close();
      }
      catch (SQLException e) {
        e.printStackTrace();
      }
      con = null;
    }
  }

  public static final int calculateRanking(int playerObjId)
  {
    Map tmpRanking = new FastMap();
    Map tmpPoints = new FastMap();

    for (Iterator i$ = _list.keySet().iterator(); i$.hasNext(); ) { int ownerId = ((Integer)i$.next()).intValue();

      int totalPoints = getPointsByOwnerId(ownerId);
      if (totalPoints != 0)
      {
        tmpPoints.put(Integer.valueOf(ownerId), Integer.valueOf(totalPoints));
      }
    }
    ArrayList list = new ArrayList(tmpPoints.entrySet());

    Collections.sort(list, _comparator);

    int ranking = 1;
    for (Map.Entry entry : list) {
      tmpRanking.put(entry.getKey(), Integer.valueOf(ranking++));
    }
    if (tmpRanking.containsKey(Integer.valueOf(playerObjId)))
      return ((Integer)tmpRanking.get(Integer.valueOf(playerObjId))).intValue();
    return 0;
  }
}