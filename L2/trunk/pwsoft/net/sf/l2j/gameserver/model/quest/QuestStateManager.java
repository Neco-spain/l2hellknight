package net.sf.l2j.gameserver.model.quest;

import java.util.List;
import javolution.util.FastList;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class QuestStateManager
{
  private static QuestStateManager _instance;
  private List<QuestState> _questStates = new FastList();

  public QuestStateManager()
  {
    ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTimerTask(), 60000L);
  }

  public void addQuestState(Quest quest, L2PcInstance player, State state, boolean completed)
  {
    QuestState qs = getQuestState(player);
    if (qs == null)
      qs = new QuestState(quest, player, state, completed);
  }

  public void cleanUp()
  {
    for (int i = getQuestStates().size() - 1; i >= 0; i--)
    {
      if (((QuestState)getQuestStates().get(i)).getPlayer() != null) {
        continue;
      }
      getQuestStates().remove(i);
    }
  }

  public static final QuestStateManager getInstance()
  {
    if (_instance == null)
      _instance = new QuestStateManager();
    return _instance;
  }

  public QuestState getQuestState(L2PcInstance player)
  {
    for (int i = 0; i < getQuestStates().size(); i++)
    {
      if ((((QuestState)getQuestStates().get(i)).getPlayer() != null) && (((QuestState)getQuestStates().get(i)).getPlayer().getObjectId() == player.getObjectId())) {
        return (QuestState)getQuestStates().get(i);
      }
    }

    return null;
  }

  public List<QuestState> getQuestStates()
  {
    if (_questStates == null)
      _questStates = new FastList();
    return _questStates;
  }

  public class ScheduleTimerTask
    implements Runnable
  {
    public ScheduleTimerTask()
    {
    }

    public void run()
    {
      try
      {
        cleanUp();
        ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTimerTask(QuestStateManager.this), 60000L);
      }
      catch (Throwable t)
      {
      }
    }
  }
}