package l2m.gameserver.taskmanager;

import java.util.concurrent.Future;
import l2p.commons.threading.RunnableImpl;
import l2p.commons.threading.SteppingRunnableQueueManager;
import l2m.gameserver.ThreadPoolManager;
import l2m.gameserver.model.Creature;

public class DecayTaskManager extends SteppingRunnableQueueManager
{
  private static final DecayTaskManager _instance = new DecayTaskManager();

  public static final DecayTaskManager getInstance() {
    return _instance;
  }

  private DecayTaskManager()
  {
    super(500L);

    ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 500L, 500L);

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

  public Future<?> addDecayTask(Creature actor, long delay)
  {
    return schedule(new RunnableImpl(actor)
    {
      public void runImpl()
        throws Exception
      {
        val$actor.doDecay();
      }
    }
    , delay);
  }
}