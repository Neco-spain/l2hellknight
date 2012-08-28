package net.sf.l2j.gameserver;

import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.instancemanager.CastleManorManager;
import net.sf.l2j.gameserver.instancemanager.CursedWeaponsManager;
import net.sf.l2j.gameserver.instancemanager.GrandBossManager;
import net.sf.l2j.gameserver.instancemanager.QuestManager;
import net.sf.l2j.gameserver.instancemanager.RaidBossSpawnManager;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.olympiad.OlympiadDatabase;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.Broadcast;
import net.sf.l2j.util.log.AbstractLogger;
import org.mmocore.network.SelectorThread;

public class Shutdown extends Thread
{
  private static Logger _log = AbstractLogger.getLogger(Shutdown.class.getName());
  private static Shutdown _instance;
  private static Shutdown _counterInstance = null;
  private int _secondsShut;
  private int _shutdownMode;
  public static final int SIGTERM = 0;
  public static final int GM_SHUTDOWN = 1;
  public static final int GM_RESTART = 2;
  public static final int ABORT = 3;
  public static final int AUTO_RESTART = 4;
  private static final String[] MODE_TEXT = { "\u041F\u0440\u043E\u0444\u0438\u043B\u0430\u043A\u0442\u0438\u0447\u0435\u0441\u043A\u043E\u0435 \u0432\u044B\u043A\u043B\u044E\u0447\u0435\u043D\u0438\u0435 \u0441\u0435\u0440\u0432\u0435\u0440\u0430!", "\u0412\u044B\u043A\u043B\u044E\u0447\u0435\u043D\u0438\u0435", "\u0420\u0435\u0441\u0442\u0430\u0440\u0442", "\u041E\u0442\u043C\u0435\u043D\u0430", "\u0410\u0432\u0442\u043E\u043C\u0430\u0442\u0438\u0447\u0435\u0441\u043A\u0438\u0439 \u0440\u0435\u0441\u0442\u0430\u0440\u0442!" };
  private boolean _AbortShutdown = false;

  public void startTelnetShutdown(String IP, int seconds, boolean restart)
  {
    Announcements _an = Announcements.getInstance();
    _log.warning("IP: " + IP + " issued shutdown command. " + MODE_TEXT[_shutdownMode] + " in " + seconds + " seconds!");

    if (restart)
      _shutdownMode = 2;
    else {
      _shutdownMode = 1;
    }

    if (_shutdownMode > 0) {
      _an.announceToAll("\u0412\u043D\u0438\u043C\u0430\u043D\u0438\u0435 \u0438\u0433\u0440\u043E\u043A\u0438!");
      _an.announceToAll(MODE_TEXT[_shutdownMode] + " \u0441\u0435\u0440\u0432\u0435\u0440\u0430 \u0447\u0435\u0440\u0435\u0437 " + seconds + " \u0441\u0435\u043A\u0443\u043D\u0434!");
      if ((_shutdownMode == 1) || (_shutdownMode == 2)) {
        _an.announceToAll("\u041F\u043E\u0436\u0430\u043B\u0443\u0439\u0441\u0442\u0430, \u0432\u044B\u0439\u0434\u0438\u0442\u0435 \u0438\u0437 \u0438\u0433\u0440\u044B.");
      }
    }

    if (_counterInstance != null) {
      _counterInstance._abort();
    }
    _counterInstance = new Shutdown(seconds, restart);
    _counterInstance.start();
  }

  public void telnetAbort(String IP)
  {
    Announcements _an = Announcements.getInstance();
    _log.warning("IP: " + IP + " issued shutdown ABORT. " + MODE_TEXT[_shutdownMode] + " has been stopped!");
    _an.announceToAll(MODE_TEXT[_shutdownMode] + ", \u0441\u0435\u0440\u0432\u0435\u0440 \u043F\u0440\u043E\u0434\u043E\u043B\u0436\u0430\u0435\u0442 \u0441\u0432\u043E\u044E \u0440\u0430\u0431\u043E\u0442\u0443!");

    if (_counterInstance != null)
      _counterInstance._abort();
  }

  public void startRestart()
  {
    _shutdownMode = 2;

    if (_counterInstance != null) {
      _counterInstance._abort();
    }

    _counterInstance = new Shutdown(45, true);
    _counterInstance.start();
  }

  public Shutdown()
  {
    _secondsShut = -1;
    _shutdownMode = 0;
  }

