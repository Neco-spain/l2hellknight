package l2p.gameserver;

import java.io.PrintStream;
import java.util.Timer;
import java.util.TimerTask;
import l2p.commons.net.nio.impl.SelectorThread;
import l2p.commons.time.cron.SchedulingPattern;
import l2p.commons.time.cron.SchedulingPattern.InvalidPatternException;
import l2p.gameserver.database.DatabaseFactory;
import l2p.gameserver.instancemanager.CoupleManager;
import l2p.gameserver.instancemanager.CursedWeaponsManager;
import l2p.gameserver.instancemanager.games.FishingChampionShipManager;
import l2p.gameserver.loginservercon.LoginServerCommunication;
import l2p.gameserver.model.GameObjectsStorage;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.Hero;
import l2p.gameserver.model.entity.SevenSigns;
import l2p.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2p.gameserver.model.entity.olympiad.OlympiadDatabase;
import l2p.gameserver.scripts.Scripts;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Shutdown extends Thread
{
  private static final Logger _log = LoggerFactory.getLogger(Shutdown.class);
  public static final int SHUTDOWN = 0;
  public static final int RESTART = 2;
  public static final int NONE = -1;
  private static final Shutdown _instance = new Shutdown();
  private Timer counter;
  private int shutdownMode;
  private int shutdownCounter;

  public static final Shutdown getInstance()
  {
    return _instance;
  }

  private Shutdown()
  {
    setName(getClass().getSimpleName());
    setDaemon(true);

    shutdownMode = -1;
  }

  public int getSeconds()
  {
    return shutdownMode == -1 ? -1 : shutdownCounter;
  }

  public int getMode()
  {
    return shutdownMode;
  }

  public synchronized void schedule(int seconds, int shutdownMode)
  {
    if (seconds < 0) {
      return;
    }
    if (counter != null) {
      counter.cancel();
    }
    this.shutdownMode = shutdownMode;
    shutdownCounter = seconds;

    _log.info(new StringBuilder().append("Scheduled server ").append(shutdownMode == 0 ? "shutdown" : "restart").append(" in ").append(Util.formatTime(seconds)).append(".").toString());

    counter = new Timer("ShutdownCounter", true);
    counter.scheduleAtFixedRate(new ShutdownCounter(null), 0L, 1000L);
  }

  public void schedule(String time, int shutdownMode)
  {
    SchedulingPattern cronTime;
    try
    {
      cronTime = new SchedulingPattern(time);
    }
    catch (SchedulingPattern.InvalidPatternException e)
    {
      return;
    }

    int seconds = (int)(cronTime.next(System.currentTimeMillis()) / 1000L - System.currentTimeMillis() / 1000L);
    schedule(seconds, shutdownMode);
  }

  public synchronized void cancel()
  {
    shutdownMode = -1;
    if (counter != null)
      counter.cancel();
    counter = null;
  }

  public void run()
  {
    System.out.println("Shutting down LS/GS communication...");
    LoginServerCommunication.getInstance().shutdown();

    System.out.println("Shutting down scripts...");
    Scripts.getInstance().shutdown();

    System.out.println("Disconnecting players...");
    disconnectAllPlayers();

    System.out.println("Saving data...");
    saveData();
    try
    {
      System.out.println("Shutting down thread pool...");
      ThreadPoolManager.getInstance().shutdown();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    System.out.println("Shutting down selector...");
    if (GameServer.getInstance() != null) {
      for (SelectorThread st : GameServer.getInstance().getSelectorThreads())
        try
        {
          st.shutdown();
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
    }
    try
    {
      System.out.println("Shutting down database communication...");
      DatabaseFactory.getInstance().shutdown();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    System.out.println("Shutdown finished.");
  }

  private void saveData()
  {
    try
    {
      if (!SevenSigns.getInstance().isSealValidationPeriod())
      {
        SevenSignsFestival.getInstance().saveFestivalData(false);
        System.out.println("SevenSignsFestival: Data saved.");
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    try
    {
      SevenSigns.getInstance().saveSevenSignsData(0, true);
      System.out.println("SevenSigns: Data saved.");
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    if (Config.ENABLE_OLYMPIAD) {
      try
      {
        OlympiadDatabase.save();
        System.out.println("Olympiad: Data saved.");
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
    if (Config.ALLOW_WEDDING) {
      try
      {
        CoupleManager.getInstance().store();
        System.out.println("CoupleManager: Data saved.");
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
    try
    {
      FishingChampionShipManager.getInstance().shutdown();
      System.out.println("FishingChampionShipManager: Data saved.");
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    try
    {
      Hero.getInstance().shutdown();
      System.out.println("Hero: Data saved.");
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    if (Config.ALLOW_CURSED_WEAPONS)
      try
      {
        CursedWeaponsManager.getInstance().saveData();
        System.out.println("CursedWeaponsManager: Data saved,");
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
  }

  private void disconnectAllPlayers()
  {
    for (Player player : GameObjectsStorage.getAllPlayersForIterate())
      try
      {
        player.logout();
      }
      catch (Exception e)
      {
        System.out.println(new StringBuilder().append("Error while disconnecting: ").append(player).append("!").toString());
        e.printStackTrace();
      }
  }

  private class ShutdownCounter extends TimerTask
  {
    private ShutdownCounter()
    {
    }

    public void run()
    {
      switch (shutdownCounter)
      {
      case 60:
      case 120:
      case 180:
      case 240:
      case 300:
      case 600:
      case 900:
      case 1800:
        Announcements.getInstance().announceByCustomMessage("THE_SERVER_WILL_BE_COMING_DOWN_IN_S1_MINUTES", new String[] { String.valueOf(Shutdown.access$000(Shutdown.this) / 60) });
        break;
      case 5:
      case 10:
      case 20:
      case 30:
        Announcements.getInstance().announceToAll(new SystemMessage(1).addNumber(shutdownCounter));
        break;
      case 0:
        switch (shutdownMode)
        {
        case 0:
          Runtime.getRuntime().exit(0);
          break;
        case 2:
          Runtime.getRuntime().exit(2);
        }

        cancel();
        return;
      }

      Shutdown.access$010(Shutdown.this);
    }
  }
}