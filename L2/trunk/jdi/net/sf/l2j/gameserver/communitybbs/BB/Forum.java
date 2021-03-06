package net.sf.l2j.gameserver.communitybbs.BB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.communitybbs.Manager.ForumsBBSManager;
import net.sf.l2j.gameserver.communitybbs.Manager.TopicBBSManager;

public class Forum
{
  public static final int ROOT = 0;
  public static final int NORMAL = 1;
  public static final int CLAN = 2;
  public static final int MEMO = 3;
  public static final int MAIL = 4;
  public static final int INVISIBLE = 0;
  public static final int ALL = 1;
  public static final int CLANMEMBERONLY = 2;
  public static final int OWNERONLY = 3;
  private static Logger _log = Logger.getLogger(Forum.class.getName());
  private List<Forum> _children;
  private Map<Integer, Topic> _topic;
  private int _forumId;
  private String _forumName;
  private int _forumType;
  private int _forumPost;
  private int _forumPerm;
  private Forum _fParent;
  private int _ownerID;
  private boolean _loaded = false;

  public Forum(int Forumid, Forum FParent)
  {
    _forumId = Forumid;
    _fParent = FParent;
    _children = new FastList();
    _topic = new FastMap();

    ForumsBBSManager.getInstance().addForum(this);
  }

  public Forum(String name, Forum parent, int type, int perm, int OwnerID)
  {
    _forumName = name;
    _forumId = ForumsBBSManager.getInstance().getANewID();

    _forumType = type;
    _forumPost = 0;
    _forumPerm = perm;
    _fParent = parent;
    _ownerID = OwnerID;
    _children = new FastList();
    _topic = new FastMap();
    parent._children.add(this);
    ForumsBBSManager.getInstance().addForum(this);
    _loaded = true;
  }

  private void load()
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT * FROM forums WHERE forum_id=?");
      statement.setInt(1, _forumId);
      ResultSet result = statement.executeQuery();

      if (result.next())
      {
        _forumName = result.getString("forum_name");

        _forumPost = Integer.parseInt(result.getString("forum_post"));
        _forumType = Integer.parseInt(result.getString("forum_type"));
        _forumPerm = Integer.parseInt(result.getString("forum_perm"));
        _ownerID = Integer.parseInt(result.getString("forum_owner_id"));
      }
      result.close();
      statement.close();
    }
    catch (Exception e)
    {
      _log.warning("data error on Forum " + _forumId + " : " + e);
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
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT * FROM topic WHERE topic_forum_id=? ORDER BY topic_id DESC");
      statement.setInt(1, _forumId);
      ResultSet result = statement.executeQuery();

      while (result.next())
      {
        Topic t = new Topic(Topic.ConstructorType.RESTORE, Integer.parseInt(result.getString("topic_id")), Integer.parseInt(result.getString("topic_forum_id")), result.getString("topic_name"), Long.parseLong(result.getString("topic_date")), result.getString("topic_ownername"), Integer.parseInt(result.getString("topic_ownerid")), Integer.parseInt(result.getString("topic_type")), Integer.parseInt(result.getString("topic_reply")));
        _topic.put(Integer.valueOf(t.getID()), t);
        if (t.getID() > TopicBBSManager.getInstance().getMaxID(this))
        {
          TopicBBSManager.getInstance().setMaxID(t.getID(), this);
        }
      }
      result.close();
      statement.close();
    }
    catch (Exception e)
    {
      _log.warning("data error on Forum " + _forumId + " : " + e);
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

  private void getChildren()
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT forum_id FROM forums WHERE forum_parent=?");
      statement.setInt(1, _forumId);
      ResultSet result = statement.executeQuery();

      while (result.next())
      {
        _children.add(new Forum(Integer.parseInt(result.getString("forum_id")), this));
      }
      result.close();
      statement.close();
    }
    catch (Exception e)
    {
      _log.warning("data error on Forum (children): " + e);
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

  public int getTopicSize()
  {
    if (!_loaded)
    {
      load();
      getChildren();
      _loaded = true;
    }
    return _topic.size();
  }

  public Topic gettopic(int j) {
    if (!_loaded)
    {
      load();
      getChildren();
      _loaded = true;
    }
    return (Topic)_topic.get(Integer.valueOf(j));
  }

  public void addtopic(Topic t) {
    if (!_loaded)
    {
      load();
      getChildren();
      _loaded = true;
    }
    _topic.put(Integer.valueOf(t.getID()), t);
  }

  public int getID()
  {
    return _forumId;
  }

  public String getName()
  {
    if (!_loaded)
    {
      load();
      getChildren();
      _loaded = true;
    }
    return _forumName;
  }

  public int getType() {
    if (!_loaded)
    {
      load();
      getChildren();
      _loaded = true;
    }
    return _forumType;
  }

  public Forum getChildByName(String name)
  {
    if (!_loaded)
    {
      load();
      getChildren();
      _loaded = true;
    }
    for (Forum f : _children)
    {
      if (f.getName().equals(name))
      {
        return f;
      }
    }
    return null;
  }

  public void rmTopicByID(int id)
  {
    _topic.remove(Integer.valueOf(id));
  }

  public void insertindb()
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("INSERT INTO forums (forum_id,forum_name,forum_parent,forum_post,forum_type,forum_perm,forum_owner_id) values (?,?,?,?,?,?,?)");
      statement.setInt(1, _forumId);
      statement.setString(2, _forumName);
      statement.setInt(3, _fParent.getID());
      statement.setInt(4, _forumPost);
      statement.setInt(5, _forumType);
      statement.setInt(6, _forumPerm);
      statement.setInt(7, _ownerID);
      statement.execute();
      statement.close();
    }
    catch (Exception e)
    {
      _log.warning("error while saving new Forum to db " + e);
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

  public void vload()
  {
    if (!_loaded)
    {
      load();
      getChildren();
      _loaded = true;
    }
  }
}