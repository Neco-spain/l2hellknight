package net.sf.l2j.gameserver.instancemanager;

import java.io.PrintStream;
import java.util.Map;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.scripting.ScriptManager;

public class QuestManager extends ScriptManager<Quest>
{
  protected static final Logger _log = Logger.getLogger(QuestManager.class.getName());
  private static QuestManager _instance;
  private Map<String, Quest> _quests = new FastMap();

  public static final QuestManager getInstance()
  {
    if (_instance == null)
    {
      System.out.println("Initializing QuestManager");
      _instance = new QuestManager();
    }
    return _instance;
  }

  public final boolean reload(String questFolder)
  {
    Quest q = getQuest(questFolder);
    if (q == null)
    {
      return false;
    }
    return q.reload();
  }

  public final boolean reload(int questId)
  {
    Quest q = getQuest(questId);
    if (q == null)
    {
      return false;
    }
    return q.reload();
  }

  public final void report()
  {
    _log.info("Loaded: " + getQuests().size() + " quests");
  }

  public final void save()
  {
    for (Quest q : getQuests().values())
    {
      q.saveGlobalData();
    }
  }

  public final Quest getQuest(String name)
  {
    return (Quest)getQuests().get(name);
  }

  public final Quest getQuest(int questId)
  {
    for (Quest q : getQuests().values())
    {
      if (q.getQuestIntId() == questId)
        return q;
    }
    return null;
  }

  public final void addQuest(Quest newQuest)
  {
    if (newQuest == null)
    {
      throw new IllegalArgumentException("Quest argument cannot be null");
    }
    Quest old = (Quest)getQuests().put(newQuest.getName(), newQuest);
    if (old != null)
    {
      _log.info("Replaced: (" + old.getName() + ") with a new version (" + newQuest.getName() + ")");
    }
  }

  public final boolean removeQuest(Quest q)
  {
    return getQuests().remove(q.getName()) != null;
  }

  public final FastMap<String, Quest> getQuests()
  {
    if (_quests == null) _quests = new FastMap();
    return (FastMap)_quests;
  }

  public Iterable<Quest> getAllManagedScripts()
  {
    return _quests.values();
  }

  public boolean unload(Quest ms)
  {
    ms.saveGlobalData();
    return removeQuest(ms);
  }

  public String getScriptManagerName()
  {
    return "QuestManager";
  }
}