package l2p.gameserver.taskmanager;

import java.util.concurrent.Future;
import l2p.commons.threading.RunnableImpl;
import l2p.commons.threading.SteppingRunnableQueueManager;
import l2p.commons.util.Rnd;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.model.Player;

public class AutoSaveManager extends SteppingRunnableQueueManager
{
  private static final AutoSaveManager _instance = new AutoSaveManager();

  public static final AutoSaveManager getInstance()
  {
    return _instance;
  }

  private AutoSaveManager()
  {
    super(10000L);
    ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 10000L, 10000L);

    ThreadPoolManager.getInstance().scheduleAtFixedRate(new RunnableImpl()
    {
      public void runImpl()
        throws Exception
      {
        purge();
      }
    }
    , 60000L, 60000L);
  }

  public Future<?> addAutoSaveTask(Player player)
  {
    long delay = Rnd.get(180, 360) * 1000L;

    return scheduleAtFixedRate(new RunnableImpl(player)
    {
      public void runImpl()
        throws Exception
      {
        if (!val$player.isOnline()) {
          return;
        }
        val$player.store(true);
      }
    }
    , delay, delay);
  }
}