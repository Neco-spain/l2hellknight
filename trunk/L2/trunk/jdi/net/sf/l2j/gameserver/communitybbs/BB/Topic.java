package net.sf.l2j.gameserver.communitybbs.BB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Logger;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.communitybbs.Manager.TopicBBSManager;

public class Topic
{
  private static Logger _log = Logger.getLogger(Topic.class.getName());
  public static final int MORMAL = 0;
  public static final int MEMO = 1;
  private int _id;
  private int _forumId;
  private String _topicName;
  private long _date;
  private String _ownerName;
  private int _ownerId;
  private int _type;
  private int _cReply;

  public Topic(ConstructorType ct, int id, int fid, String name, long date, String oname, int oid, int type, int Creply)
  {
    _id = id;
    _forumId = fid;
    _topicName = name;
    _date = date;
    _ownerName = oname;
    _ownerId = oid;
    _type = type;
    _cReply = Creply;
    TopicBBSManager.getInstance().addTopic(this);

    if (ct == ConstructorType.CREATE)
    {
      insertindb();
    }
  }

  public void insertindb()
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("INSERT INTO topic (topic_id,topic_forum_id,topic_name,topic_date,topic_ownername,topic_ownerid,topic_type,topic_reply) values (?,?,?,?,?,?,?,?)");
      statement.setInt(1, _id);
      statement.setInt(2, _forumId);
      statement.setString(3, _topicName);
      statement.setLong(4, _date);
      statement.setString(5, _ownerName);
      statement.setInt(6, _ownerId);
      statement.setInt(7, _type);
      statement.setInt(8, _cReply);
      statement.execute();
      statement.close();
    }
    catch (Exception e)
    {
      _log.warning("error while saving new Topic to db " + e);
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

  public int getID()
  {
    return _id;
  }

  public int getForumID() {
    return _forumId;
  }

  public String getName()
  {
    return _topicName;
  }

  public String getOwnerName()
  {
    return _ownerName;
  }

  public void deleteme(Forum f)
  {
    TopicBBSManager.getInstance().delTopic(this);
    f.rmTopicByID(getID());
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("DELETE FROM topic WHERE topic_id=? AND topic_forum_id=?");
      statement.setInt(1, getID());
      statement.setInt(2, f.getID());
      statement.execute();
      statement.close();
    }
    catch (Exception e)
    {
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

  public long getDate()
  {
    return _date;
  }

  public static enum ConstructorType
  {
    RESTORE, CREATE;
  }
}