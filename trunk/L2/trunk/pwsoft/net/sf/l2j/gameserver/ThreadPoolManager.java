package net.sf.l2j.gameserver;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.network.clientpackets.L2GameClientPacket;
import net.sf.l2j.util.log.AbstractLogger;

public class ThreadPoolManager
{
  private static final Logger _log = AbstractLogger.getLogger(ThreadPoolManager.class.getName());
  private static ThreadPoolManager _instance;
  private ScheduledThreadPoolExecutor _effectsScheduledThreadPool;
  private ScheduledThreadPoolExecutor _generalScheduledThreadPool;
  private ScheduledThreadPoolExecutor _npcAiScheduledThreadPool;
  private ScheduledThreadPoolExecutor _playerAiScheduledThreadPool;
  private ThreadPoolExecutor _generalPacketsThreadPool;
  private ThreadPoolExecutor _pathfindThreadPool;
  private ThreadPoolExecutor _ioPacketsThreadPool;
  private boolean _shutdown;

  public static ThreadPoolManager getInstance()
  {
    return _instance;
  }

  public static void init() {
    _instance = new ThreadPoolManager();
    _instance.load();
  }

  private void load()
  {
    _generalScheduledThreadPool = new ScheduledThreadPoolExecutor(Config.THREAD_P_GENERAL, new PriorityThreadFactory("GerenalSTPool", 6));
    _generalScheduledThreadPool.setKeepAliveTime(1L, TimeUnit.SECONDS);
    _generalScheduledThreadPool.allowCoreThreadTimeOut(true);

    _effectsScheduledThreadPool = new ScheduledThreadPoolExecutor(Config.THREAD_P_EFFECTS, new PriorityThreadFactory("EffectsSTPool", 1));
    _effectsScheduledThreadPool.setKeepAliveTime(1L, TimeUnit.SECONDS);
    _effectsScheduledThreadPool.allowCoreThreadTimeOut(true);

    _npcAiScheduledThreadPool = new ScheduledThreadPoolExecutor(Config.NPC_AI_MAX_THREAD, new PriorityThreadFactory("NpcAiSTPool", 3));
    _npcAiScheduledThreadPool.setKeepAliveTime(1L, TimeUnit.SECONDS);
    _npcAiScheduledThreadPool.allowCoreThreadTimeOut(true);

    _playerAiScheduledThreadPool = new ScheduledThreadPoolExecutor(Config.PLAYER_AI_MAX_THREAD, new PriorityThreadFactory("PlayerAiSTPool", 7));
    _playerAiScheduledThreadPool.setKeepAliveTime(1L, TimeUnit.SECONDS);
    _playerAiScheduledThreadPool.allowCoreThreadTimeOut(true);

    _pathfindThreadPool = new ThreadPoolExecutor(Config.THREAD_P_PATHFIND, Config.THREAD_P_PATHFIND, 1L, TimeUnit.SECONDS, new LinkedBlockingQueue(), new PriorityThreadFactory("Pathfind Pool", 5));
    _pathfindThreadPool.allowCoreThreadTimeOut(true);

    _ioPacketsThreadPool = new ThreadPoolExecutor(Config.GENERAL_PACKET_THREAD_CORE_SIZE / 2, 2147483647, 5L, TimeUnit.SECONDS, new LinkedBlockingQueue(), new PriorityThreadFactory("High Packet Pool", 8));

    if (Config.GENERAL_PACKET_THREAD_CORE_SIZE == -1)
      _generalPacketsThreadPool = new ThreadPoolExecutor(0, 2147483647, 15L, TimeUnit.SECONDS, new SynchronousQueue(), new PriorityThreadFactory("Normal Packet Pool", 8));
    else
      _generalPacketsThreadPool = new ThreadPoolExecutor(Config.GENERAL_PACKET_THREAD_CORE_SIZE, Config.GENERAL_PACKET_THREAD_CORE_SIZE * 2, 120L, TimeUnit.SECONDS, new LinkedBlockingQueue(), new PriorityThreadFactory("Normal Packet Pool", 8));
  }

  public ScheduledFuture<?> scheduleEffect(Runnable r, long delay)
  {
    try {
      if (delay < 0L) {
        delay = 0L;
      }
      return _effectsScheduledThreadPool.schedule(r, delay, TimeUnit.MILLISECONDS); } catch (RejectedExecutionException e) {
    }
    return null;
  }

  public ScheduledFuture<?> scheduleEffectAtFixedRate(Runnable r, long initial, long delay) {
    try {
      if (delay < 0L) {
        delay = 0L;
      }
      if (initial < 0L) {
        initial = 0L;
      }
      return _effectsScheduledThreadPool.scheduleAtFixedRate(r, initial, delay, TimeUnit.MILLISECONDS); } catch (RejectedExecutionException e) {
    }
    return null;
  }

  public ScheduledFuture<?> scheduleGeneral(Runnable r, long delay) {
    try {
      if (delay < 0L) {
        delay = 0L;
      }
      return _generalScheduledThreadPool.schedule(r, delay, TimeUnit.MILLISECONDS); } catch (RejectedExecutionException e) {
    }
    return null;
  }

  public ScheduledFuture<?> scheduleGeneralAtFixedRate(Runnable r, long initial, long delay) {
    try {
      if (delay < 0L) {
        delay = 0L;
      }
      if (initial < 0L) {
        initial = 0L;
      }
      return _generalScheduledThreadPool.scheduleAtFixedRate(r, initial, delay, TimeUnit.MILLISECONDS); } catch (RejectedExecutionException e) {
    }
    return null;
  }

