package net.sf.l2j.gameserver.util;

import java.io.PrintStream;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.util.TimeLogger;

public class MemoryAgent
{
  private static MemoryAgent _instance = null;

  public static MemoryAgent getInstance() {
    return _instance;
  }

  public static void init()
  {
    _instance = new MemoryAgent();
    load();
  }

  private static void load()
  {
    if (Config.CONSOLE_ADVANCED) {
      return;
    }
    ThreadPoolManager.getInstance().scheduleGeneral(new MemoryPrint(), Config.WEBSTAT_INTERVAL2);
  }

  static class MemoryPrint
    implements Runnable
  {
    public void run()
    {
      try
      {
        Runtime r = Runtime.getRuntime();
        int online = L2World.getInstance().getAllPlayersCount();
        int traders = L2World.getInstance().getAllOfflineCount();
        long memory = (r.totalMemory() - r.freeMemory()) / 1024L / 1024L;
        System.out.println(TimeLogger.getLogTime() + "Online: current online " + online + "; offline traders: " + traders + "; Used memory: " + memory + "MB.");

        ThreadPoolManager.getInstance().scheduleGeneral(new MemoryPrint(), Config.WEBSTAT_INTERVAL2);
      }
      catch (Throwable e)
      {
      }
    }
  }
}