package net.sf.l2j.gameserver.datatables;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastMap.Entry;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.L2Skill;

public class CharSchemesTable
{
  private static FastMap<Integer, FastMap<String, FastList<L2Skill>>> _schemesTable;
  private static CharSchemesTable _instance = null;

  private static Logger _log = Logger.getLogger(CharSchemesTable.class.getName());
  private static final String SQL_LOAD_SCHEME = "SELECT * FROM character_buff_profiles WHERE ownerId=?";
  private static final String SQL_DELETE_SCHEME = "DELETE FROM character_buff_profiles WHERE ownerId=?";
  private static final String SQL_INSERT_SCHEME = "INSERT INTO character_buff_profiles (ownerId, id, level, scheme) VALUES (?,?,?,?)";

  public static CharSchemesTable getInstance()
  {
    if (_instance == null)
      _instance = new CharSchemesTable();
    return _instance;
  }

  public CharSchemesTable()
  {
    _schemesTable = new FastMap();
  }

  public void clearDB()
  {
    if (_schemesTable.isEmpty()) {
      return;
    }
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      e = _schemesTable.head();
      for (FastMap.Entry end = _schemesTable.tail(); (e = e.getNext()) != end; )
      {
        PreparedStatement statement = con.prepareStatement("DELETE FROM character_buff_profiles WHERE ownerId=?");

        statement.setInt(1, ((Integer)e.getKey()).intValue());
        statement.execute();
      }
    }
    catch (Exception e)
    {
      FastMap.Entry e;
      _log.warning("CharSchemesTable: Error while trying to delete schemes");
    }
    finally
    {
      try
      {
        con.close();
      }
      catch (Exception e)
      {
      }
    }
  }

  public FastMap<String, FastList<L2Skill>> getAllSchemes(int playerId)
  {
    return (FastMap)_schemesTable.get(Integer.valueOf(playerId));
  }

  public FastList<L2Skill> getScheme(int playerid, String scheme_key)
  {
    if (_schemesTable.get(Integer.valueOf(playerid)) == null)
      return null;
    return (FastList)((FastMap)_schemesTable.get(Integer.valueOf(playerid))).get(scheme_key);
  }

  public boolean getSchemeContainsSkill(int playerId, String scheme_key, int skillId)
  {
    for (L2Skill sk : getScheme(playerId, scheme_key))
    {
      if (sk.getId() == skillId)
        return true;
    }
    return false;
  }

  public FastMap<Integer, FastMap<String, FastList<L2Skill>>> getSchemesTable()
  {
    return _schemesTable;
  }

  public void loadScheme(int objectId)
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT * FROM character_buff_profiles WHERE ownerId=?");
      statement.setInt(1, objectId);

      ResultSet rs = statement.executeQuery();

      FastMap map = new FastMap();

      while (rs.next())
      {
        int skillId = rs.getInt("id");
        int skillLevel = rs.getInt("level");
        String scheme = rs.getString("scheme");

        if ((!map.containsKey(scheme)) && (map.size() <= 4))
        {
          map.put(scheme, new FastList());
        }
        if ((map.get(scheme) != null) && (((FastList)map.get(scheme)).size() < 40))
        {
          ((FastList)map.get(scheme)).add(SkillTable.getInstance().getInfo(skillId, skillLevel));
        }

      }

      if (!map.isEmpty()) {
        _schemesTable.put(Integer.valueOf(objectId), map);
      }
      statement.close();
      rs.close();
    }
    catch (Exception e)
    {
      _log.warning("Error trying to load buff scheme from object id: " + objectId);
    }
    finally
    {
      try
      {
        con.close();
      }
      catch (Exception e)
      {
      }
    }
  }

  public void onPlayerLogin(int playerId) {
    if (_schemesTable.get(Integer.valueOf(playerId)) == null)
      loadScheme(playerId);
  }

  public void onServerShutdown()
  {
    clearDB();
    saveDataToDB();
  }

  public void saveDataToDB()
  {
    if (_schemesTable.isEmpty()) {
      return;
    }
    Connection con = null;
    int count = 0;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      e = _schemesTable.head();
      for (FastMap.Entry end = _schemesTable.tail(); (e = e.getNext()) != end; )
      {
        if ((e.getValue() == null) || (((FastMap)e.getValue()).isEmpty()))
          continue;
        FastMap.Entry a = ((FastMap)e.getValue()).head();
        for (FastMap.Entry enda = ((FastMap)e.getValue()).tail(); (a = a.getNext()) != enda; )
        {
          if ((a.getValue() == null) || (((FastList)a.getValue()).isEmpty())) {
            continue;
          }
          for (L2Skill sk : (FastList)a.getValue())
          {
            PreparedStatement statement = con.prepareStatement("INSERT INTO character_buff_profiles (ownerId, id, level, scheme) VALUES (?,?,?,?)");

            statement.setInt(1, ((Integer)e.getKey()).intValue());
            statement.setInt(2, sk.getId());
            statement.setInt(3, sk.getLevel());
            statement.setString(4, (String)a.getKey());
            statement.execute();
          }
        }
        count++;
      }
    }
    catch (Exception e)
    {
      FastMap.Entry e;
      _log.warning("CharSchemesTable: Error while trying to delete schemes");
    }
    finally
    {
      try
      {
        con.close();
      }
      catch (Exception e) {
      }
      System.out.println("CharSchemeTable: Saved " + String.valueOf(new StringBuilder().append(count).append(" scheme(s)").toString()));
    }
  }

  public void setScheme(int playerId, String schemeKey, FastList<L2Skill> list)
  {
    ((FastMap)_schemesTable.get(Integer.valueOf(playerId))).put(schemeKey, list);
  }
}