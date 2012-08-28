package net.sf.l2j.gameserver.taskmanager;

import java.util.Map;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.network.serverpackets.AutoAttackStop;
import net.sf.l2j.util.log.AbstractLogger;

public class AttackStanceTaskManager
{
  protected static final Logger _log = AbstractLogger.getLogger(AttackStanceTaskManager.class.getName());

  protected Map<L2Character, Long> _attackStanceTasks = new FastMap().shared("AttackStanceTaskManager._attackStanceTasks");
  private static AttackStanceTaskManager _instance;

  public static AttackStanceTaskManager getInstance()
  {
    return _instance;
  }

  public static void init()
  {
    _instance = new AttackStanceTaskManager();
    _instance.load();
  }

  private void load()
  {
    ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new FightModeScheduler(), 0L, 1000L);
  }

  public void addAttackStanceTask(L2Character actor)
  {
    _attackStanceTasks.put(actor, Long.valueOf(System.currentTimeMillis()));
  }

  public void removeAttackStanceTask(L2Character actor)
  {
    _attackStanceTasks.remove(actor);
  }

  public boolean getAttackStanceTask(L2Character actor)
  {
    return _attackStanceTasks.containsKey(actor);
  }

  private class FightModeScheduler
    implements Runnable
  {
    protected FightModeScheduler()
    {
    }

    public void run()
    {
      Long current = Long.valueOf(System.currentTimeMillis());
      try
      {
        if (_attackStanceTasks != null)
          synchronized (this) {
            for (L2Character actor : _attackStanceTasks.keySet())
            {
              if (current.longValue() - ((Long)_attackStanceTasks.get(actor)).longValue() > 15000L)
              {
                actor.broadcastPacket(new AutoAttackStop(actor.getObjectId()));
                actor.getAI().setAutoAttacking(false);
                _attackStanceTasks.remove(actor);
              }
            }
          }
      }
      catch (Throwable e) {
        AttackStanceTaskManager._log.warning(e.toString());
      }
    }
  }
}