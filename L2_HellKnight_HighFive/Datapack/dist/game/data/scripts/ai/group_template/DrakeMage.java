/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ai.group_template;

import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.model.L2CharPosition;
import l2.hellknight.gameserver.model.actor.L2Attackable;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;

public class DrakeMage extends L2AttackableAIScript
{
  private static final int PRINCESS = 22851;
  private L2Npc princess1;
  private L2Npc princess2;
  private L2Npc princess3;
  private L2Npc princess4;
  private L2Npc princess5;
  private L2Npc princess6;
  private static final L2CharPosition princess1_1 = new L2CharPosition(145180, 113285, -3725, 0);
  private static final L2CharPosition princess1_2 = new L2CharPosition(145890, 112583, -3725, 0);
  private static final L2CharPosition princess1_3 = new L2CharPosition(147249, 112436, -3725, 0);
  private static final L2CharPosition princess1_4 = new L2CharPosition(148885, 113022, -3725, 0);
  private static final L2CharPosition princess1_5 = new L2CharPosition(149144, 114033, -3725, 0);
  private static final L2CharPosition princess1_6 = new L2CharPosition(149074, 115274, -3725, 0);
  private static final L2CharPosition princess1_7 = new L2CharPosition(148226, 115923, -3725, 0);
  private static final L2CharPosition princess1_8 = new L2CharPosition(146786, 116347, -3725, 0);
  private static final L2CharPosition princess1_9 = new L2CharPosition(145571, 115830, -3725, 0);
  private static final L2CharPosition princess1_10 = new L2CharPosition(145080, 114976, -3725, 0);
  private static final L2CharPosition princess1_11 = new L2CharPosition(144484, 114552, -3725, 0);
  private static final L2CharPosition princess1_12 = new L2CharPosition(145128, 113925, -3725, 0);

  private static final L2CharPosition princess2_1 = new L2CharPosition(149144, 114033, -3725, 0);
  private static final L2CharPosition princess2_2 = new L2CharPosition(149074, 115274, -3725, 0);
  private static final L2CharPosition princess2_3 = new L2CharPosition(148226, 115923, -3725, 0);
  private static final L2CharPosition princess2_4 = new L2CharPosition(146786, 116347, -3725, 0);
  private static final L2CharPosition princess2_5 = new L2CharPosition(145571, 115830, -3725, 0);
  private static final L2CharPosition princess2_6 = new L2CharPosition(145080, 114976, -3725, 0);
  private static final L2CharPosition princess2_7 = new L2CharPosition(144484, 114552, -3725, 0);
  private static final L2CharPosition princess2_8 = new L2CharPosition(145128, 113925, -3725, 0);
  private static final L2CharPosition princess2_9 = new L2CharPosition(145180, 113285, -3725, 0);
  private static final L2CharPosition princess2_10 = new L2CharPosition(145890, 112583, -3725, 0);
  private static final L2CharPosition princess2_11 = new L2CharPosition(147249, 112436, -3725, 0);
  private static final L2CharPosition princess2_12 = new L2CharPosition(148885, 113022, -3725, 0);

  private static final L2CharPosition princess3_1 = new L2CharPosition(145571, 115830, -3725, 0);
  private static final L2CharPosition princess3_2 = new L2CharPosition(145080, 114976, -3725, 0);
  private static final L2CharPosition princess3_3 = new L2CharPosition(144484, 114552, -3725, 0);
  private static final L2CharPosition princess3_4 = new L2CharPosition(145128, 113925, -3725, 0);
  private static final L2CharPosition princess3_5 = new L2CharPosition(145180, 113285, -3725, 0);
  private static final L2CharPosition princess3_6 = new L2CharPosition(145890, 112583, -3725, 0);
  private static final L2CharPosition princess3_7 = new L2CharPosition(147249, 112436, -3725, 0);
  private static final L2CharPosition princess3_8 = new L2CharPosition(148885, 113022, -3725, 0);
  private static final L2CharPosition princess3_9 = new L2CharPosition(149144, 114033, -3725, 0);
  private static final L2CharPosition princess3_10 = new L2CharPosition(149074, 115274, -3725, 0);
  private static final L2CharPosition princess3_11 = new L2CharPosition(148226, 115923, -3725, 0);
  private static final L2CharPosition princess3_12 = new L2CharPosition(146786, 116347, -3725, 0);

