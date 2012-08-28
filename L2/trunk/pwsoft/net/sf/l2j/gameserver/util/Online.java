package net.sf.l2j.gameserver.util;

import java.io.PrintStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import net.sf.l2j.util.TimeLogger;

public class Online
{
  private static final Online _instance = new Online();

  private int _maxOnline = 0;
  private String _maxOnlineDate = "";

  private long _lastUpdate = 0L;

  public static Online getInstance()
  {
    return _instance;
  }

  public int getCurrentOnline()
  {
    return L2World.getInstance().getAllPlayersCount();
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
    Connect con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT * FROM `z_maxonline` LIMIT 1");
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
      Close.CSR(con, statement, rset);
    }

    if (Config.SONLINE_ANNOUNE)
      ThreadPoolManager.getInstance().scheduleGeneral(new announceOnline(), Config.SONLINE_ANNOUNCE_DELAY);
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
    Connect con = null;
    PreparedStatement statement = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE `z_maxonline` SET `online`=?,`date`=?");
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
      Close.CS(con, statement);
    }
    _maxOnline = newInline;
    _maxOnlineDate = newDate;
  }

  private static String getDate()
  {
    Date date = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy \u0432 HH:mm");

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

        if (Config.SONLINE_SHOW_OFFLINE)
          an.announceToAll("\u0418\u0433\u0440\u043E\u043A\u043E\u0432 \u043E\u043D\u043B\u0430\u0439\u043D: " + currentOnline + "; \u041E\u0444\u0444\u043B\u0430\u0439\u043D \u0442\u043E\u0440\u0433\u043E\u0432\u0446\u0435\u0432: " + offTraders);
        else {
          an.announceToAll("\u0418\u0433\u0440\u043E\u043A\u043E\u0432 \u043E\u043D\u043B\u0430\u0439\u043D: " + currentOnline);
        }
        if (Config.SONLINE_SHOW_MAXONLINE)
        {
          an.announceToAll("\u041C\u0430\u043A\u0441\u0438\u043C\u0430\u043B\u044C\u043D\u044B\u0439 \u043E\u043D\u043B\u0430\u0439\u043D \u0431\u044B\u043B: " + maxOnline);
          if (Config.SONLINE_SHOW_MAXONLINE_DATE)
            an.announceToAll("\u044D\u0442\u043E \u0431\u044B\u043B\u043E: " + getMaxOnlineDate());
        }
        Runtime r = Runtime.getRuntime();
        System.out.println(TimeLogger.getLogTime() + "Online: current online " + currentOnline + "; offline traders: " + offTraders + "; Used memory: " + (r.totalMemory() - r.freeMemory()) / 1024L / 1024L + "MB.");

        ThreadPoolManager.getInstance().scheduleGeneral(new announceOnline(Online.this), Config.SONLINE_ANNOUNCE_DELAY);
      }
      catch (Throwable e)
      {
      }
    }
  }
}