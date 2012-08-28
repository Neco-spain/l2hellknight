package net.sf.l2j.gameserver.model.quest;

import net.sf.l2j.gameserver.instancemanager.QuestManager;

public class State
{
  private String _questName;
  private String _name;

  public State(String name, Quest quest)
  {
    _name = name;
    _questName = quest.getName();
    quest.addState(this);
  }

  public void addQuestDrop(int npcId, int itemId, int chance)
  {
    QuestManager.getInstance().getQuest(_questName).registerItem(itemId);
  }

  public String getName()
  {
    return _name;
  }

  public String toString()
  {
    return _name;
  }
}