  private static final L2CharPosition princess4_1 = new L2CharPosition(144868, 114504, -3725, 0);
  private static final L2CharPosition princess4_2 = new L2CharPosition(145085, 113825, -3725, 0);
  private static final L2CharPosition princess4_3 = new L2CharPosition(145656, 112956, -3725, 0);
  private static final L2CharPosition princess4_4 = new L2CharPosition(146602, 112187, -3647, 0);
  private static final L2CharPosition princess4_5 = new L2CharPosition(148011, 112145, -3725, 0);
  private static final L2CharPosition princess4_6 = new L2CharPosition(148781, 112726, -3725, 0);
  private static final L2CharPosition princess4_7 = new L2CharPosition(149137, 113789, -3725, 0);
  private static final L2CharPosition princess4_8 = new L2CharPosition(149312, 114978, -3725, 0);
  private static final L2CharPosition princess4_9 = new L2CharPosition(148314, 115972, -3725, 0);
  private static final L2CharPosition princess4_10 = new L2CharPosition(147086, 116425, -3708, 0);
  private static final L2CharPosition princess4_11 = new L2CharPosition(145557, 115653, -3725, 0);
  private static final L2CharPosition princess4_12 = new L2CharPosition(145150, 114855, -3725, 0);

  private static final L2CharPosition princess5_1 = new L2CharPosition(148011, 112145, -3725, 0);
  private static final L2CharPosition princess5_2 = new L2CharPosition(148781, 112726, -3725, 0);
  private static final L2CharPosition princess5_3 = new L2CharPosition(149137, 113789, -3725, 0);
  private static final L2CharPosition princess5_4 = new L2CharPosition(149312, 114978, -3725, 0);
  private static final L2CharPosition princess5_5 = new L2CharPosition(148314, 115972, -3725, 0);
  private static final L2CharPosition princess5_6 = new L2CharPosition(147086, 116425, -3708, 0);
  private static final L2CharPosition princess5_7 = new L2CharPosition(145557, 115653, -3725, 0);
  private static final L2CharPosition princess5_8 = new L2CharPosition(145150, 114855, -3725, 0);
  private static final L2CharPosition princess5_9 = new L2CharPosition(144868, 114504, -3725, 0);
  private static final L2CharPosition princess5_10 = new L2CharPosition(145085, 113825, -3725, 0);
  private static final L2CharPosition princess5_11 = new L2CharPosition(145656, 112956, -3725, 0);
  private static final L2CharPosition princess5_12 = new L2CharPosition(146602, 112187, -3647, 0);

  private static final L2CharPosition princess6_1 = new L2CharPosition(148314, 115972, -3725, 0);
  private static final L2CharPosition princess6_2 = new L2CharPosition(147086, 116425, -3708, 0);
  private static final L2CharPosition princess6_3 = new L2CharPosition(145557, 115653, -3725, 0);
  private static final L2CharPosition princess6_4 = new L2CharPosition(145150, 114855, -3725, 0);
  private static final L2CharPosition princess6_5 = new L2CharPosition(144868, 114504, -3725, 0);
  private static final L2CharPosition princess6_6 = new L2CharPosition(145085, 113825, -3725, 0);
  private static final L2CharPosition princess6_7 = new L2CharPosition(145656, 112956, -3725, 0);
  private static final L2CharPosition princess6_8 = new L2CharPosition(146602, 112187, -3647, 0);
  private static final L2CharPosition princess6_9 = new L2CharPosition(148011, 112145, -3725, 0);
  private static final L2CharPosition princess6_10 = new L2CharPosition(148781, 112726, -3725, 0);
  private static final L2CharPosition princess6_11 = new L2CharPosition(149137, 113789, -3725, 0);
  private static final L2CharPosition princess6_12 = new L2CharPosition(149312, 114978, -3725, 0);