  public Shutdown(int seconds, boolean restart)
  {
    if (seconds < 0) {
      seconds = 0;
    }

    _secondsShut = seconds;

    if (restart)
      _shutdownMode = 2;
    else
      _shutdownMode = 1;
  }

  public static Shutdown getInstance()
  {
    if (_instance == null) {
      _instance = new Shutdown();
    }
    return _instance;
  }

  public void run()
  {
    if (this == _instance) {
      Announcements _an = Announcements.getInstance();
      switch (_shutdownMode) {
      case 0:
        System.err.println("SIGTERM received. Shutting down after 60 sec!");
        try
        {
          L2World.getInstance().deleteVisibleNpcSpawns();
          _an.announceToAll("\u0412\u044B\u043A\u043B\u044E\u0447\u0435\u043D\u0438\u0435 \u0441\u0435\u0440\u0432\u0435\u0440\u0430 \u043D\u0430 \u043F\u0440\u043E\u0444\u0438\u043B\u0430\u043A\u0442\u0438\u043A\u0443 \u0447\u0435\u0440\u0435\u0437 \u043C\u0438\u043D\u0443\u0442\u0443!");
          _an.announceToAll("\u0412\u044B\u0439\u0434\u0438\u0442\u0435 \u0438\u0437 \u0438\u0433\u0440\u044B, \u0447\u0442\u043E-\u0431\u044B \u043D\u0435 \u043F\u043E\u0442\u0435\u0440\u044F\u0442\u044C \u0434\u043E\u0441\u0442\u0438\u0433\u043D\u0443\u0442\u044B\u0445 \u0440\u0435\u0437\u0443\u043B\u044C\u0442\u0430\u0442\u043E\u0432.");
          _an.announceToAll("\u041F\u0440\u0438\u043D\u043E\u0441\u0438\u043C \u0438\u0437\u0432\u0438\u043D\u0435\u043D\u0438\u044F \u0437\u0430 \u0432\u043E\u0437\u043C\u043E\u0436\u043D\u044B\u0435 \u043D\u0435\u0443\u0434\u043E\u0431\u0441\u0442\u0432\u0430.");
          Broadcast.toAllOnlinePlayers(SystemMessage.id(SystemMessageId.THE_SERVER_WILL_BE_COMING_DOWN_IN_S1_SECONDS).addNumber(60));
          Thread.sleep(30000L);
        }
        catch (InterruptedException e) {
        }
        try {
          System.err.println("SIGTERM received. Shutting down after 30 sec!");
          _an.announceToAll("\u0412\u044B\u043A\u043B\u044E\u0447\u0435\u043D\u0438\u0435 \u0441\u0435\u0440\u0432\u0435\u0440\u0430 \u043D\u0430 \u043F\u0440\u043E\u0444\u0438\u043B\u0430\u043A\u0442\u0438\u043A\u0443 \u0447\u0435\u0440\u0435\u0437 30 \u0441\u0435\u043A\u0443\u043D\u0434!");
          _an.announceToAll("\u0412\u044B\u0439\u0434\u0438\u0442\u0435 \u0438\u0437 \u0438\u0433\u0440\u044B, \u0447\u0442\u043E-\u0431\u044B \u043D\u0435 \u043F\u043E\u0442\u0435\u0440\u044F\u0442\u044C \u0434\u043E\u0441\u0442\u0438\u0433\u043D\u0443\u0442\u044B\u0445 \u0440\u0435\u0437\u0443\u043B\u044C\u0442\u0430\u0442\u043E\u0432.");
          _an.announceToAll("\u041F\u0440\u0438\u043D\u043E\u0441\u0438\u043C \u0438\u0437\u0432\u0438\u043D\u0435\u043D\u0438\u044F \u0437\u0430 \u0432\u043E\u0437\u043C\u043E\u0436\u043D\u044B\u0435 \u043D\u0435\u0443\u0434\u043E\u0431\u0441\u0442\u0432\u0430.");
          Broadcast.toAllOnlinePlayers(SystemMessage.id(SystemMessageId.THE_SERVER_WILL_BE_COMING_DOWN_IN_S1_SECONDS).addNumber(20));
          Thread.sleep(20000L);
        }
        catch (InterruptedException e) {
        }
        for (int i = 10; i > 0; i--)
          try {
            System.err.println("SIGTERM received. Shutting down after " + i + " sec!");
            _an.announceToAll("\u0412\u044B\u043A\u043B\u044E\u0447\u0435\u043D\u0438\u0435 \u0441\u0435\u0440\u0432\u0435\u0440\u0430 \u043D\u0430 \u043F\u0440\u043E\u0444\u0438\u043B\u0430\u043A\u0442\u0438\u043A\u0443 \u0447\u0435\u0440\u0435\u0437 " + i + " \u0441\u0435\u043A\u0443\u043D\u0434.");
            _an.announceToAll("\u041F\u043E\u0436\u0430\u043B\u0443\u0439\u0441\u0442\u0430, \u0432\u044B\u0439\u0434\u0438\u0442\u0435 \u0438\u0437 \u0438\u0433\u0440\u044B.");
            if (i == 3) {
              disconnectAllCharacters();
            }
            Broadcast.toAllOnlinePlayers(SystemMessage.id(SystemMessageId.THE_SERVER_WILL_BE_COMING_DOWN_IN_S1_SECONDS).addNumber(i));
            Thread.sleep(1000L);
          }
          catch (InterruptedException e) {
          }
        System.err.println("SIGTERM received. Shutting down now...");
      }

      try
      {
        GameTimeController.getInstance().stopTimer();
      }
      catch (Throwable t)
      {
      }
      try
      {
        ThreadPoolManager.getInstance().shutdown();
      }
      catch (Throwable t)
      {
      }

      saveData();
      try
      {
        LoginServerThread.getInstance().interrupt();
      }
      catch (Throwable t)
      {
      }
      try
      {
        GameServer.gameServer.getSelectorThread().shutdown();
        GameServer.gameServer.getSelectorThread().setDaemon(true);
      }
      catch (Throwable t)
      {
      }
      try
      {
        L2DatabaseFactory.getInstance().shutdown();
      }
      catch (Throwable t)
      {
      }
      if ((_instance._shutdownMode == 2) || (_instance._shutdownMode == 4))
        Runtime.getRuntime().halt(2);
      else
        Runtime.getRuntime().halt(0);
    }
    else
    {
      countdown();

      _log.warning("GM shutdown countdown is over. " + MODE_TEXT[_shutdownMode] + " NOW!");
      switch (_shutdownMode) {
      case 1:
        _instance.setMode(1);
        System.exit(0);
        break;
      case 2:
        _instance.setMode(2);
        System.exit(2);
      case 4:
        _instance.setMode(4);
        System.exit(2);
      case 3:
      }
    }
  }

