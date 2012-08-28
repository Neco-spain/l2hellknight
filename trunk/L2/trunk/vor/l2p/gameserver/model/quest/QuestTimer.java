package l2p.gameserver.model.quest;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import l2p.commons.threading.RunnableImpl;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.model.instances.NpcInstance;

public class QuestTimer extends RunnableImpl
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
    _schedule = ThreadPoolManager.getInstance().schedule(this, _time);
  }

  public void runImpl()
    throws Exception
  {
    QuestState qs = getQuestState();
    if (qs != null)
    {
      qs.removeQuestTimer(getName());
      qs.getQuest().notifyEvent(getName(), qs, getNpc());
    }
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
}