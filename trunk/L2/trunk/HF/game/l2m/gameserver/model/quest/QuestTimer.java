package l2m.gameserver.model.quest;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import l2p.commons.threading.RunnableImpl;
import l2m.gameserver.ThreadPoolManager;
import l2m.gameserver.model.instances.NpcInstance;

public class QuestTimer
{
  private String _name;
  private NpcInstance _npc;
  private long _time;
  private QuestState _qs;
  private ScheduledFuture<?> _schedule;

  public QuestTimer(String name, long time, NpcInstance npc)
  {
    _name = name;
    _time = time;
    _npc = npc;
  }

  void setQuestState(QuestState qs)
  {
    _qs = qs;
  }

  QuestState getQuestState()
  {
    return _qs;
  }

  void start()
  {
    _schedule = ThreadPoolManager.getInstance().schedule(new StartEvent(null), _time);
  }

  void pause()
  {
    if (_schedule != null)
    {
      _time = _schedule.getDelay(TimeUnit.SECONDS);
      _schedule.cancel(false);
    }
  }

  void stop()
  {
    if (_schedule != null)
      _schedule.cancel(false);
  }

  public boolean isActive()
  {
    return (_schedule != null) && (!_schedule.isDone());
  }

  public String getName()
  {
    return _name;
  }

  public long getTime()
  {
    return _time;
  }

  public NpcInstance getNpc()
  {
    return _npc;
  }

  public final String toString()
  {
    return _name;
  }

  public boolean equals(Object o)
  {
    if (o == this)
      return true;
    if (o == null)
      return false;
    if (o.getClass() != getClass())
      return false;
    return ((QuestTimer)o).getName().equals(getName());
  }

  private class StartEvent extends RunnableImpl
  {
    private StartEvent()
    {
    }

    public void runImpl() {
      QuestState qs = getQuestState();
      if (qs != null)
      {
        qs.getQuest().notifyEvent(getName(), qs, getNpc());
        qs.removeQuestTimer(getName());
      }
    }
  }
}