package net.sf.l2j.gameserver.ai.special;

import java.util.ArrayList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.Quest.QuestEventType;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.util.Rnd;

public class Transform extends Quest
{
  private ArrayList<Transformer> _mobs = new ArrayList();

  private static String[] Message = { "I cannot despise the fellow! I see his sincerity in the duel.", "Nows we truly begin!", "Fool! Right now is only practice!", "Have a look at my true strength.", "This time at the last! The end!" };

  public Transform(int questId, String name, String descr)
  {
    super(questId, name, descr);

    _mobs.add(new Transformer(21261, 21262, 1, 5, null));
    _mobs.add(new Transformer(21262, 21263, 1, 5, null));
    _mobs.add(new Transformer(21263, 21264, 1, 5, null));
    _mobs.add(new Transformer(21258, 21259, 100, 5, null));
    _mobs.add(new Transformer(20835, 21608, 1, 5, null));
    _mobs.add(new Transformer(21608, 21609, 1, 5, null));
    _mobs.add(new Transformer(20832, 21602, 1, 5, null));
    _mobs.add(new Transformer(21602, 21603, 1, 5, null));
    _mobs.add(new Transformer(20833, 21605, 1, 5, null));
    _mobs.add(new Transformer(21605, 21606, 1, 5, null));
    _mobs.add(new Transformer(21625, 21623, 1, 5, null));
    _mobs.add(new Transformer(21623, 21624, 1, 5, null));
    _mobs.add(new Transformer(20842, 21620, 1, 5, null));
    _mobs.add(new Transformer(21620, 21621, 1, 5, null));
    _mobs.add(new Transformer(20830, 20859, 100, 0, null));
    _mobs.add(new Transformer(21067, 21068, 100, 0, null));
    _mobs.add(new Transformer(21062, 21063, 100, 0, null));
    _mobs.add(new Transformer(20831, 20860, 100, 0, null));
    _mobs.add(new Transformer(21070, 21071, 100, 0, null));

    int[] mobsKill = { 20830, 21067, 21062, 20831, 21070 };

    for (int mob : mobsKill)
    {
      addEventId(mob, Quest.QuestEventType.ON_KILL);
    }

    int[] mobsAttack = { 21620, 20842, 21623, 21625, 21605, 20833, 21602, 20832, 21608, 20835, 21258 };

    for (int mob : mobsAttack)
    {
      addEventId(mob, Quest.QuestEventType.ON_ATTACK);
    }
  }

  public String onAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
  {
    for (Transformer monster : _mobs)
    {
      if (npc.getNpcId() == monster.getId())
      {
        if (Rnd.get(100) <= monster.getChance() * Config.RATE_DROP_QUEST)
        {
          if (monster.getMessage() != 0)
          {
            npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), Message[Rnd.get(monster.getMessage())]));
          }
          npc.onDecay();
          L2Attackable newNpc = (L2Attackable)addSpawn(monster.getIdPoly(), npc);
          L2Character originalAttacker = isPet ? attacker.getPet() : attacker;
          newNpc.setRunning();
          newNpc.addDamageHate(originalAttacker, 0, 999);
          newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, originalAttacker);
        }
      }
    }
    return super.onAttack(npc, attacker, damage, isPet);
  }

  public String onKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet)
  {
    for (Transformer monster : _mobs)
    {
      if (npc.getNpcId() == monster.getId())
      {
        if (monster.getMessage() != 0)
        {
          npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), Message[Rnd.get(monster.getMessage())]));
        }
        L2Attackable newNpc = (L2Attackable)addSpawn(monster.getIdPoly(), npc);
        L2Character originalAttacker = isPet ? killer.getPet() : killer;
        newNpc.setRunning();
        newNpc.addDamageHate(originalAttacker, 0, 999);
        newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, originalAttacker);
      }
    }
    return super.onKill(npc, killer, isPet);
  }

  private static class Transformer
  {
    private int _id;
    private int _idPoly;
    private int _chance;
    private int _message;

    private Transformer(int id, int idPoly, int chance, int message)
    {
      _id = id;
      _idPoly = idPoly;
      _chance = chance;
      _message = message;
    }

    private int getId()
    {
      return _id;
    }

    private int getIdPoly()
    {
      return _idPoly;
    }

    private int getChance()
    {
      return _chance;
    }

    private int getMessage()
    {
      return _message;
    }
  }
}