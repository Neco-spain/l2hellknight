package net.sf.l2j.gameserver.communitybbs.Manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.communitybbs.BB.Forum;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class ForumsBBSManager extends BaseBBSManager
{
  private static Logger _log = Logger.getLogger(ForumsBBSManager.class.getName());
  private Map<Integer, Forum> _root;
  private List<Forum> _table;
  private static ForumsBBSManager _instance;
  private int _lastid = 1;

  public static ForumsBBSManager getInstance()
  {
    if (_instance == null)
    {
      _instance = new ForumsBBSManager();
      _instance.load();
    }
    return _instance;
  }

  public ForumsBBSManager()
  {
    _root = new FastMap();
    _table = new FastList();
  }

  public void addForum(Forum ff)
  {
    _table.add(ff);

    if (ff.getID() > _lastid)
    {
      _lastid = ff.getID();
    }
  }

  private void load()
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT forum_id FROM forums WHERE forum_type=0");
      ResultSet result = statement.executeQuery();
      while (result.next())
      {
        Forum f = new Forum(Integer.parseInt(result.getString("forum_id")), null);
        _root.put(Integer.valueOf(Integer.parseInt(result.getString("forum_id"))), f);
      }
      result.close();
      statement.close();
    }
    catch (Exception e)
    {
      _log.warning("data error on Forum (root): " + e);
      e.printStackTrace();
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

  public void parsecmd(String command, L2PcInstance activeChar)
  {
  }

  public Forum getForumByName(String Name)
  {
    for (Forum f : _table)
    {
      if (f.getName().equals(Name))
      {
        return f;
      }
    }

    return null;
  }

  public Forum createNewForum(String name, Forum parent, int type, int perm, int oid)
  {
    Forum forum = new Forum(name, parent, type, perm, oid);
    forum.insertindb();
    return forum;
  }

  public int getANewID()
  {
    _lastid += 1;
    return _lastid;
  }

  public Forum getForumByID(int idf)
  {
    for (Forum f : _table)
    {
      if (f.getID() == idf)
      {
        return f;
      }
    }
    return null;
  }

  public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
  {
  }
}