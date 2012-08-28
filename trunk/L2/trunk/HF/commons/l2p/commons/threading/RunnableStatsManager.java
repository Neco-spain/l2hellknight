package l2m.commons.threading;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class RunnableStatsManager
{
  private static final RunnableStatsManager _instance = new RunnableStatsManager();
  private final Map<Class<?>, ClassStat> classStats;
  private final Lock lock;

  public RunnableStatsManager()
  {
    classStats = new HashMap();
    lock = new ReentrantLock();
  }

  public static final RunnableStatsManager getInstance()
  {
    return _instance;
  }

  public void handleStats(Class<?> cl, long runTime)
  {
    try
    {
      lock.lock();

      ClassStat stat = (ClassStat)classStats.get(cl);

      if (stat == null) {
        stat = new ClassStat(cl, null);
      }
      ClassStat.access$208(stat);
      ClassStat.access$314(stat, runTime);
      if (stat.minTime > runTime)
        ClassStat.access$402(stat, runTime);
      if (stat.maxTime < runTime)
        ClassStat.access$502(stat, runTime);
    }
    finally
    {
      lock.unlock();
    }
  }

  private List<ClassStat> getSortedClassStats()
  {
    List result = Collections.emptyList();
    try
    {
      lock.lock();

      result = Arrays.asList(classStats.values().toArray(new ClassStat[classStats.size()]));
    }
    finally
    {
      lock.unlock();
    }

    Collections.sort(result, new Comparator()
    {
      public int compare(RunnableStatsManager.ClassStat c1, RunnableStatsManager.ClassStat c2)
      {
        if (c1.maxTime < c2.maxTime)
          return 1;
        if (c1.maxTime == c2.maxTime)
          return 0;
        return -1;
      }
    });
    return result;
  }

  public CharSequence getStats()
  {
    StringBuilder list = new StringBuilder();

    List stats = getSortedClassStats();

    for (ClassStat stat : stats)
    {
      list.append(stat.clazz.getName()).append(":\n");

      list.append("\tRun: ............ ").append(stat.runCount).append("\n");
      list.append("\tTime: ........... ").append(stat.runTime).append("\n");
      list.append("\tMin: ............ ").append(stat.minTime).append("\n");
      list.append("\tMax: ............ ").append(stat.maxTime).append("\n");
      list.append("\tAverage: ........ ").append(stat.runTime / stat.runCount).append("\n");
    }

    return list;
  }

  private class ClassStat
  {
    private final Class<?> clazz;
    private long runCount = 0L;
    private long runTime = 0L;
    private long minTime = 9223372036854775807L;
    private long maxTime = -9223372036854775808L;

    private ClassStat()
    {
      clazz = cl;
      classStats.put(cl, this);
    }
  }
}