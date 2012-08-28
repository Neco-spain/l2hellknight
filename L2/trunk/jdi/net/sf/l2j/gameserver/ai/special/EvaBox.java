package net.sf.l2j.gameserver.ai.special;

import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.Quest.QuestEventType;
import net.sf.l2j.util.Rnd;

public class EvaBox extends Quest
{
  private static final int[] KISS_OF_EVA = { 1073, 3141, 3252 };
  private static final int BOX = 32342;
  private static final int[] REWARDS = { 9692, 9693 };

  public EvaBox(int questId, String name, String descr)
  {
    super(questId, name, descr);

    addEventId(32342, Quest.QuestEventType.ON_KILL);
  }

  public void dropItem(L2NpcInstance npc, int itemId, int count, L2PcInstance player)
  {
    L2ItemInstance ditem = ItemTable.getInstance().createItem("Loot", itemId, count, player);
    ditem.dropMe(npc, npc.getX(), npc.getY(), npc.getZ());
  }

  public String onKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet)
  {
    boolean found = false;
    L2Effect[] effects = killer.getAllEffects();
    for (L2Effect effect : effects)
    {
      for (int i = 0; i < 3; i++)
      {
        if (effect.getSkill().getId() == KISS_OF_EVA[i]) {
          found = true;
        }
      }
    }
    if (found == true)
    {
      int dropid = Rnd.get(1);
      if (dropid == 1)
        dropItem(npc, REWARDS[dropid], 1, killer);
      else if (dropid == 0) {
        dropItem(npc, REWARDS[dropid], 1, killer);
      }
    }
    return super.onKill(npc, killer, isPet);
  }
}