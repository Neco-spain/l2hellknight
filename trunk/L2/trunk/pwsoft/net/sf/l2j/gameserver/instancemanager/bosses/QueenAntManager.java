package net.sf.l2j.gameserver.instancemanager.bosses;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.instancemanager.EventManager;
import net.sf.l2j.gameserver.instancemanager.GrandBossManager;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import net.sf.l2j.util.log.AbstractLogger;
import scripts.ai.QueenAnt;

public class QueenAntManager extends GrandBossManager
{
  private static final Logger _log = AbstractLogger.getLogger(QueenAntManager.class.getName());
  private static final int BOSS = 29001;
  private QueenAnt self;
  private boolean _enter;
  private static QueenAntManager _instance;
  private static Status _status;

  public QueenAntManager()
  {
    self = null;
    _enter = false;
  }

  public static final QueenAntManager getInstance()
  {
    return _instance;
  }

  public static void init()
  {
    _instance = new QueenAntManager();
    _instance.load();
  }

  public void load()
  {
    Connect con = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      st = con.prepareStatement("SELECT spawn_date, status FROM grandboss_data WHERE boss_id=?");
      st.setInt(1, 29001);
      rs = st.executeQuery();
      if (rs.next())
      {
        int status = rs.getInt("status");
        long respawn = rs.getLong("spawn_date");

        if (status > 1) {
          status = 1;
        }
        if (respawn > 0L) {
          status = 0;
        }
        _status = new Status(status, respawn);
      }
    }
    catch (SQLException e)
    {
      _log.warning("QueenAntManager, failed to load: " + e);
      e.getMessage();
    }
    finally
    {
      Close.CSR(con, st, rs);
    }

    switch (_status.status)
    {
    case 0:
      String date = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(_status.respawn));
      _log.info("QueenAnt: dead; spawn date: " + date);
      break;
    case 1:
      _log.info("QueenAnt: live; farm delay: " + Config.AQ_RESTART_DELAY / 60000L + "min.");
    }

    ThreadPoolManager.getInstance().scheduleGeneral(new ManageBoss(), Config.AQ_RESTART_DELAY);
  }

  public void spawnBoss()
  {
    setState(1, 0L);
    self = ((QueenAnt)createOnePrivateEx(29001, -21468, 181638, -5720, 10836));
    self.setRunning();
  }

  public void setState(int status, long respawn)
  {
    _status.status = status;
    _status.respawn = respawn;

    Connect con = null;
    PreparedStatement statement = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE `grandboss_data` SET `status`=?, `spawn_date`=? WHERE `boss_id`=?");
      statement.setInt(1, status);
      statement.setLong(2, respawn);
      statement.setInt(3, 29001);
      statement.executeUpdate();
    }
    catch (SQLException e)
    {
      _log.warning("QueenAntManager, could not set QueenAnt status" + e);
      e.getMessage();
    }
    finally
    {
      Close.CS(con, statement);
    }

    switch (status)
    {
    case 0:
      ThreadPoolManager.getInstance().scheduleGeneral(new SpawnBoss(), respawn - System.currentTimeMillis());
      String date = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(respawn));
      _log.info("QueenAntManager, QueenAnt status: 0; killed, respawn date: " + date);
      break;
    case 1:
      if (Config.ANNOUNCE_EPIC_STATES)
        EventManager.getInstance().announce(Static.ANTQUEEN_SPAWNED);
      _log.info("QueenAntManager, QueenAnt status: 1; spawned.");
    }
  }

  public int getStatus()
  {
    if (_status.wait) {
      return 0;
    }
    return _status.status;
  }

  public boolean spawned()
  {
    return _status.spawned;
  }

  public void setSpawned()
  {
    _status.spawned = true;
  }

  public void notifyDie()
  {
    if (self == null) {
      return;
    }
    self = null;
    _status.spawned = false;

    long offset = (Config.AQ_MIN_RESPAWN + Config.AQ_MAX_RESPAWN) / 2L;
    setState(0, System.currentTimeMillis() + offset);

    if (Config.ANNOUNCE_EPIC_STATES)
      EventManager.getInstance().announce(Static.ANTQUEEN_DIED);
  }

  class SpawnBoss
    implements Runnable
  {
    SpawnBoss()
    {
    }

    public void run()
    {
      spawnBoss();
    }
  }

  class ManageBoss
    implements Runnable
  {
    ManageBoss()
    {
    }

    public void run()
    {
      long delay = 0L;
      if (QueenAntManager._status.respawn > 0L) {
        delay = QueenAntManager._status.respawn - System.currentTimeMillis();
      }
      if (delay <= 0L)
        spawnBoss();
      else
        ThreadPoolManager.getInstance().scheduleGeneral(new QueenAntManager.SpawnBoss(QueenAntManager.this), delay);
      QueenAntManager._status.wait = false;
    }
  }

  static class Status
  {
    public int status;
    public long respawn;
    public boolean wait = true;
    public boolean spawned = false;

    public Status(int status, long respawn)
    {
      this.status = status;
      this.respawn = respawn;
    }
  }
}