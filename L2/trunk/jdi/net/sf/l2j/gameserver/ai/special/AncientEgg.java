package net.sf.l2j.gameserver.ai.special;

import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.Quest.QuestEventType;

public class AncientEgg extends Quest
{
  private static final int NPC = 18344;

  public AncientEgg(int questId, String name, String descr)
  {
    super(questId, name, descr);

    addEventId(18344, Quest.QuestEventType.ON_ATTACK);
  }

  public String onAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
  {
    if (npc.getNpcId() == 18344)
    {
      npc.setTarget(attacker);
      npc.doCast(SkillTable.getInstance().getInfo(5088, 1));
    }
    return super.onAttack(npc, attacker, damage, isPet);
  }
}