package l2p.gameserver.taskmanager;

import l2p.commons.threading.RunnableImpl;
import l2p.commons.threading.SteppingRunnableQueueManager;
import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.ThreadPoolManager;

public class AiTaskManager extends SteppingRunnableQueueManager
{
  private static final long TICK = 250L;
  private static final AiTaskManager[] _instances = new AiTaskManager[Config.AI_TASK_MANAGER_COUNT];
  private static int randomizer;

  public static final AiTaskManager getInstance()
  {
    return _instances[(randomizer++ & _instances.length - 1)];
  }

  private AiTaskManager()
  {
    super(250L);
    ThreadPoolManager.getInstance().scheduleAtFixedRate(this, Rnd.get(250L), 250L);

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

  public CharSequence getStats(int num)
  {
    return _instances[num].getStats();
  }

  static
  {
    for (int i = 0; i < _instances.length; i++) {
      _instances[i] = new AiTaskManager();
    }

    randomizer = 0;
  }
}