package net.sf.l2j.gameserver.instancemanager;

import java.util.Map;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.jython.QuestJython;
import net.sf.l2j.util.log.AbstractLogger;

public class QuestManager
{
  protected static final Logger _log = AbstractLogger.getLogger(QuestManager.class.getName());
  private static QuestManager _instance;
  private Map<String, Quest> _quests = new FastMap();

  public static final QuestManager getInstance()
  {
    return _instance;
  }

  public static void init()
  {
    _log.info("Initializing QuestManager");
    _instance = new QuestManager();
    if (!Config.ALT_DEV_NO_QUESTS)
      _instance.load();
  }

  public final boolean reload(String questFolder)
  {
    Quest q = getQuest(questFolder);
    String path = "";
    if (q != null)
    {
      q.saveGlobalData();
      path = q.getPrefixPath();
    }
    return QuestJython.reloadQuest(path + questFolder);
  }

  public final boolean reload(int questId)
  {
    Quest q = getQuest(questId);
    if (q == null)
    {
      return false;
    }
    q.saveGlobalData();
    return QuestJython.reloadQuest(q.getPrefixPath() + q.getName());
  }

  private final void load()
  {
    QuestJython.init();
    _log.info("QuestManager: Loaded " + getQuests().size() + " quests");
  }

  public final void save() {
    for (Quest q : getQuests().values())
      q.saveGlobalData();
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
    if (getQuests().containsKey(newQuest.getName())) {
      _log.info("QuestManager: Replaced " + newQuest.getName() + " with a new version");
    }

    getQuests().put(newQuest.getName(), newQuest);
  }

  public final FastMap<String, Quest> getQuests()
  {
    if (_quests == null) _quests = new FastMap();
    return (FastMap)_quests;
  }
}