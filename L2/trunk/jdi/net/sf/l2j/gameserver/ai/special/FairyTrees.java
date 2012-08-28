package net.sf.l2j.gameserver.ai.special;

import java.util.ArrayList;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.Quest.QuestEventType;
import net.sf.l2j.util.Rnd;

public class FairyTrees extends Quest
{
  private ArrayList<mobs> _mobs = new ArrayList();

  public FairyTrees(int questId, String name, String descr)
  {
    super(questId, name, descr);

    _mobs.add(new mobs(27185, null));
    _mobs.add(new mobs(27186, null));
    _mobs.add(new mobs(27187, null));
    _mobs.add(new mobs(27188, null));

    int[] mobsKill = { 27185, 27186, 27187, 27188 };

    for (int mob : mobsKill)
    {
      addEventId(mob, Quest.QuestEventType.ON_KILL);
    }
  }

  public String onKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet)
  {
    int npcId = npc.getNpcId();
    for (mobs monster : _mobs)
    {
      if (npcId == monster.getId())
      {
        for (int i = 0; i < 20; i++)
        {
          L2Attackable newNpc = (L2Attackable)addSpawn(27189, npc.getX(), npc.getY(), npc.getZ(), 0, false, 30000);
          L2Character originalKiller = isPet ? killer.getPet() : killer;
          newNpc.setRunning();
          newNpc.addDamageHate(originalKiller, 0, 999);
          newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, originalKiller);
          if (Rnd.get(1, 2) != 1)
            continue;
          L2Skill skill = SkillTable.getInstance().getInfo(4243, 1);
          if ((skill == null) || (originalKiller == null))
            continue;
          skill.getEffects(newNpc, originalKiller);
        }

      }

    }

    return super.onKill(npc, killer, isPet);
  }

  private static class mobs
  {
    private int _id;

    private mobs(int id)
    {
      _id = id;
    }

    private int getId()
    {
      return _id;
    }
  }
}