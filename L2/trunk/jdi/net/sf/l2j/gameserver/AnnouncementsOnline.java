package net.sf.l2j.gameserver;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.L2World;

public class AnnouncementsOnline
{
  private static final AnnouncementsOnline _instance = new AnnouncementsOnline();
  private int _maxOnline;
  private String _maxOnlineDate;
  private long _lastUpdate;

  public AnnouncementsOnline()
  {
    _maxOnline = 0;
    _maxOnlineDate = "";

    _lastUpdate = 0L;
  }

  public static AnnouncementsOnline getInstance() {
    return _instance;
  }

  public int getCurrentOnline()
  {
    L2World.getInstance(); return L2World.getAllPlayersCount();
  }

  public int getOfflineTradersOnline()
  {
    return L2World.getInstance().getAllOfflineCount();
  }

  public int getMaxOnline()
  {
    return _maxOnline;
  }

  public String getMaxOnlineDate()
  {
    return _maxOnlineDate;
  }

  public void loadMaxOnline()
  {
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT * FROM `announce_records` LIMIT 1");
      rset = statement.executeQuery();
      if (rset.next())
      {
        _maxOnline = rset.getInt("online");
        _maxOnlineDate = rset.getString("date");
        System.out.println("Online: Max online was " + _maxOnline);
      }
    }
    catch (Exception e)
    {
      System.out.println("Online: can't load maxOnline");
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
      try
      {
        statement.close();
      }
      catch (SQLException e)
      {
        e.printStackTrace();
      }
      try
      {
        rset.close();
      }
      catch (SQLException e)
      {
        e.printStackTrace();
      }
    }

    if (Config.ONLINE_ANNOUNE)
      ThreadPoolManager.getInstance().scheduleGeneral(new announceOnline(), Config.ONLINE_ANNOUNCE_DELAY * 60000);
  }

  public void checkMaxOnline()
  {
    if (getCurrentOnline() > _maxOnline)
      updateMaxOnline();
  }

  private void updateMaxOnline()
  {
    int newInline = getCurrentOnline();
    if (newInline < _maxOnline) {
      return;
    }
    String newDate = getDate();

    if (System.currentTimeMillis() - _lastUpdate < 60000L)
    {
      _maxOnline = newInline;
      _maxOnlineDate = newDate;
      return;
    }

    _lastUpdate = System.currentTimeMillis();

    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE `announce_records` SET `online`=?,`date`=?");
      statement.setInt(1, newInline);
      statement.setString(2, newDate);
      statement.execute();
    }
    catch (Exception e)
    {
      System.out.println("Online: can't set new maxOnline");
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
      try
      {
        statement.close();
      }
      catch (SQLException e)
      {
        e.printStackTrace();
      }
    }
    _maxOnline = newInline;
    _maxOnlineDate = newDate;
  }

  private static String getDate()
  {
    Date date = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy \u043F\u0457\u0405 HH:mm");

    return sdf.format(date);
  }

  class announceOnline
    implements Runnable
  {
    announceOnline()
    {
    }

    public void run()
    {
      try
      {
        Announcements an = Announcements.getInstance();
        int currentOnline = getCurrentOnline();
        int offTraders = getOfflineTradersOnline();
        int maxOnline = getMaxOnline();

        if (Config.ONLINE_SHOW_OFFLINE)
        {
          an.announceToAll("\u0418\u0433\u0440\u043E\u043A\u043E\u0432 \u043E\u043D\u043B\u0430\u0439\u043D: " + currentOnline);
          an.announceToAll("\u041E\u0444\u0444\u043B\u0430\u0439\u043D \u0442\u043E\u0440\u0433\u043E\u0432\u0446\u0435\u0432: " + offTraders);
        }
        else
        {
          an.announceToAll("\u0418\u0433\u0440\u043E\u043A\u043E\u0432 \u043E\u043D\u043B\u0430\u0439\u043D: " + currentOnline);
        }
        if (Config.ONLINE_SHOW_MAXONLINE)
        {
          an.announceToAll("\u041C\u0430\u043A\u0441\u0438\u043C\u0430\u043B\u044C\u043D\u044B\u0439 \u043E\u043D\u043B\u0430\u0439\u043D \u0431\u044B\u043B: " + maxOnline);
          if (Config.ONLINE_SHOW_MAXONLINE_DATE)
            an.announceToAll("\u044D\u0442\u043E \u0431\u044B\u043B\u043E: " + getMaxOnlineDate());
        }
        ThreadPoolManager.getInstance().scheduleGeneral(new announceOnline(AnnouncementsOnline.this), Config.ONLINE_ANNOUNCE_DELAY * 60000);
      }
      catch (Throwable e)
      {
      }
    }
  }
}