  public void startShutdown(L2PcInstance activeChar, int seconds, boolean restart)
  {
    Announcements _an = Announcements.getInstance();
    _log.warning("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") issued shutdown command. " + MODE_TEXT[_shutdownMode] + " in " + seconds + " seconds!");

    if (restart)
      _shutdownMode = 2;
    else {
      _shutdownMode = 1;
    }

    if (_counterInstance != null) {
      _counterInstance._abort();
    }

    _counterInstance = new Shutdown(seconds, restart);
    _counterInstance.start();
  }

  public void abort(L2PcInstance activeChar)
  {
    Announcements _an = Announcements.getInstance();
    _log.warning("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") issued shutdown ABORT. " + MODE_TEXT[_shutdownMode] + " has been stopped!");
    _an.announceToAll(MODE_TEXT[_shutdownMode] + ", \u0441\u0435\u0440\u0432\u0435\u0440 \u043F\u0440\u043E\u0434\u043E\u043B\u0436\u0430\u0435\u0442 \u0441\u0432\u043E\u044E \u0440\u0430\u0431\u043E\u0442\u0443.");

    _AbortShutdown = true;

    if (_counterInstance != null)
      _counterInstance._abort();
  }

  private void setMode(int mode)
  {
    _shutdownMode = mode;
  }

  private void _abort()
  {
    _shutdownMode = 3;
  }