  public DrakeMage(int questId, String name, String descr)
  {
    super(questId, name, descr);

    addKillId(PRINCESS);
    addAggroRangeEnterId(PRINCESS);

    startQuestTimer("princess1_spawn", 7000L, null, null);
    startQuestTimer("princess2_spawn", 7000L, null, null);
    startQuestTimer("princess3_spawn", 7000L, null, null);
    startQuestTimer("princess4_spawn", 7000L, null, null);
    startQuestTimer("princess5_spawn", 7000L, null, null);
    startQuestTimer("princess6_spawn", 7000L, null, null);
  }

  @Override
public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
  {
    if ((event.equalsIgnoreCase("princess1_spawn")) && (this.princess1 == null))
    {
      this.princess1 = addSpawn(PRINCESS, 145180, 113285, -3725, 0, false, 0L);
      this.princess1.setIsNoRndWalk(true);
      this.princess1.setRunning();
      startQuestTimer("trasa_1", 7000L, this.princess1, null);
    }
    else if ((event.equalsIgnoreCase("trasa_nova")) && (this.princess1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_nova", 7000L, this.princess1, null);
      }
      else
      {
        this.princess1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess1_1);
        this.princess1.setRunning();
        startQuestTimer("trasa_1", 7000L, this.princess1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_1")) && (this.princess1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_1", 7000L, this.princess1, null);
      }
      else
      {
        if (!this.princess1.isInsideRadius(princess1_1.x, princess1_1.y, princess1_1.z, 100, true, false))
          this.princess1.teleToLocation(princess1_1.x, princess1_1.y, princess1_1.z);
        this.princess1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess1_2);
        this.princess1.setRunning();
        startQuestTimer("trasa_2", 7000L, this.princess1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_2")) && (this.princess1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_2", 7000L, this.princess1, null);
      }
      else
      {
        this.princess1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess1_3);
        this.princess1.setRunning();
        startQuestTimer("trasa_3", 7000L, this.princess1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_3")) && (this.princess1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_3", 7000L, this.princess1, null);
      }
      else
      {
        this.princess1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess1_4);
        this.princess1.setRunning();
        startQuestTimer("trasa_4", 7000L, this.princess1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_4")) && (this.princess1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_4", 7000L, this.princess1, null);
      }
      else
      {
        this.princess1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess1_5);
        this.princess1.setRunning();
        startQuestTimer("trasa_5", 7000L, this.princess1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_5")) && (this.princess1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_5", 7000L, this.princess1, null);
      }
      else
      {
        this.princess1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess1_6);
        this.princess1.setRunning();
        startQuestTimer("trasa_6", 7000L, this.princess1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_6")) && (this.princess1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_6", 7000L, this.princess1, null);
      }
      else
      {
        this.princess1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess1_7);
        this.princess1.setRunning();
        startQuestTimer("trasa_7", 7000L, this.princess1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_7")) && (this.princess1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_7", 7000L, this.princess1, null);
      }
      else
      {
        this.princess1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess1_8);
        this.princess1.setRunning();
        startQuestTimer("trasa_8", 7000L, this.princess1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_8")) && (this.princess1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_8", 7000L, this.princess1, null);
      }
      else
      {
        this.princess1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess1_9);
        this.princess1.setRunning();
        startQuestTimer("trasa_9", 7000L, this.princess1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_9")) && (this.princess1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_9", 7000L, this.princess1, null);
      }
      else
      {
        this.princess1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess1_10);
        this.princess1.setRunning();
        startQuestTimer("trasa_10", 7000L, this.princess1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_10")) && (this.princess1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_10", 7000L, this.princess1, null);
      }
      else
      {
        this.princess1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess1_11);
        this.princess1.setRunning();
        startQuestTimer("trasa_11", 7000L, this.princess1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_11")) && (this.princess1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_11", 7000L, this.princess1, null);
      }
      else
      {
        this.princess1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess1_12);
        this.princess1.setRunning();
        startQuestTimer("trasa_nova", 7000L, this.princess1, null);
      }
    }
    else if ((event.equalsIgnoreCase("princess2_spawn")) && (this.princess2 == null))
    {
      this.princess2 = addSpawn(PRINCESS, 149144, 114033, -3725, 0, false, 0L);
      this.princess2.setIsNoRndWalk(true);
      this.princess2.setRunning();
      startQuestTimer("trasa_12", 7000L, this.princess2, null);
    }
    else if ((event.equalsIgnoreCase("trasa_nova2")) && (this.princess2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_nova2", 7000L, this.princess2, null);
      }
      else
      {
        this.princess2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess2_1);
        this.princess2.setRunning();
        startQuestTimer("trasa_12", 7000L, this.princess2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_12")) && (this.princess2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_12", 7000L, this.princess2, null);
      }
      else
      {
        if (!this.princess2.isInsideRadius(princess2_1.x, princess2_1.y, princess2_1.z, 100, true, false))
          this.princess2.teleToLocation(princess2_1.x, princess2_1.y, princess2_1.z);
        this.princess2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess2_2);
        this.princess2.setRunning();
        startQuestTimer("trasa_13", 7000L, this.princess2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_13")) && (this.princess2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_13", 7000L, this.princess2, null);
      }
      else
      {
        this.princess2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess2_3);
        this.princess2.setRunning();
        startQuestTimer("trasa_14", 7000L, this.princess2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_14")) && (this.princess2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_14", 7000L, this.princess2, null);
      }
      else
      {
        this.princess2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess2_4);
        this.princess2.setRunning();
        startQuestTimer("trasa_15", 7000L, this.princess2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_15")) && (this.princess2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_15", 7000L, this.princess2, null);
      }
      else
      {
        this.princess2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess2_5);
        this.princess2.setRunning();
        startQuestTimer("trasa_16", 7000L, this.princess2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_16")) && (this.princess2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_16", 7000L, this.princess2, null);
      }
      else
      {
        this.princess2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess2_6);
        this.princess2.setRunning();
        startQuestTimer("trasa_17", 7000L, this.princess2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_17")) && (this.princess2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_17", 7000L, this.princess2, null);
      }
      else
      {
        this.princess2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess2_7);
        this.princess2.setRunning();
        startQuestTimer("trasa_18", 7000L, this.princess2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_18")) && (this.princess2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_18", 7000L, this.princess2, null);
      }
      else
      {
        this.princess2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess2_8);
        this.princess2.setRunning();
        startQuestTimer("trasa_19", 7000L, this.princess2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_19")) && (this.princess2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_19", 7000L, this.princess2, null);
      }
      else
      {
        this.princess2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess2_9);
        this.princess2.setRunning();
        startQuestTimer("trasa_20", 7000L, this.princess2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_20")) && (this.princess2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_20", 7000L, this.princess2, null);
      }
      else
      {
        this.princess2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess2_10);
        this.princess2.setRunning();
        startQuestTimer("trasa_21", 7000L, this.princess2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_21")) && (this.princess2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_21", 7000L, this.princess2, null);
      }
      else
      {
        this.princess2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess2_11);
        this.princess2.setRunning();
        startQuestTimer("trasa_22", 7000L, this.princess2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_22")) && (this.princess2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_22", 7000L, this.princess2, null);
      }
      else
      {
        this.princess2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess2_12);
        this.princess2.setRunning();
        startQuestTimer("trasa_nova2", 7000L, this.princess2, null);
      }
    }
    else if ((event.equalsIgnoreCase("princess3_spawn")) && (this.princess3 == null))
    {
      this.princess3 = addSpawn(PRINCESS, 145571, 115830, -3725, 0, false, 0L);
      this.princess3.setIsNoRndWalk(true);
      this.princess3.setRunning();
      startQuestTimer("trasa_23", 7000L, this.princess3, null);
    }
    else if ((event.equalsIgnoreCase("trasa_nova3")) && (this.princess3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_nova3", 7000L, this.princess3, null);
      }
      else
      {
        this.princess3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess3_1);
        this.princess3.setRunning();
        startQuestTimer("trasa_23", 7000L, this.princess3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_23")) && (this.princess3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_23", 7000L, this.princess3, null);
      }
      else
      {
        if (!this.princess3.isInsideRadius(princess3_1.x, princess3_1.y, princess3_1.z, 100, true, false))
          this.princess3.teleToLocation(princess3_1.x, princess3_1.y, princess3_1.z);
        this.princess3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess3_2);
        this.princess3.setRunning();
        startQuestTimer("trasa_24", 7000L, this.princess3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_24")) && (this.princess3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_24", 7000L, this.princess3, null);
      }
      else
      {
        this.princess3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess3_3);
        this.princess3.setRunning();
        startQuestTimer("trasa_25", 7000L, this.princess3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_25")) && (this.princess3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_25", 7000L, this.princess3, null);
      }
      else
      {
        this.princess3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess3_4);
        this.princess3.setRunning();
        startQuestTimer("trasa_26", 7000L, this.princess3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_26")) && (this.princess3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_26", 7000L, this.princess3, null);
      }
      else
      {
        this.princess3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess3_5);
        this.princess3.setRunning();
        startQuestTimer("trasa_27", 7000L, this.princess3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_27")) && (this.princess3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_27", 7000L, this.princess3, null);
      }
      else
      {
        this.princess3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess3_6);
        this.princess3.setRunning();
        startQuestTimer("trasa_28", 7000L, this.princess3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_28")) && (this.princess3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_28", 7000L, this.princess3, null);
      }
      else
      {
        this.princess3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess3_7);
        this.princess3.setRunning();
        startQuestTimer("trasa_29", 7000L, this.princess3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_29")) && (this.princess3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_29", 7000L, this.princess3, null);
      }
      else
      {
        this.princess3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess3_8);
        this.princess3.setRunning();
        startQuestTimer("trasa_30", 7000L, this.princess3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_30")) && (this.princess3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_30", 7000L, this.princess3, null);
      }
      else
      {
        this.princess3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess3_9);
        this.princess3.setRunning();
        startQuestTimer("trasa_31", 7000L, this.princess3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_31")) && (this.princess3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_31", 7000L, this.princess3, null);
      }
      else
      {
        this.princess3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess3_10);
        this.princess3.setRunning();
        startQuestTimer("trasa_32", 7000L, this.princess3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_32")) && (this.princess3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_32", 7000L, this.princess3, null);
      }
      else
      {
        this.princess3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess3_11);
        this.princess3.setRunning();
        startQuestTimer("trasa_33", 7000L, this.princess3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_33")) && (this.princess3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_33", 7000L, this.princess3, null);
      }
      else
      {
        this.princess3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess3_12);
        this.princess3.setRunning();
        startQuestTimer("trasa_nova3", 7000L, this.princess3, null);
      }
    }
    else if ((event.equalsIgnoreCase("princess4_spawn")) && (this.princess4 == null))
    {
      this.princess4 = addSpawn(PRINCESS, 144868, 114504, -3725, 0, false, 0L);
      this.princess4.setIsNoRndWalk(true);
      this.princess4.setRunning();
      startQuestTimer("trasa_34", 7000L, this.princess4, null);
    }
    else if ((event.equalsIgnoreCase("trasa_nova4")) && (this.princess4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_nova4", 7000L, this.princess4, null);
      }
      else
      {
        this.princess4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess4_1);
        this.princess4.setRunning();
        startQuestTimer("trasa_34", 7000L, this.princess4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_34")) && (this.princess4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_34", 7000L, this.princess4, null);
      }
      else
      {
        if (!this.princess4.isInsideRadius(princess4_1.x, princess4_1.y, princess4_1.z, 100, true, false))
          this.princess4.teleToLocation(princess4_1.x, princess4_1.y, princess4_1.z);
        this.princess4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess4_2);
        this.princess4.setRunning();
        startQuestTimer("trasa_35", 7000L, this.princess4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_35")) && (this.princess4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_35", 7000L, this.princess4, null);
      }
      else
      {
        this.princess4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess4_3);
        this.princess4.setRunning();
        startQuestTimer("trasa_36", 7000L, this.princess4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_36")) && (this.princess4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_36", 7000L, this.princess4, null);
      }
      else
      {
        this.princess4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess4_4);
        this.princess4.setRunning();
        startQuestTimer("trasa_37", 7000L, this.princess4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_37")) && (this.princess4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_37", 7000L, this.princess4, null);
      }
      else
      {
        this.princess4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess4_5);
        this.princess4.setRunning();
        startQuestTimer("trasa_38", 7000L, this.princess4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_38")) && (this.princess4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_38", 7000L, this.princess4, null);
      }
      else
      {
        this.princess4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess4_6);
        this.princess4.setRunning();
        startQuestTimer("trasa_39", 7000L, this.princess4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_39")) && (this.princess4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_39", 7000L, this.princess4, null);
      }
      else
      {
        this.princess4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess4_7);
        this.princess4.setRunning();
        startQuestTimer("trasa_40", 7000L, this.princess4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_40")) && (this.princess4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_40", 7000L, this.princess4, null);
      }
      else
      {
        this.princess4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess4_8);
        this.princess4.setRunning();
        startQuestTimer("trasa_41", 7000L, this.princess4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_41")) && (this.princess4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_41", 7000L, this.princess4, null);
      }
      else
      {
        this.princess4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess4_9);
        this.princess4.setRunning();
        startQuestTimer("trasa_42", 7000L, this.princess4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_42")) && (this.princess4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_42", 7000L, this.princess4, null);
      }
      else
      {
        this.princess4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess4_10);
        this.princess4.setRunning();
        startQuestTimer("trasa_43", 7000L, this.princess4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_43")) && (this.princess4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_43", 7000L, this.princess4, null);
      }
      else
      {
        this.princess4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess4_11);
        this.princess4.setRunning();
        startQuestTimer("trasa_44", 7000L, this.princess4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_44")) && (this.princess4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_44", 7000L, this.princess4, null);
      }
      else
      {
        this.princess4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess4_12);
        this.princess4.setRunning();
        startQuestTimer("trasa_nova4", 7000L, this.princess4, null);
      }
    }
    else if ((event.equalsIgnoreCase("princess5_spawn")) && (this.princess5 == null))
    {
      this.princess5 = addSpawn(PRINCESS, 148011, 112145, -3725, 0, false, 0L);
      this.princess5.setIsNoRndWalk(true);
      this.princess5.setRunning();
      startQuestTimer("trasa_45", 7000L, this.princess5, null);
    }
    else if ((event.equalsIgnoreCase("trasa_nova5")) && (this.princess5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_nova5", 7000L, this.princess5, null);
      }
      else
      {
        this.princess5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess5_1);
        this.princess5.setRunning();
        startQuestTimer("trasa_45", 7000L, this.princess5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_45")) && (this.princess5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_45", 7000L, this.princess5, null);
      }
      else
      {
        if (!this.princess5.isInsideRadius(princess5_1.x, princess5_1.y, princess5_1.z, 100, true, false))
          this.princess5.teleToLocation(princess5_1.x, princess5_1.y, princess5_1.z);
        this.princess5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess5_2);
        this.princess5.setRunning();
        startQuestTimer("trasa_46", 7000L, this.princess5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_46")) && (this.princess5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_46", 7000L, this.princess5, null);
      }
      else
      {
        this.princess5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess5_3);
        this.princess5.setRunning();
        startQuestTimer("trasa_47", 7000L, this.princess5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_47")) && (this.princess5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_47", 7000L, this.princess5, null);
      }
      else
      {
        this.princess5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess5_4);
        this.princess5.setRunning();
        startQuestTimer("trasa_48", 7000L, this.princess5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_48")) && (this.princess5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_48", 7000L, this.princess5, null);
      }
      else
      {
        this.princess5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess5_5);
        this.princess5.setRunning();
        startQuestTimer("trasa_49", 7000L, this.princess5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_49")) && (this.princess5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_49", 7000L, this.princess5, null);
      }
      else
      {
        this.princess5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess5_6);
        this.princess5.setRunning();
        startQuestTimer("trasa_50", 7000L, this.princess5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_50")) && (this.princess5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_50", 7000L, this.princess5, null);
      }
      else
      {
        this.princess5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess5_7);
        this.princess5.setRunning();
        startQuestTimer("trasa_51", 7000L, this.princess5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_51")) && (this.princess5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_51", 7000L, this.princess5, null);
      }
      else
      {
        this.princess5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess5_8);
        this.princess5.setRunning();
        startQuestTimer("trasa_52", 7000L, this.princess5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_52")) && (this.princess5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_52", 7000L, this.princess5, null);
      }
      else
      {
        this.princess5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess5_9);
        this.princess5.setRunning();
        startQuestTimer("trasa_53", 7000L, this.princess5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_53")) && (this.princess5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_53", 7000L, this.princess5, null);
      }
      else
      {
        this.princess5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess5_10);
        this.princess5.setRunning();
        startQuestTimer("trasa_54", 7000L, this.princess5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_54")) && (this.princess5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_54", 7000L, this.princess5, null);
      }
      else
      {
        this.princess5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess5_11);
        this.princess5.setRunning();
        startQuestTimer("trasa_55", 7000L, this.princess5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_55")) && (this.princess5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_55", 7000L, this.princess5, null);
      }
      else
      {
        this.princess5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess5_12);
        this.princess5.setRunning();
        startQuestTimer("trasa_nova5", 7000L, this.princess5, null);
      }
    }
    else if ((event.equalsIgnoreCase("princess6_spawn")) && (this.princess6 == null))
    {
      this.princess6 = addSpawn(PRINCESS, 148314, 115972, -3725, 0, false, 0L);
      this.princess6.setIsNoRndWalk(true);
      this.princess6.setRunning();
      startQuestTimer("trasa_56", 7000L, this.princess6, null);
    }
    else if ((event.equalsIgnoreCase("trasa_nova6")) && (this.princess6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_nova6", 7000L, this.princess6, null);
      }
      else
      {
        this.princess6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess6_1);
        this.princess6.setRunning();
        startQuestTimer("trasa_56", 7000L, this.princess6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_56")) && (this.princess6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_56", 7000L, this.princess6, null);
      }
      else
      {
        if (!this.princess6.isInsideRadius(princess6_1.x, princess6_1.y, princess6_1.z, 100, true, false))
          this.princess6.teleToLocation(princess6_1.x, princess6_1.y, princess6_1.z);
        this.princess6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess6_2);
        this.princess6.setRunning();
        startQuestTimer("trasa_57", 7000L, this.princess6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_57")) && (this.princess6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_57", 7000L, this.princess6, null);
      }
      else
      {
        this.princess6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess6_3);
        this.princess6.setRunning();
        startQuestTimer("trasa_58", 7000L, this.princess6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_58")) && (this.princess6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_58", 7000L, this.princess6, null);
      }
      else
      {
        this.princess6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess6_4);
        this.princess6.setRunning();
        startQuestTimer("trasa_59", 7000L, this.princess6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_59")) && (this.princess6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_59", 7000L, this.princess6, null);
      }
      else
      {
        this.princess6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess6_5);
        this.princess6.setRunning();
        startQuestTimer("trasa_60", 7000L, this.princess6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_60")) && (this.princess6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_60", 7000L, this.princess6, null);
      }
      else
      {
        this.princess6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess6_6);
        this.princess6.setRunning();
        startQuestTimer("trasa_61", 7000L, this.princess6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_61")) && (this.princess6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_61", 7000L, this.princess6, null);
      }
      else
      {
        this.princess6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess6_7);
        this.princess6.setRunning();
        startQuestTimer("trasa_62", 7000L, this.princess6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_62")) && (this.princess6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_62", 7000L, this.princess6, null);
      }
      else
      {
        this.princess6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess6_8);
        this.princess6.setRunning();
        startQuestTimer("trasa_63", 7000L, this.princess6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_63")) && (this.princess6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_63", 7000L, this.princess6, null);
      }
      else
      {
        this.princess6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess6_9);
        this.princess6.setRunning();
        startQuestTimer("trasa_64", 7000L, this.princess6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_64")) && (this.princess6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_64", 7000L, this.princess6, null);
      }
      else
      {
        this.princess6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess6_10);
        this.princess6.setRunning();
        startQuestTimer("trasa_65", 7000L, this.princess6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_65")) && (this.princess6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_65", 7000L, this.princess6, null);
      }
      else
      {
        this.princess6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess6_11);
        this.princess6.setRunning();
        startQuestTimer("trasa_66", 7000L, this.princess6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_66")) && (this.princess6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_66", 7000L, this.princess6, null);
      }
      else
      {
        this.princess6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess6_12);
        this.princess6.setRunning();
        startQuestTimer("trasa_nova6", 7000L, this.princess6, null);
      }
    }
    return super.onAdvEvent(event, npc, player);
  }

  @Override