  public <T extends Runnable> ScheduledFuture<T> scheduleAi(T r, long delay, boolean isPlayer) {
    try {
      if (delay < 0L) {
        delay = 0L;
      }

      if (isPlayer) {
        return _playerAiScheduledThreadPool.schedule(r, delay, TimeUnit.MILLISECONDS);
      }

      return _npcAiScheduledThreadPool.schedule(r, delay, TimeUnit.MILLISECONDS); } catch (RejectedExecutionException e) {
    }
    return null;
  }

  public <T extends Runnable> ScheduledFuture<T> scheduleAiAtFixedRate(T r, long initial, long delay) {
    try {
      if (delay < 0L) {
        delay = 0L;
      }
      if (initial < 0L) {
        initial = 0L;
      }

      return _npcAiScheduledThreadPool.scheduleAtFixedRate(r, initial, delay, TimeUnit.MILLISECONDS); } catch (RejectedExecutionException e) {
    }
    return null;
  }

  public void executePacket(L2GameClientPacket pkt) {
    _generalPacketsThreadPool.execute(pkt);
  }

  public void executeIOPacket(L2GameClientPacket pkt) {
    _ioPacketsThreadPool.execute(pkt);
  }

  public void executeGeneral(Runnable r) {
    _generalScheduledThreadPool.execute(r);
  }

  public void executeAi(Runnable r, boolean isPlayer) {
    if (isPlayer)
      _playerAiScheduledThreadPool.execute(r);
    else
      _npcAiScheduledThreadPool.execute(r);
  }

  public void executePathfind(Runnable r)
  {
    _pathfindThreadPool.execute(r);
  }

  public String[] getStats() {
    return new String[] { "STP:", " + Effects:", " |- ActiveThreads:   " + _effectsScheduledThreadPool.getActiveCount(), " |- getCorePoolSize: " + _effectsScheduledThreadPool.getCorePoolSize(), " |- PoolSize:        " + _effectsScheduledThreadPool.getPoolSize(), " |- MaximumPoolSize: " + _effectsScheduledThreadPool.getMaximumPoolSize(), " |- CompletedTasks:  " + _effectsScheduledThreadPool.getCompletedTaskCount(), " |- ScheduledTasks:  " + (_effectsScheduledThreadPool.getTaskCount() - _effectsScheduledThreadPool.getCompletedTaskCount()), " | -------", " + General:", " |- ActiveThreads:   " + _generalScheduledThreadPool.getActiveCount(), " |- getCorePoolSize: " + _generalScheduledThreadPool.getCorePoolSize(), " |- PoolSize:        " + _generalScheduledThreadPool.getPoolSize(), " |- MaximumPoolSize: " + _generalScheduledThreadPool.getMaximumPoolSize(), " |- CompletedTasks:  " + _generalScheduledThreadPool.getCompletedTaskCount(), " |- ScheduledTasks:  " + (_generalScheduledThreadPool.getTaskCount() - _generalScheduledThreadPool.getCompletedTaskCount()), " | -------", "TP:", " + AI:", " |- Not Done" };
  }

  public void shutdown()
  {
    _shutdown = true;
    try {
      _effectsScheduledThreadPool.shutdown();
      _generalScheduledThreadPool.shutdown();
      _npcAiScheduledThreadPool.shutdown();
      _playerAiScheduledThreadPool.shutdown();
      _pathfindThreadPool.shutdown();
      _ioPacketsThreadPool.shutdown();
      _generalPacketsThreadPool.shutdown();

      _effectsScheduledThreadPool.awaitTermination(1L, TimeUnit.SECONDS);
      _generalScheduledThreadPool.awaitTermination(1L, TimeUnit.SECONDS);
      _generalPacketsThreadPool.awaitTermination(1L, TimeUnit.SECONDS);
      _pathfindThreadPool.awaitTermination(1L, TimeUnit.SECONDS);
      _ioPacketsThreadPool.awaitTermination(1L, TimeUnit.SECONDS);
      _playerAiScheduledThreadPool.awaitTermination(1L, TimeUnit.SECONDS);
      _npcAiScheduledThreadPool.awaitTermination(1L, TimeUnit.SECONDS);

      _log.info("All ThreadPools are now stoped");
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
  }

  public boolean isShutdown() {
    return _shutdown;
  }

  public void purge()
  {
    _effectsScheduledThreadPool.purge();
    _generalScheduledThreadPool.purge();
    _npcAiScheduledThreadPool.purge();
    _playerAiScheduledThreadPool.purge();
  }

  public static class PriorityThreadFactory
    implements ThreadFactory
  {
    private int _prio;
    private String _name;
    private AtomicInteger _threadNumber = new AtomicInteger(1);
    private ThreadGroup _group;

    public PriorityThreadFactory(String name, int prio)
    {
      _prio = prio;
      _name = name;
      _group = new ThreadGroup(_name);
    }

    public Thread newThread(Runnable r)
    {
      Thread t = new Thread(_group, r);
      t.setName(_name + "-" + _threadNumber.getAndIncrement());
      t.setPriority(_prio);
      return t;
    }

    public ThreadGroup getGroup() {
      return _group;
    }
  }
}