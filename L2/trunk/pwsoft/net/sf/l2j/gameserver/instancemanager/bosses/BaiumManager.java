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
import scripts.ai.Baium;
import scripts.zone.type.L2BossZone;

public class BaiumManager extends GrandBossManager
{
  private static final Logger _log = AbstractLogger.getLogger(BaiumManager.class.getName());
  private static final int BOSS = 29020;
  private Baium self;
  private boolean _enter;
  private static BaiumManager _instance;
  private static Status _status;

  public BaiumManager()
  {
    self = null;
    _enter = false;
  }

  public static final BaiumManager getInstance()
  {
    return _instance;
  }

  public static void init()
  {
    _instance = new BaiumManager();
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
      st.setInt(1, 29020);
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
      _log.warning("BaiumManager, failed to load: " + e);
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
      _log.info("Baium: dead; spawn date: " + date);
      break;
    case 1:
      _log.info("Baium: live; farm delay: " + Config.BAIUM_RESTART_DELAY / 60000L + "min.");
    }

    ThreadPoolManager.getInstance().scheduleGeneral(new ManageBoss(), Config.BAIUM_RESTART_DELAY);
  }

  public void prepareBoss()
  {
    setState(1, 0L);
    GrandBossManager.getInstance().createOnePrivateEx(29025, 116026, 17426, 10106, 37604, 0);
  }

  public void setBaium(Baium baium)
  {
    if (baium == null) {
      return;
    }
    self = baium;
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
      statement.setInt(3, 29020);
      statement.executeUpdate();
    }
    catch (SQLException e)
    {
      _log.warning("BaiumManager, could not set Baium status" + e);
      e.getMessage();
    }
    finally
    {
      Close.CS(con, statement);
    }

    switch (status)
    {
    case 0:
      ThreadPoolManager.getInstance().scheduleGeneral(new PrepareBoss(), respawn - System.currentTimeMillis());
      String date = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(respawn));
      _log.info("BaiumManager, Baium status: 0; killed, respawn date: " + date);
      break;
    case 1:
      _log.info("BaiumManager, Baium status: 1; ready for farm.");
      if (!Config.ANNOUNCE_EPIC_STATES) break;
      EventManager.getInstance().announce(Static.BAIUM_SPAWNED); break;
    case 2:
      _log.info("BaiumManager, Baium status: 2; under attack.");
    }
  }

  public int getStatus()
  {
    if (_status.wait) {
      return 0;
    }
    return _status.status;
  }

  public void notifyEnter()
  {
    if (_enter) {
      return;
    }
    _enter = true;
    ThreadPoolManager.getInstance().scheduleGeneral(new CloseGate(), Config.BAIUM_CLOSE_PORT);
    ThreadPoolManager.getInstance().scheduleGeneral(new Sleep(), Config.BAIUM_UPDATE_LAIR);
  }

  public void notifyDie()
  {
    if (self == null) {
      return;
    }
    self = null;
    GrandBossManager.getInstance().createOnePrivateEx(29055, 115203, 16620, 10078, 0);

    long offset = (Config.BAIUM_MIN_RESPAWN + Config.BAIUM_MAX_RESPAWN) / 2L;
    setState(0, System.currentTimeMillis() + offset);

    if (Config.ANNOUNCE_EPIC_STATES)
      EventManager.getInstance().announce(Static.BAIUM_DIED);
  }

  class Sleep
    implements Runnable
  {
    Sleep()
    {
    }

    public void run()
    {
      if ((self == null) || (self.isDead())) {
        return;
      }
      long lastHit = System.currentTimeMillis() - self.getLastHit();
      if (lastHit > Config.BAIUM_UPDATE_LAIR)
      {
        if (self != null)
        {
          self.unSpawnAngels(true);
          self.deleteMe();
          BaiumManager.access$202(BaiumManager.this, null);
          GrandBossManager.getInstance().createOnePrivateEx(29025, 116026, 17426, 10106, 37604, 0);
        }
        BaiumManager.access$102(BaiumManager.this, false);
        setState(1, 0L);
        GrandBossManager.getInstance().getZone(116040, 17455, 10078).oustAllPlayers();
        return;
      }

      if ((lastHit > 30000L) && (self.getCurrentHp() > self.getMaxHp() * 3.0D / 4.0D)) {
        self.addUseSkillDesire(4136, 1);
      }
      else if (self.getCurrentHp() > self.getMaxHp() * 1.0D / 4.0D) {
        if (self.getFirstEffect(4241) == null)
          self.addUseSkillDesire(4241, 1);
        else if (self.getCurrentHp() > self.getMaxHp() * 2.0D / 4.0D) {
          if (self.getFirstEffect(4240) == null)
            self.addUseSkillDesire(4240, 1);
          else if (self.getCurrentHp() > self.getMaxHp() * 3.0D / 4.0D)
            if (self.getFirstEffect(4239) == null)
              self.addUseSkillDesire(4239, 1);
            else if (self.getFirstEffect(4125) == null)
              self.addUseSkillDesire(4125, 1);
        }
      }
      ThreadPoolManager.getInstance().scheduleGeneral(new Sleep(BaiumManager.this), Config.BAIUM_UPDATE_LAIR);
    }
  }

  class CloseGate
    implements Runnable
  {
    CloseGate()
    {
    }

    public void run()
    {
      BaiumManager.access$102(BaiumManager.this, false);
      setState(2, 0L);
    }
  }

  class PrepareBoss
    implements Runnable
  {
    PrepareBoss()
    {
    }

    public void run()
    {
      prepareBoss();
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
      if (BaiumManager._status.respawn > 0L) {
        delay = BaiumManager._status.respawn - System.currentTimeMillis();
      }
      if (delay <= 0L)
        prepareBoss();
      else
        ThreadPoolManager.getInstance().scheduleGeneral(new BaiumManager.PrepareBoss(BaiumManager.this), delay);
      BaiumManager._status.wait = false;
    }
  }

  static class Status
  {
    public int status;
    public long respawn;
    public boolean wait = true;

    public Status(int status, long respawn)
    {
      this.status = status;
      this.respawn = respawn;
    }
  }
}