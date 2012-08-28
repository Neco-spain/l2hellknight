package net.sf.l2j.gameserver;

import java.io.PrintStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.instancemanager.CastleManorManager;
import net.sf.l2j.gameserver.instancemanager.CursedWeaponsManager;
import net.sf.l2j.gameserver.instancemanager.GrandBossManager;
import net.sf.l2j.gameserver.instancemanager.QuestManager;
import net.sf.l2j.gameserver.instancemanager.RaidBossSpawnManager;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.olympiad.OlympiadDatabase;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.RestartResponse;
import net.sf.l2j.gameserver.network.serverpackets.ServerClose;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.Broadcast;
import net.sf.l2j.util.TimeLogger;
import net.sf.l2j.util.log.AbstractLogger;
import org.mmocore.network.SelectorThread;

public class AutoRestart
{
  private static Logger _log = AbstractLogger.getLogger(AutoRestart.class.getName());
  private static AutoRestart _instance;
  private static long _restartTime = 0L;

  public static AutoRestart getInstance() {
    return _instance;
  }

  public static void init()
  {
    _instance = new AutoRestart();
    _instance.prepare();
  }

  private void prepare() {
    ThreadPoolManager.getInstance().scheduleGeneral(new Timer(56), getNextRestart());
    _log.info(TimeLogger.getLogTime() + "Auto Restart: scheduled at " + Config.RESTART_HOUR + " hour. (" + getNextRestart() / 60000L + " minutes remaining.)");
  }

  private static long getNextRestart() {
    Calendar tomorrow = new GregorianCalendar();
    tomorrow.add(5, 1);
    Calendar result = new GregorianCalendar(tomorrow.get(1), tomorrow.get(2), tomorrow.get(5), Config.RESTART_HOUR, 0);

    _restartTime = result.getTimeInMillis();
    return _restartTime - System.currentTimeMillis();
  }

  public long remain() {
    return _restartTime - System.currentTimeMillis();
  }

  protected static void startRestart()
  {
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
    Runtime.getRuntime().halt(2);
  }

  private static void saveData()
  {
    if (!SevenSigns.getInstance().isSealValidationPeriod()) {
      SevenSignsFestival.getInstance().saveFestivalData(false);
      System.err.println("#Auto Restart: Seven Signs Festival, saved.");
    }

    SevenSigns.getInstance().saveSevenSignsData(null, true);
    System.err.println("#Auto Restart: Seven Sings, saved.");

    RaidBossSpawnManager.getInstance().cleanUp();
    System.err.println("#Auto Restart: Raid Boss Spawn, saved.");
    GrandBossManager.getInstance().cleanUp();
    System.err.println("#Auto Restart: Grand Boss Spawn, saved.");
    TradeController.getInstance().dataCountStore();
    System.err.println("#Auto Restart: Trade Controller, saved.");
    try
    {
      OlympiadDatabase.save();
      System.err.println("#Auto Restart: Olympiad, saved.");
    } catch (Exception e) {
      e.printStackTrace();
    }

    CursedWeaponsManager.getInstance().saveData();
    System.err.println("#Auto Restart: Cursed Weapons, saved.");

    CastleManorManager.getInstance().save();
    System.err.println("#Auto Restart: Castle Manor Manager, saved.");

    QuestManager.getInstance().save();

    System.err.println("#Auto Restart: Data saved. Starting Up Server...");
    System.err.println("#########################################################################");
    System.err.println(" ");
    System.err.println(" ");
    try {
      Thread.sleep(5000L);
    }
    catch (InterruptedException e)
    {
    }
  }

  private static void disconnectAllCharacters()
  {
    for (L2PcInstance player : L2World.getInstance().getAllPlayers())
      try
      {
        L2GameClient.saveCharToDisk(player);
        if (player.isInOfflineMode()) {
          player.logout();
        } else {
          player.sendPacket(new RestartResponse());
          player.sendPacket(new ServerClose());
        }
      }
      catch (Throwable t)
      {
      }
    try {
      Thread.sleep(1000L);
    } catch (Throwable t) {
      _log.log(Level.INFO, "", t);
    }
  }

  static class Timer
    implements Runnable
  {
    private int step;

    Timer(int step)
    {
      this.step = step;
    }

    public void run() {
      Announcements _an = Announcements.getInstance();
      switch (step) {
      case 26:
      case 36:
      case 46:
      case 56:
        String count = String.valueOf(step).substring(0, 1).trim();
        _an.announceToAll("\u0423\u0432\u0430\u0436\u0430\u0435\u043C\u044B\u0439 \u0438\u0433\u0440\u043E\u043A!");
        _an.announceToAll("\u0410\u0432\u0442\u043E\u043C\u0430\u0442\u0438\u0447\u0435\u0441\u043A\u0438\u0439 \u0440\u0435\u0441\u0442\u0430\u0440\u0442 \u0441\u0435\u0440\u0432\u0435\u0440\u0430 \u0447\u0435\u0440\u0435\u0437 " + count + " \u043C\u0438\u043D\u0443\u0442!");
        ThreadPoolManager.getInstance().scheduleGeneral(new Timer(step - 10), 60000L);
        AutoRestart._log.info(TimeLogger.getLogTime() + "Auto Restart: " + count + " minutes remaining.");
        break;
      case 16:
        _an.announceToAll("\u0423\u0432\u0430\u0436\u0430\u0435\u043C\u044B\u0439 \u0438\u0433\u0440\u043E\u043A!");
        _an.announceToAll("\u0410\u0432\u0442\u043E\u043C\u0430\u0442\u0438\u0447\u0435\u0441\u043A\u0438\u0439 \u0440\u0435\u0441\u0442\u0430\u0440\u0442 \u0441\u0435\u0440\u0432\u0435\u0440\u0430 \u0447\u0435\u0440\u0435\u0437 1 \u043C\u0438\u043D\u0443\u0442\u0443!");
        AutoRestart._log.info(TimeLogger.getLogTime() + "Auto Restart: 1 minute remaining.");
        ThreadPoolManager.getInstance().scheduleGeneral(new Timer(3), 30000L);
        break;
      case 1:
      case 2:
      case 3:
        _an.announceToAll("\u0423\u0432\u0430\u0436\u0430\u0435\u043C\u044B\u0439 \u0438\u0433\u0440\u043E\u043A!");
        _an.announceToAll("\u0410\u0432\u0442\u043E\u043C\u0430\u0442\u0438\u0447\u0435\u0441\u043A\u0438\u0439 \u0440\u0435\u0441\u0442\u0430\u0440\u0442 \u0441\u0435\u0440\u0432\u0435\u0440\u0430 \u0447\u0435\u0440\u0435\u0437 " + step * 10 + " \u0441\u0435\u043A\u0443\u043D\u0434!");

        AutoRestart._log.info(TimeLogger.getLogTime() + "Auto Restart: " + step * 10 + " seconds remaining.");
        Broadcast.toAllOnlinePlayers(SystemMessage.id(SystemMessageId.THE_SERVER_WILL_BE_COMING_DOWN_IN_S1_SECONDS).addNumber(step * 10));
        ThreadPoolManager.getInstance().scheduleGeneral(new Timer(step - 1), 10000L);
        break;
      case 0:
        System.err.println(" ");
        System.err.println(" ");
        System.err.println("#########################################################################");
        try {
          Thread.sleep(5000L);
        } catch (InterruptedException e) {
        }
        System.err.println("#Auto Restart: All players disconnected.");

        Shutdown.getInstance().startRestart();
      }
    }
  }
}