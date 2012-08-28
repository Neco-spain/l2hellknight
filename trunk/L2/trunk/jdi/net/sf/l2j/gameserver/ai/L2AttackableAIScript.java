package net.sf.l2j.gameserver.ai;

import java.io.PrintStream;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.instancemanager.DimensionalRiftManager;
import net.sf.l2j.gameserver.instancemanager.DimensionalRiftManager.DimensionalRiftRoom;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2RiftInvaderInstance;
import net.sf.l2j.gameserver.model.entity.DimensionalRift;
import net.sf.l2j.gameserver.model.quest.Quest.QuestEventType;
import net.sf.l2j.gameserver.model.quest.jython.QuestJython;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2AttackableAIScript extends QuestJython
{
  public void registerMobs(int[] mobs)
  {
    for (int id : mobs)
    {
      addEventId(id, Quest.QuestEventType.ON_ATTACK);
      addEventId(id, Quest.QuestEventType.ON_KILL);
      addEventId(id, Quest.QuestEventType.ON_SPAWN);
      addEventId(id, Quest.QuestEventType.ON_SPELL_FINISHED);
      addEventId(id, Quest.QuestEventType.ON_SKILL_SEE);
      addEventId(id, Quest.QuestEventType.ON_FACTION_CALL);
      addEventId(id, Quest.QuestEventType.ON_AGGRO_RANGE_ENTER);
    }
  }

  public static <T> boolean contains(T[] array, T obj)
  {
    for (int i = 0; i < array.length; i++)
    {
      if (array[i] == obj)
      {
        return true;
      }
    }
    return false;
  }

  public static boolean contains(int[] array, int obj)
  {
    for (int i = 0; i < array.length; i++)
    {
      if (array[i] == obj)
      {
        return true;
      }
    }
    return false;
  }

  public L2AttackableAIScript(int questId, String name, String descr)
  {
    super(questId, name, descr);
  }

  public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
  {
    return null;
  }

  public String onSpellFinished(L2NpcInstance npc, L2PcInstance player, L2Skill skill)
  {
    return null;
  }

  public String onSkillSee(L2NpcInstance npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
  {
    if (caster == null)
    {
      return null;
    }
    if (!(npc instanceof L2Attackable))
    {
      return null;
    }

    L2Attackable attackable = (L2Attackable)npc;

    if (skill.getAggroPoints() > 0)
    {
      if ((attackable.hasAI()) && (attackable.getAI().getIntention() == CtrlIntention.AI_INTENTION_ATTACK))
      {
        L2Object npcTarget = attackable.getTarget();
        for (L2Object skillTarget : targets)
        {
          if ((npcTarget != skillTarget) && (npc != skillTarget))
            continue;
          L2Character originalCaster = isPet ? caster.getPet() : caster;
          attackable.addDamageHate(originalCaster, 0, skill.getAggroPoints() * 150 / (attackable.getLevel() + 7));
        }
      }

    }

    return null;
  }

  public String onFactionCall(L2NpcInstance npc, L2NpcInstance caller, L2PcInstance attacker, boolean isPet)
  {
    L2Character originalAttackTarget = isPet ? attacker.getPet() : attacker;
    if ((attacker.isInParty()) && (attacker.getParty().isInDimensionalRift()))
    {
      byte riftType = attacker.getParty().getDimensionalRift().getType();
      byte riftRoom = attacker.getParty().getDimensionalRift().getCurrentRoom();

      if (((caller instanceof L2RiftInvaderInstance)) && (!DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(npc.getX(), npc.getY(), npc.getZ())))
      {
        return null;
      }
    }

    npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, originalAttackTarget, Integer.valueOf(1));

    return null;
  }

  public String onAggroRangeEnter(L2NpcInstance npc, L2PcInstance player, boolean isPet)
  {
    return null;
  }

  public String onSpawn(L2NpcInstance npc)
  {
    return null;
  }

  public String onAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
  {
    if ((attacker != null) && ((npc instanceof L2Attackable)))
    {
      L2Attackable attackable = (L2Attackable)npc;

      L2Character originalAttacker = isPet ? attacker.getPet() : attacker;
      attackable.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, originalAttacker);
      attackable.addDamageHate(originalAttacker, damage, damage * 100 / (attackable.getLevel() + 7));
    }
    return null;
  }

  public String onKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet)
  {
    return null;
  }

  public static void main(String[] args)
  {
    L2AttackableAIScript ai = new L2AttackableAIScript(-1, "L2AttackableAIScript", "L2AttackableAIScript");
    for (int level = 1; level < 100; level++)
    {
      L2NpcTemplate[] templates = NpcTable.getInstance().getAllOfLevel(level);
      if ((templates == null) || (templates.length <= 0))
        continue;
      for (L2NpcTemplate t : templates)
      {
        try
        {
          if (L2Attackable.class.isAssignableFrom(Class.forName("net.sf.l2j.gameserver.model.actor.instance." + t.type + "Instance")))
          {
            ai.addEventId(t.npcId, Quest.QuestEventType.ON_ATTACK);
            ai.addEventId(t.npcId, Quest.QuestEventType.ON_KILL);
            ai.addEventId(t.npcId, Quest.QuestEventType.ON_SPAWN);
            ai.addEventId(t.npcId, Quest.QuestEventType.ON_SKILL_SEE);
            ai.addEventId(t.npcId, Quest.QuestEventType.ON_FACTION_CALL);
            ai.addEventId(t.npcId, Quest.QuestEventType.ON_AGGRO_RANGE_ENTER);
          }
        }
        catch (ClassNotFoundException ex)
        {
          System.out.println("Class not found " + t.type + "Instance");
        }
      }
    }
  }

  public String onSkillUse(L2NpcInstance npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
  {
    if (caster == null)
    {
      return null;
    }
    if (!(npc instanceof L2Attackable))
    {
      return null;
    }

    L2Attackable attackable = (L2Attackable)npc;

    int skillAggroPoints = skill.getAggroPoints();

    if (caster.getPet() != null)
    {
      if ((targets.length == 1) && (contains(targets, caster.getPet()))) {
        skillAggroPoints = 0;
      }
    }
    if (skillAggroPoints > 0)
    {
      if ((attackable.hasAI()) && (attackable.getAI().getIntention() == CtrlIntention.AI_INTENTION_ATTACK))
      {
        L2Object npcTarget = attackable.getTarget();
        for (L2Object skillTarget : targets)
        {
          if ((npcTarget != skillTarget) && (npc != skillTarget))
            continue;
          L2Character originalCaster = isPet ? caster.getPet() : caster;
          attackable.addDamageHate(originalCaster, 0, skillAggroPoints * 150 / (attackable.getLevel() + 7));
        }
      }

    }

    return null;
  }
}