public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
  {
    if (npc == null) {
      return super.onKill(npc, killer, isPet);
    }
    if (npc.getNpcId() == PRINCESS)
    {
      if (npc == this.princess1)
      {
        startQuestTimer("princess1_spawn", 300000L, null, null);
        this.princess1 = null;
      }

      if (npc == this.princess2)
      {
        startQuestTimer("princess2_spawn", 300000L, null, null);
        this.princess2 = null;
      }

      if (npc == this.princess3)
      {
        startQuestTimer("princess3_spawn", 300000L, null, null);
        this.princess3 = null;
      }

      if (npc == this.princess4)
      {
        startQuestTimer("princess4_spawn", 300000L, null, null);
        this.princess4 = null;
      }

      if (npc == this.princess5)
      {
        startQuestTimer("princess5_spawn", 300000L, null, null);
        this.princess5 = null;
      }

      if (npc == this.princess6)
      {
        startQuestTimer("princess6_spawn", 300000L, null, null);
        this.princess6 = null;
      }
    }
    return super.onKill(npc, killer, isPet);
  }

  @Override
public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
  {
    if ((!npc.isCastingNow()) && (!npc.isAttackingNow()) && (!npc.isInCombat()) && (!player.isDead()))
    {
      ((L2Attackable)npc).addDamageHate(player, 0, 999);
      ((L2Attackable)npc).getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
		((L2Attackable) npc).canSeeThroughSilentMove();

    }
    return super.onAggroRangeEnter(npc, player, isPet);
  }

  public static void main(String[] args)
  {
    new DrakeMage(-1, "DrakeMage", "ai");
  }
}