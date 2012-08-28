package net.sf.l2j.gameserver.ai.special;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2AttackableAIScript;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class ZombieGatekeepers extends L2AttackableAIScript
{
  private FastMap<Integer, FastList<L2Character>> _attackersList = new FastMap();

  public ZombieGatekeepers(int questId, String name, String descr)
  {
    super(questId, name, descr);
    super.addAttackId(22136);
    super.addAggroRangeEnterId(22136);
  }

  public String onAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
  {
    int npcObjId = npc.getObjectId();

    L2Character target = isPet ? attacker.getPet() : attacker;

    if (_attackersList.get(Integer.valueOf(npcObjId)) == null)
    {
      FastList player = new FastList();
      player.add(target);
      _attackersList.put(Integer.valueOf(npcObjId), player);
    }
    else if (!((FastList)_attackersList.get(Integer.valueOf(npcObjId))).contains(target)) {
      ((FastList)_attackersList.get(Integer.valueOf(npcObjId))).add(target);
    }
    return super.onAttack(npc, attacker, damage, isPet);
  }

  public String onAggroRangeEnter(L2NpcInstance npc, L2PcInstance player, boolean isPet)
  {
    int npcObjId = npc.getObjectId();

    L2Character target = isPet ? player.getPet() : player;

    L2ItemInstance VisitorsMark = player.getInventory().getItemByItemId(8064);
    L2ItemInstance FadedVisitorsMark = player.getInventory().getItemByItemId(8065);
    L2ItemInstance PagansMark = player.getInventory().getItemByItemId(8067);

    int mark1 = VisitorsMark == null ? 0 : VisitorsMark.getCount();
    int mark2 = FadedVisitorsMark == null ? 0 : FadedVisitorsMark.getCount();
    int mark3 = PagansMark == null ? 0 : PagansMark.getCount();

    if ((mark1 == 0) && (mark2 == 0) && (mark3 == 0))
    {
      ((L2Attackable)npc).addDamageHate(target, 0, 999);
      npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
    }
    else if ((_attackersList.get(Integer.valueOf(npcObjId)) == null) || (!((FastList)_attackersList.get(Integer.valueOf(npcObjId))).contains(target))) {
      ((L2Attackable)npc).getAggroListRP().remove(target);
    }
    else {
      ((L2Attackable)npc).addDamageHate(target, 0, 999);
      npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
    }

    return super.onAggroRangeEnter(npc, player, isPet);
  }

  public String onKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet)
  {
    int npcObjId = npc.getObjectId();
    if (_attackersList.get(Integer.valueOf(npcObjId)) != null) {
      ((FastList)_attackersList.get(Integer.valueOf(npcObjId))).clear();
    }
    return super.onKill(npc, killer, isPet);
  }
}