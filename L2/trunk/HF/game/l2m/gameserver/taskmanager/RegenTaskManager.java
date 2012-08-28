package l2m.gameserver.taskmanager;

import l2p.commons.threading.RunnableImpl;
import l2p.commons.threading.SteppingRunnableQueueManager;
import l2m.gameserver.ThreadPoolManager;

public class RegenTaskManager extends SteppingRunnableQueueManager
{
  private static final RegenTaskManager _instance = new RegenTaskManager();

  public static final RegenTaskManager getInstance() {
    return _instance;
  }

  private RegenTaskManager()
  {
    super(1000L);
    ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 1000L, 1000L);

    ThreadPoolManager.getInstance().scheduleAtFixedRate(new RunnableImpl()
    {
      public void runImpl()
        throws Exception
      {
        purge();
      }
    }
    , 10000L, 10000L);
  }
}