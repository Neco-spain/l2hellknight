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
  private ScheduledFuture<?> _schedular;

  public QuestTimer(Quest quest, String name, long time, L2NpcInstance npc, L2PcInstance player)
  {
    _name = name;
    _quest = quest;
    _player = player;
    _npc = npc;
    _schedular = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTimerTask(), time);
  }

  public QuestTimer(QuestState qs, String name, long time)
  {
    _name = name;
    _quest = qs.getQuest();
    _player = qs.getPlayer();
    _npc = null;
    _schedular = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTimerTask(), time);
  }

  public void cancel()
  {
    _isActive = false;

    if (_schedular != null) _schedular.cancel(true);

    getQuest().removeQuestTimer(this);
  }

  public boolean isMatch(Quest quest, String name, L2NpcInstance npc, L2PcInstance player)
  {
    if ((quest == null) || (name == null))
      return false;
    if ((quest != getQuest()) || (name.compareToIgnoreCase(getName()) != 0))
      return false;
    return ((npc == null) || (getNpc() == null) || (npc == getNpc())) && ((player == null) || (getPlayer() == null) || (player == getPlayer()));
  }

  public final boolean getIsActive()
  {
    return _isActive;
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
      if ((this == null) || (!getIsActive())) return;

      try
      {
        getQuest().notifyEvent(getName(), getNpc(), getPlayer());
        cancel();
      }
      catch (Throwable t)
      {
      }
    }
  }
}