  private void countdown()
  {
    Announcements _an = Announcements.getInstance();
    try {
      while (_secondsShut > 0)
      {
        switch (_secondsShut) {
        case 60:
        case 120:
        case 180:
        case 240:
        case 300:
        case 360:
        case 420:
        case 480:
        case 540:
          LoginServerThread.getInstance().setServerStatus(4);
          SendCountdownMMessage(MODE_TEXT[_shutdownMode], _secondsShut);
          L2World.getInstance().deleteVisibleNpcSpawns();
          break;
        case 30:
          for (L2PcInstance player : L2World.getInstance().getAllPlayers()) {
            if (player == null)
              continue;
            try
            {
              player.sendHtmlMessage("\u0412\u044B\u0439\u0434\u0438\u0442\u0435 \u0438\u0437 \u0438\u0433\u0440\u044B!");
              player.setIsParalyzed(true);
            }
            catch (Throwable t) {
            }
          }
          _an.announceToAll(MODE_TEXT[_shutdownMode] + " \u0447\u0435\u0440\u0435\u0437 " + _secondsShut + " \u0441\u0435\u043A\u0443\u043D\u0434!");
          break;
        case 5:
          disconnectAllCharacters();
          _an.announceToAll(MODE_TEXT[_shutdownMode] + " \u0447\u0435\u0440\u0435\u0437 " + _secondsShut + " \u0441\u0435\u043A\u0443\u043D\u0434!");
          _an.announceToAll("\u041F\u043E\u0436\u0430\u043B\u0443\u0439\u0441\u0442\u0430, \u0432\u044B\u0439\u0434\u0438\u0442\u0435 \u0438\u0437 \u0438\u0433\u0440\u044B.");
          Broadcast.toAllOnlinePlayers(SystemMessage.id(SystemMessageId.THE_SERVER_WILL_BE_COMING_DOWN_IN_S1_SECONDS).addNumber(_secondsShut));
        }

        _secondsShut -= 1;

        int delay = 1000;
        Thread.sleep(delay);

        if (_shutdownMode == 3)
          break;
      }
    }
    catch (InterruptedException e)
    {
    }
  }

  private void SendCountdownMMessage(String Mode, int secs) {
    Announcements _an = Announcements.getInstance();
    _an.announceToAll(Mode + " \u0441\u0435\u0440\u0432\u0435\u0440\u0430 \u0447\u0435\u0440\u0435\u0437 " + secs / 60 + " \u043C\u0438\u043D\u0443\u0442.");
    _an.announceToAll("\u041F\u043E\u0436\u0430\u043B\u0443\u0439\u0441\u0442\u0430, \u0432\u044B\u0439\u0434\u0438\u0442\u0435 \u0438\u0437 \u0438\u0433\u0440\u044B.");

    Broadcast.toAllOnlinePlayers(SystemMessage.id(SystemMessageId.THE_SERVER_WILL_BE_COMING_DOWN_IN_S1_SECONDS).addNumber(secs));
  }

  private void saveData()
  {
    Announcements _an = Announcements.getInstance();
    try
    {
      _an.announceToAll(MODE_TEXT[_shutdownMode] + " \u0441\u0435\u0440\u0432\u0435\u0440\u0430!");
    } catch (Throwable t) {
      _log.log(Level.INFO, "", t);
    }

    if (!SevenSigns.getInstance().isSealValidationPeriod()) {
      SevenSignsFestival.getInstance().saveFestivalData(false);
    }
    System.err.println("Seven Signs Festival Data       ... Saved");

    SevenSigns.getInstance().saveSevenSignsData(null, true);
    System.err.println("Seven Sings Data                ... Saved");

    RaidBossSpawnManager.getInstance().cleanUp();
    System.err.println("Raid Boss Spawn Data            ... Saved");
    GrandBossManager.getInstance().cleanUp();
    System.err.println("Grand Boss Spawn Data           ... Saved");
    TradeController.getInstance().dataCountStore();
    System.err.println("Trade Controller Data           ... Saved");
    try
    {
      OlympiadDatabase.save();
      System.err.println("Olympiad Data                   ... Saved");
    } catch (Exception e) {
      e.printStackTrace();
    }

    CursedWeaponsManager.getInstance().saveData();
    System.err.println("Cursed Weapons Data             ... Saved");

    CastleManorManager.getInstance().save();
    System.err.println("Castle Manor Manager Data       ... Saved");

    QuestManager.getInstance().save();

    System.err.println("Data saved. All players disconnected, shutting down. Wait 5 seconds...");
    try
    {
      int delay = 5000;
      Thread.sleep(delay);
    }
    catch (InterruptedException e)
    {
    }
  }

  private void disconnectAllCharacters()
  {
    SystemMessage sysm = Static.YOU_HAVE_BEEN_DISCONNECTED;
    for (L2PcInstance player : L2World.getInstance().getAllPlayers()) {
      if (player == null)
      {
        continue;
      }

      try
      {
        player.kick();
      }
      catch (Throwable t)
      {
      }
    }
    try
    {
      Thread.sleep(1000L);
    } catch (Throwable t) {
      _log.log(Level.INFO, "", t);
    }

    for (L2PcInstance player : L2World.getInstance().getAllPlayers()) {
      if (player == null)
        continue;
      try
      {
        player.closeNetConnection();
      }
      catch (Throwable t)
      {
      }
    }
  }
}