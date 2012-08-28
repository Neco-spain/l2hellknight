package net.sf.l2j.gameserver.model.entity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.util.sql.SQLQuery;
import net.sf.l2j.gameserver.util.sql.SQLQueue;
import net.sf.l2j.util.CloseUtil;
import net.sf.l2j.util.Rnd;

public class L2Top
  implements Runnable
{
  private static L2Top _instance = null;

  private static final Logger _log = Logger.getLogger(L2Top.class.getName());
  private ScheduledFuture<?> _task;
  private Timestamp _lastVote;
  private boolean _firstRun = false;

  public static L2Top getInstance()
  {
    if (_instance == null)
      _instance = new L2Top();
    return _instance;
  }

  private L2Top()
  {
    _lastVote = null;
    if (Config.L2TOP_ENABLED)
    {
      Connection con = null;
      try
      {
        con = L2DatabaseFactory.getInstance().getConnection();
        PreparedStatement stm = con.prepareStatement("select max(votedate) from l2votes");
        ResultSet r = stm.executeQuery();
        if (r.next())
          _lastVote = r.getTimestamp(1);
        if (_lastVote == null)
        {
          _firstRun = true;
          _lastVote = new Timestamp(0L);
        }
        r.close();
        stm.close();

        _task = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(this, 60000L, Config.L2TOP_POLLINTERVAL * 60000);
        Runtime.getRuntime().addShutdownHook(new Terminator(null));
        _log.info("L2Top: Started with poll interval " + Config.L2TOP_POLLINTERVAL + " minute(s)");
      }
      catch (SQLException e)
      {
        _log.info("L2Top: Error connection to database: " + e.getMessage());
      }
      finally
      {
        CloseUtil.close(con);
        con = null;
      }
    }
  }

  private boolean checkVotes()
  {
    try
    {
      _log.info("L2Top: Checking l2top.ru....");
      int nVotes = 0;
      URL url = new URL(Config.L2TOP_URL);
      BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
      if (reader != null)
      {
        Timestamp last = _lastVote;
        String line;
        while ((line = reader.readLine()) != null)
        {
          if (line.indexOf("\t") == -1)
            continue;
          Timestamp voteDate = Timestamp.valueOf(line.substring(0, line.indexOf("\t")).trim());
          if (voteDate.after(_lastVote))
          {
            if (voteDate.after(last))
              last = voteDate;
            String charName = line.substring(line.indexOf("\t") + 1).toLowerCase();
            if ((Config.L2TOP_PREFIX != null) && (Config.L2TOP_PREFIX.length() > 0)) {
              if (charName.startsWith(Config.L2TOP_PREFIX)) {
                charName = charName.substring(Config.L2TOP_PREFIX.length());
              }
            }

            SQLQueue.getInstance().add(new VotesUpdate(voteDate, charName, _firstRun));
            nVotes++;
          }
        }

        _lastVote = last;
        _log.info("L2Top: " + nVotes + " vote(s) parsed");
        reader.close();
        return true;
      }

    }
    catch (Exception e)
    {
      _log.info("L2Top: Error while reading data" + e);
    }
    return false;
  }

  public void run()
  {
    checkVotes();
    _firstRun = false;
  }

  private class VotesUpdate
    implements SQLQuery
  {
    private Timestamp _votedate;
    private String _charName;
    private boolean _fr;

    public VotesUpdate(Timestamp votedate, String charName, boolean fr)
    {
      _votedate = votedate;
      _charName = charName;
      _fr = fr;
    }

    public void execute(Connection con)
    {
      try
      {
        PreparedStatement stm = con.prepareStatement("insert into l2votes select ?,? from characters where not exists(select * from l2votes where votedate=? and charName =?) limit 1");
        stm.setTimestamp(1, _votedate);
        stm.setTimestamp(3, _votedate);
        stm.setString(2, _charName);
        stm.setString(4, _charName);
        boolean sendPrize = stm.executeUpdate() > 0;
        stm.close();
        if ((_fr) && (Config.L2TOP_IGNOREFIRST))
          sendPrize = false;
        if (sendPrize)
        {
          L2PcInstance player = L2Utils.loadPlayer(_charName);
          if (player != null)
          {
            int numItems = Config.L2TOP_MIN;
            if (Config.L2TOP_MAX > Config.L2TOP_MIN)
              numItems += Rnd.get(Config.L2TOP_MAX - Config.L2TOP_MIN);
            player.addItem("l2top", Config.L2TOP_ITEM, numItems, null, true);
            if ((player.isOnline() != 0) && (Config.L2TOP_MESSAGE != null) && (Config.L2TOP_MESSAGE.length() > 0))
              player.sendMessage(Config.L2TOP_MESSAGE);
            player.store();
          }
        }
      }
      catch (SQLException e)
      {
        e.printStackTrace();
      }
    }
  }

  private class Terminator extends Thread
  {
    private Terminator()
    {
    }

    public void run()
    {
      System.out.println("L2Top: stopped");
      try
      {
        if (L2Top.getInstance()._task != null)
        {
          L2Top.getInstance()._task.cancel(true);
        }
      }
      catch (Exception e)
      {
      }
    }
  }
}