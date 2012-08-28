package net.sf.l2j.gameserver.model.quest.jython;

import net.sf.l2j.gameserver.model.quest.Quest;

public abstract class QuestJython extends Quest
{
  public QuestJython(int questId, String name, String descr)
  {
    super(questId, name, descr);
  }
}