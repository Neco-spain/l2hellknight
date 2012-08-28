package l2p.gameserver.network.telnet.commands;

import com.sun.management.HotSpotDiagnosticMXBean;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.management.MBeanServer;
import l2p.commons.dao.JdbcEntityStats;
import l2p.commons.lang.StatsUtils;
import l2p.commons.net.nio.impl.SelectorThread;
import l2p.commons.threading.RunnableStatsManager;
import l2p.gameserver.Config;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.dao.ItemsDAO;
import l2p.gameserver.dao.MailDAO;
import l2p.gameserver.database.DatabaseFactory;
import l2p.gameserver.geodata.PathFindBuffers;
import l2p.gameserver.network.telnet.TelnetCommand;
import l2p.gameserver.network.telnet.TelnetCommandHolder;
import l2p.gameserver.taskmanager.AiTaskManager;
import l2p.gameserver.taskmanager.EffectTaskManager;
import l2p.gameserver.utils.GameStats;
import net.sf.ehcache.Cache;
import net.sf.ehcache.statistics.LiveCacheStatistics;
import org.apache.commons.io.FileUtils;

public class TelnetPerfomance
  implements TelnetCommandHolder
{
  private Set<TelnetCommand> _commands = new LinkedHashSet();

  public TelnetPerfomance()
  {
    _commands.add(new TelnetCommand("pool", new String[] { "p" })
    {
      public String getUsage()
      {
        return "pool [dump]";
      }

      public String handle(String[] args)
      {
        StringBuilder sb = new StringBuilder();

        if ((args.length == 0) || (args[0].isEmpty()))
        {
          sb.append(ThreadPoolManager.getInstance().getStats());
        }
        else if ((args[0].equals("dump")) || (args[0].equals("d")))
          try
          {
            new File("stats").mkdir();
            FileUtils.writeStringToFile(new File(new StringBuilder().append("stats/RunnableStats-").append(new SimpleDateFormat("MMddHHmmss").format(Long.valueOf(System.currentTimeMillis()))).append(".txt").toString()), RunnableStatsManager.getInstance().getStats().toString());
            sb.append("Runnable stats saved.\n");
          }
          catch (IOException e)
          {
            sb.append(new StringBuilder().append("Exception: ").append(e.getMessage()).append("!\n").toString());
          }
        else {
          return null;
        }
        return sb.toString();
      }
    });
    _commands.add(new TelnetCommand("mem", new String[] { "m" })
    {
      public String getUsage()
      {
        return "mem";
      }

      public String handle(String[] args)
      {
        StringBuilder sb = new StringBuilder();
        sb.append(StatsUtils.getMemUsage());

        return sb.toString();
      }
    });
    _commands.add(new TelnetCommand("heap")
    {
      public String getUsage()
      {
        return "heap [dump] <live>";
      }

      public String handle(String[] args)
      {
        StringBuilder sb = new StringBuilder();

        if ((args.length == 0) || (args[0].isEmpty()))
          return null;
        if ((args[0].equals("dump")) || (args[0].equals("d")))
          try
          {
            boolean live = (args.length == 2) && (!args[1].isEmpty()) && ((args[1].equals("live")) || (args[1].equals("l")));
            new File("dumps").mkdir();
            String filename = new StringBuilder().append("dumps/HeapDump").append(live ? "Live" : "").append("-").append(new SimpleDateFormat("MMddHHmmss").format(Long.valueOf(System.currentTimeMillis()))).append(".hprof").toString();

            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            HotSpotDiagnosticMXBean bean = (HotSpotDiagnosticMXBean)ManagementFactory.newPlatformMXBeanProxy(server, "com.sun.management:type=HotSpotDiagnostic", HotSpotDiagnosticMXBean.class);
            bean.dumpHeap(filename, live);

            sb.append("Heap dumped.\n");
          }
          catch (IOException e)
          {
            sb.append(new StringBuilder().append("Exception: ").append(e.getMessage()).append("!\n").toString());
          }
        else {
          return null;
        }
        return sb.toString();
      }
    });
    _commands.add(new TelnetCommand("threads", new String[] { "t" })
    {
      public String getUsage()
      {
        return "threads [dump]";
      }

      public String handle(String[] args)
      {
        StringBuilder sb = new StringBuilder();

        if ((args.length == 0) || (args[0].isEmpty()))
        {
          sb.append(StatsUtils.getThreadStats());
        }
        else if ((args[0].equals("dump")) || (args[0].equals("d")))
          try
          {
            new File("stats").mkdir();
            FileUtils.writeStringToFile(new File(new StringBuilder().append("stats/ThreadsDump-").append(new SimpleDateFormat("MMddHHmmss").format(Long.valueOf(System.currentTimeMillis()))).append(".txt").toString()), StatsUtils.getThreadStats(true, true, true).toString());
            sb.append("Threads stats saved.\n");
          }
          catch (IOException e)
          {
            sb.append(new StringBuilder().append("Exception: ").append(e.getMessage()).append("!\n").toString());
          }
        else {
          return null;
        }
        return sb.toString();
      }
    });
    _commands.add(new TelnetCommand("gc")
    {
      public String getUsage()
      {
        return "gc";
      }

      public String handle(String[] args)
      {
        StringBuilder sb = new StringBuilder();
        sb.append(StatsUtils.getGCStats());

        return sb.toString();
      }
    });
    _commands.add(new TelnetCommand("net", new String[] { "ns" })
    {
      public String getUsage()
      {
        return "net";
      }

      public String handle(String[] args)
      {
        StringBuilder sb = new StringBuilder();

        sb.append(SelectorThread.getStats());

        return sb.toString();
      }
    });
    _commands.add(new TelnetCommand("pathfind", new String[] { "pfs" })
    {
      public String getUsage()
      {
        return "pathfind";
      }

      public String handle(String[] args)
      {
        StringBuilder sb = new StringBuilder();

        sb.append(PathFindBuffers.getStats());

        return sb.toString();
      }
    });
    _commands.add(new TelnetCommand("dbstats", new String[] { "ds" })
    {
      public String getUsage()
      {
        return "dbstats";
      }

      public String handle(String[] args)
      {
        StringBuilder sb = new StringBuilder();

        sb.append("Basic database usage\n");
        sb.append("=================================================\n");
        sb.append("Connections").append("\n");
        try
        {
          sb.append("     Busy: ........................ ").append(DatabaseFactory.getInstance().getBusyConnectionCount()).append("\n");
          sb.append("     Idle: ........................ ").append(DatabaseFactory.getInstance().getIdleConnectionCount()).append("\n");
        }
        catch (SQLException e)
        {
          return new StringBuilder().append("Error: ").append(e.getMessage()).append("\n").toString();
        }

        sb.append("Players").append("\n");
        sb.append("     Update: ...................... ").append(GameStats.getUpdatePlayerBase()).append("\n");

        Cache cache = ItemsDAO.getInstance().getCache();
        LiveCacheStatistics cacheStats = cache.getLiveCacheStatistics();
        JdbcEntityStats entityStats = ItemsDAO.getInstance().getStats();

        double cacheHitCount = cacheStats.getCacheHitCount();
        double cacheMissCount = cacheStats.getCacheMissCount();
        double cacheHitRatio = cacheHitCount / (cacheHitCount + cacheMissCount);

        sb.append("Items").append("\n");
        sb.append("     getLoadCount: ................ ").append(entityStats.getLoadCount()).append("\n");
        sb.append("     getInsertCount: .............. ").append(entityStats.getInsertCount()).append("\n");
        sb.append("     getUpdateCount: .............. ").append(entityStats.getUpdateCount()).append("\n");
        sb.append("     getDeleteCount: .............. ").append(entityStats.getDeleteCount()).append("\n");
        sb.append("Cache").append("\n");
        sb.append("     getPutCount: ................. ").append(cacheStats.getPutCount()).append("\n");
        sb.append("     getUpdateCount: .............. ").append(cacheStats.getUpdateCount()).append("\n");
        sb.append("     getRemovedCount: ............. ").append(cacheStats.getRemovedCount()).append("\n");
        sb.append("     getEvictedCount: ............. ").append(cacheStats.getEvictedCount()).append("\n");
        sb.append("     getExpiredCount: ............. ").append(cacheStats.getExpiredCount()).append("\n");
        sb.append("     getSize: ..................... ").append(cacheStats.getSize()).append("\n");
        sb.append("     getInMemorySize: ............. ").append(cacheStats.getInMemorySize()).append("\n");
        sb.append("     getOnDiskSize: ............... ").append(cacheStats.getOnDiskSize()).append("\n");
        sb.append("     cacheHitRatio: ............... ").append(String.format("%2.2f", new Object[] { Double.valueOf(cacheHitRatio) })).append("\n");
        sb.append("=================================================\n");

        cache = MailDAO.getInstance().getCache();
        cacheStats = cache.getLiveCacheStatistics();
        entityStats = MailDAO.getInstance().getStats();

        cacheHitCount = cacheStats.getCacheHitCount();
        cacheMissCount = cacheStats.getCacheMissCount();
        cacheHitRatio = cacheHitCount / (cacheHitCount + cacheMissCount);

        sb.append("Mail").append("\n");
        sb.append("     getLoadCount: ................ ").append(entityStats.getLoadCount()).append("\n");
        sb.append("     getInsertCount: .............. ").append(entityStats.getInsertCount()).append("\n");
        sb.append("     getUpdateCount: .............. ").append(entityStats.getUpdateCount()).append("\n");
        sb.append("     getDeleteCount: .............. ").append(entityStats.getDeleteCount()).append("\n");
        sb.append("Cache").append("\n");
        sb.append("     getPutCount: ................. ").append(cacheStats.getPutCount()).append("\n");
        sb.append("     getUpdateCount: .............. ").append(cacheStats.getUpdateCount()).append("\n");
        sb.append("     getRemovedCount: ............. ").append(cacheStats.getRemovedCount()).append("\n");
        sb.append("     getEvictedCount: ............. ").append(cacheStats.getEvictedCount()).append("\n");
        sb.append("     getExpiredCount: ............. ").append(cacheStats.getExpiredCount()).append("\n");
        sb.append("     getSize: ..................... ").append(cacheStats.getSize()).append("\n");
        sb.append("     getInMemorySize: ............. ").append(cacheStats.getInMemorySize()).append("\n");
        sb.append("     getOnDiskSize: ............... ").append(cacheStats.getOnDiskSize()).append("\n");
        sb.append("     cacheHitRatio: ............... ").append(String.format("%2.2f", new Object[] { Double.valueOf(cacheHitRatio) })).append("\n");
        sb.append("=================================================\n");

        return sb.toString();
      }
    });
    _commands.add(new TelnetCommand("aistats", new String[] { "as" })
    {
      public String getUsage()
      {
        return "aistats";
      }

      public String handle(String[] args)
      {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < Config.AI_TASK_MANAGER_COUNT; i++)
        {
          sb.append("AiTaskManager #").append(i + 1).append("\n");
          sb.append("=================================================\n");
          sb.append(AiTaskManager.getInstance().getStats(i));
          sb.append("=================================================\n");
        }

        return sb.toString();
      }
    });
    _commands.add(new TelnetCommand("effectstats", new String[] { "es" })
    {
      public String getUsage()
      {
        return "effectstats";
      }

      public String handle(String[] args)
      {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < Config.EFFECT_TASK_MANAGER_COUNT; i++)
        {
          sb.append("EffectTaskManager #").append(i + 1).append("\n");
          sb.append("=================================================\n");
          sb.append(EffectTaskManager.getInstance().getStats(i));
          sb.append("=================================================\n");
        }

        return sb.toString();
      }
    });
  }

  public Set<TelnetCommand> getCommands()
  {
    return _commands;
  }
}