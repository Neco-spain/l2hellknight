package net.sf.l2j.gameserver.model.quest;

import java.util.concurrent.ScheduledFuture;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class QuestTimer
{
  private boolean _isActive = true;
  private String _name;
  private Quest _quest;
  private L2NpcInstance _npc;
  private L2PcInstance _player;
  private boolean _isRepeating;
  private ScheduledFuture<?> _schedular;

  public QuestTimer(Quest quest, String name, long time, L2NpcInstance npc, L2PcInstance player, boolean repeating)
  {
    _name = name;
    _quest = quest;
    _player = player;
    _npc = npc;
    _isRepeating = repeating;
    if (repeating)
      _schedular = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ScheduleTimerTask(), time, time);
    else
      _schedular = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTimerTask(), time);
  }

  public QuestTimer(Quest quest, String name, long time, L2NpcInstance npc, L2PcInstance player)
  {
    this(quest, name, time, npc, player, false);
  }

  public QuestTimer(QuestState qs, String name, long time)
  {
    this(qs.getQuest(), name, time, null, qs.getPlayer(), false);
  }

  public void cancel()
  {
    _isActive = false;

    if (_schedular != null) {
      _schedular.cancel(false);
    }
    getQuest().removeQuestTimer(this);
  }

  public boolean isMatch(Quest quest, String name, L2NpcInstance npc, L2PcInstance player)
  {
    if ((quest == null) || (name == null))
      return false;
    if ((quest != getQuest()) || (name.compareToIgnoreCase(getName()) != 0))
      return false;
    return (npc == getNpc()) && (player == getPlayer());
  }

  public final boolean getIsActive()
  {
    return _isActive;
  }

  public final boolean getIsRepeating()
  {
    return _isRepeating;
  }

  public final Quest getQuest()
  {
    return _quest;
  }

  public final String getName()
  {
    return _name;
  }

  public final L2NpcInstance getNpc()
  {
    return _npc;
  }

  public final L2PcInstance getPlayer()
  {
    return _player;
  }

  public final String toString()
  {
    return _name;
  }

  public class ScheduleTimerTask
    implements Runnable
  {
    public ScheduleTimerTask()
    {
    }

    public void run()
    {
      if ((this == null) || (!getIsActive())) {
        return;
      }
      try
      {
        if (!getIsRepeating())
          cancel();
        getQuest().notifyEvent(getName(), getNpc(), getPlayer());
      }
      catch (Throwable t)
      {
        t.printStackTrace();
      }
    }
  }
}