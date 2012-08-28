package l2p.gameserver.instancemanager;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import l2p.gameserver.model.quest.Quest;

public class QuestManager
{
  private static Map<String, Quest> _questsByName = new ConcurrentHashMap();
  private static Map<Integer, Quest> _questsById = new ConcurrentHashMap();

  public static Quest getQuest(String name)
  {
    return (Quest)_questsByName.get(name);
  }

  public static Quest getQuest(Class<?> quest)
  {
    return getQuest(quest.getSimpleName());
  }

  public static Quest getQuest(int questId)
  {
    return (Quest)_questsById.get(Integer.valueOf(questId));
  }

  public static Quest getQuest2(String nameOrId)
  {
    if (_questsByName.containsKey(nameOrId))
      return (Quest)_questsByName.get(nameOrId);
    try
    {
      int questId = Integer.valueOf(nameOrId).intValue();
      return (Quest)_questsById.get(Integer.valueOf(questId));
    }
    catch (Exception e) {
    }
    return null;
  }

  public static void addQuest(Quest newQuest)
  {
    _questsByName.put(newQuest.getName(), newQuest);
    _questsById.put(Integer.valueOf(newQuest.getQuestIntId()), newQuest);
  }

  public static Collection<Quest> getQuests()
  {
    return _questsByName.values();
  }
}