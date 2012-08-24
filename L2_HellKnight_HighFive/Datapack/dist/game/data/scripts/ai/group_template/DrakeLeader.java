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

public class DrakeLeader extends L2AttackableAIScript
{
  private static final int LEADER = 22848;
  private L2Npc leader1;
  private L2Npc leader2;
  private L2Npc leader3;
  private L2Npc leader4;
  private L2Npc leader5;
  private L2Npc leader6;
  private static final L2CharPosition leader1_1 = new L2CharPosition(145452, 115611, -3725, 0);
  private static final L2CharPosition leader1_2 = new L2CharPosition(145109, 114475, -3725, 0);
  private static final L2CharPosition leader1_3 = new L2CharPosition(145597, 112768, -3725, 0);
  private static final L2CharPosition leader1_4 = new L2CharPosition(146762, 112800, -3725, 0);
  private static final L2CharPosition leader1_5 = new L2CharPosition(148644, 112478, -3725, 0);
  private static final L2CharPosition leader1_6 = new L2CharPosition(148950, 113645, -3725, 0);
  private static final L2CharPosition leader1_7 = new L2CharPosition(149138, 114907, -3725, 0);
  private static final L2CharPosition leader1_8 = new L2CharPosition(148422, 115838, -3725, 0);
  private static final L2CharPosition leader1_9 = new L2CharPosition(147331, 116621, -3704, 0);
  private static final L2CharPosition leader1_10 = new L2CharPosition(146355, 116327, -3725, 0);
  private static final L2CharPosition leader1_11 = new L2CharPosition(146096, 115898, -3725, 0);

  private static final L2CharPosition leader2_1 = new L2CharPosition(148644, 112478, -3725, 0);
  private static final L2CharPosition leader2_2 = new L2CharPosition(148950, 113645, -3725, 0);
  private static final L2CharPosition leader2_3 = new L2CharPosition(149138, 114907, -3725, 0);
  private static final L2CharPosition leader2_4 = new L2CharPosition(148422, 115838, -3725, 0);
  private static final L2CharPosition leader2_5 = new L2CharPosition(147331, 116621, -3704, 0);
  private static final L2CharPosition leader2_6 = new L2CharPosition(146355, 116327, -3725, 0);
  private static final L2CharPosition leader2_7 = new L2CharPosition(146096, 115898, -3725, 0);
  private static final L2CharPosition leader2_8 = new L2CharPosition(145452, 115611, -3725, 0);
  private static final L2CharPosition leader2_9 = new L2CharPosition(145109, 114475, -3725, 0);
  private static final L2CharPosition leader2_10 = new L2CharPosition(145597, 112768, -3725, 0);
  private static final L2CharPosition leader2_11 = new L2CharPosition(146762, 112800, -3725, 0);

  private static final L2CharPosition leader3_1 = new L2CharPosition(147331, 116621, -3704, 0);
  private static final L2CharPosition leader3_2 = new L2CharPosition(146355, 116327, -3725, 0);
  private static final L2CharPosition leader3_3 = new L2CharPosition(146096, 115898, -3725, 0);
  private static final L2CharPosition leader3_4 = new L2CharPosition(145452, 115611, -3725, 0);
  private static final L2CharPosition leader3_5 = new L2CharPosition(145109, 114475, -3725, 0);
  private static final L2CharPosition leader3_6 = new L2CharPosition(145597, 112768, -3725, 0);
  private static final L2CharPosition leader3_7 = new L2CharPosition(146762, 112800, -3725, 0);
  private static final L2CharPosition leader3_8 = new L2CharPosition(148644, 112478, -3725, 0);
  private static final L2CharPosition leader3_9 = new L2CharPosition(148950, 113645, -3725, 0);
  private static final L2CharPosition leader3_10 = new L2CharPosition(149138, 114907, -3725, 0);
  private static final L2CharPosition leader3_11 = new L2CharPosition(148422, 115838, -3725, 0);

  private static final L2CharPosition leader4_1 = new L2CharPosition(148579, 111993, -3710, 0);
  private static final L2CharPosition leader4_2 = new L2CharPosition(148931, 113094, -3725, 0);
  private static final L2CharPosition leader4_3 = new L2CharPosition(149276, 114105, -3725, 0);
  private static final L2CharPosition leader4_4 = new L2CharPosition(148909, 115547, -3725, 0);
  private static final L2CharPosition leader4_5 = new L2CharPosition(147495, 116305, -3711, 0);
  private static final L2CharPosition leader4_6 = new L2CharPosition(146110, 116156, -3725, 0);
  private static final L2CharPosition leader4_7 = new L2CharPosition(145191, 114990, -3725, 0);
  private static final L2CharPosition leader4_8 = new L2CharPosition(145025, 113670, -3725, 0);
  private static final L2CharPosition leader4_9 = new L2CharPosition(145868, 112717, -3725, 0);
  private static final L2CharPosition leader4_10 = new L2CharPosition(146762, 112951, -3725, 0);
  private static final L2CharPosition leader4_11 = new L2CharPosition(147641, 112101, -3725, 0);

  private static final L2CharPosition leader5_1 = new L2CharPosition(147495, 116305, -3711, 0);
  private static final L2CharPosition leader5_2 = new L2CharPosition(146110, 116156, -3725, 0);
  private static final L2CharPosition leader5_3 = new L2CharPosition(145191, 114990, -3725, 0);
  private static final L2CharPosition leader5_4 = new L2CharPosition(145025, 113670, -3725, 0);
  private static final L2CharPosition leader5_5 = new L2CharPosition(145868, 112717, -3725, 0);
  private static final L2CharPosition leader5_6 = new L2CharPosition(146762, 112951, -3725, 0);
  private static final L2CharPosition leader5_7 = new L2CharPosition(147641, 112101, -3725, 0);
  private static final L2CharPosition leader5_8 = new L2CharPosition(148579, 111993, -3710, 0);
  private static final L2CharPosition leader5_9 = new L2CharPosition(148931, 113094, -3725, 0);
  private static final L2CharPosition leader5_10 = new L2CharPosition(149276, 114105, -3725, 0);
  private static final L2CharPosition leader5_11 = new L2CharPosition(148909, 115547, -3725, 0);

  private static final L2CharPosition leader6_1 = new L2CharPosition(145868, 112717, -3725, 0);
  private static final L2CharPosition leader6_2 = new L2CharPosition(146762, 112951, -3725, 0);
  private static final L2CharPosition leader6_3 = new L2CharPosition(147641, 112101, -3725, 0);
  private static final L2CharPosition leader6_4 = new L2CharPosition(148579, 111993, -3710, 0);
  private static final L2CharPosition leader6_5 = new L2CharPosition(148931, 113094, -3725, 0);
  private static final L2CharPosition leader6_6 = new L2CharPosition(149276, 114105, -3725, 0);
  private static final L2CharPosition leader6_7 = new L2CharPosition(148909, 115547, -3725, 0);
  private static final L2CharPosition leader6_8 = new L2CharPosition(147495, 116305, -3711, 0);
  private static final L2CharPosition leader6_9 = new L2CharPosition(146110, 116156, -3725, 0);
  private static final L2CharPosition leader6_10 = new L2CharPosition(145191, 114990, -3725, 0);
  private static final L2CharPosition leader6_11 = new L2CharPosition(145025, 113670, -3725, 0);

  public DrakeLeader(int questId, String name, String descr)
  {
    super(questId, name, descr);

    addKillId(LEADER);
    addAggroRangeEnterId(LEADER);

    startQuestTimer("leader1_spawn", 1800L, null, null);
    startQuestTimer("leader2_spawn", 1800L, null, null);
    startQuestTimer("leader3_spawn", 1800L, null, null);
    startQuestTimer("leader4_spawn", 1800L, null, null);
    startQuestTimer("leader5_spawn", 1800L, null, null);
    startQuestTimer("leader6_spawn", 1800L, null, null);
  }

  @Override
public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
  {
    if ((event.equalsIgnoreCase("leader1_spawn")) && (this.leader1 == null))
    {
      this.leader1 = addSpawn(LEADER, 145452, 115611, -3725, 0, false, 0L);
      this.leader1.setIsNoRndWalk(true);
      this.leader1.setRunning();
      startQuestTimer("trasa_1", 9000L, this.leader1, null);
    }
    else if ((event.equalsIgnoreCase("trasa_nova")) && (this.leader1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_nova", 9000L, this.leader1, null);
      }
      else
      {
        this.leader1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader1_1);
        this.leader1.setRunning();
        startQuestTimer("trasa_1", 9000L, this.leader1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_1")) && (this.leader1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_1", 9000L, this.leader1, null);
      }
      else
      {
        if (!this.leader1.isInsideRadius(leader1_1.x, leader1_1.y, leader1_1.z, 100, true, false))
          this.leader1.teleToLocation(leader1_1.x, leader1_1.y, leader1_1.z);
        this.leader1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader1_2);
        this.leader1.setRunning();
        startQuestTimer("trasa_2", 9000L, this.leader1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_2")) && (this.leader1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_2", 9000L, this.leader1, null);
      }
      else
      {
        this.leader1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader1_3);
        this.leader1.setRunning();
        startQuestTimer("trasa_3", 9000L, this.leader1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_3")) && (this.leader1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_3", 9000L, this.leader1, null);
      }
      else
      {
        this.leader1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader1_4);
        this.leader1.setRunning();
        startQuestTimer("trasa_4", 9000L, this.leader1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_4")) && (this.leader1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_4", 9000L, this.leader1, null);
      }
      else
      {
        this.leader1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader1_5);
        this.leader1.setRunning();
        startQuestTimer("trasa_5", 9000L, this.leader1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_5")) && (this.leader1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_5", 9000L, this.leader1, null);
      }
      else
      {
        this.leader1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader1_6);
        this.leader1.setRunning();
        startQuestTimer("trasa_6", 9000L, this.leader1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_6")) && (this.leader1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_6", 9000L, this.leader1, null);
      }
      else
      {
        this.leader1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader1_7);
        this.leader1.setRunning();
        startQuestTimer("trasa_7", 9000L, this.leader1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_7")) && (this.leader1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_7", 9000L, this.leader1, null);
      }
      else
      {
        this.leader1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader1_8);
        this.leader1.setRunning();
        startQuestTimer("trasa_8", 9000L, this.leader1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_8")) && (this.leader1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_8", 9000L, this.leader1, null);
      }
      else
      {
        this.leader1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader1_9);
        this.leader1.setRunning();
        startQuestTimer("trasa_9", 9000L, this.leader1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_9")) && (this.leader1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_9", 9000L, this.leader1, null);
      }
      else
      {
        this.leader1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader1_10);
        this.leader1.setRunning();
        startQuestTimer("trasa_10", 9000L, this.leader1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_10")) && (this.leader1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_10", 9000L, this.leader1, null);
      }
      else
      {
        this.leader1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader1_11);
        this.leader1.setRunning();
        startQuestTimer("trasa_nova", 4000L, this.leader1, null);
      }
    }
    else if ((event.equalsIgnoreCase("leader2_spawn")) && (this.leader2 == null))
    {
      this.leader2 = addSpawn(LEADER, 148644, 112478, -3725, 0, false, 0L);
      this.leader2.setIsNoRndWalk(true);
      this.leader2.setRunning();
      startQuestTimer("trasa_1", 9000L, this.leader2, null);
    }
    else if ((event.equalsIgnoreCase("trasa_nova2")) && (this.leader2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_nova2", 9000L, this.leader2, null);
      }
      else
      {
        this.leader2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader2_1);
        this.leader2.setRunning();
        startQuestTimer("trasa_11", 9000L, this.leader2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_11")) && (this.leader2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_11", 9000L, this.leader2, null);
      }
      else
      {
        if (!this.leader2.isInsideRadius(leader2_1.x, leader2_1.y, leader2_1.z, 100, true, false))
          this.leader2.teleToLocation(leader2_1.x, leader2_1.y, leader2_1.z);
        this.leader2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader2_2);
        this.leader2.setRunning();
        startQuestTimer("trasa_12", 9000L, this.leader2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_12")) && (this.leader2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_12", 9000L, this.leader2, null);
      }
      else
      {
        this.leader2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader2_3);
        this.leader2.setRunning();
        startQuestTimer("trasa_13", 9000L, this.leader2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_13")) && (this.leader2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_13", 9000L, this.leader2, null);
      }
      else
      {
        this.leader2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader2_4);
        this.leader2.setRunning();
        startQuestTimer("trasa_14", 9000L, this.leader2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_14")) && (this.leader2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_14", 9000L, this.leader2, null);
      }
      else
      {
        this.leader2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader2_5);
        this.leader2.setRunning();
        startQuestTimer("trasa_15", 9000L, this.leader2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_15")) && (this.leader2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_15", 9000L, this.leader2, null);
      }
      else
      {
        this.leader2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader2_6);
        this.leader2.setRunning();
        startQuestTimer("trasa_16", 9000L, this.leader2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_16")) && (this.leader2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_16", 9000L, this.leader2, null);
      }
      else
      {
        this.leader2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader2_7);
        this.leader2.setRunning();
        startQuestTimer("trasa_17", 9000L, this.leader2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_17")) && (this.leader2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_17", 9000L, this.leader2, null);
      }
      else
      {
        this.leader2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader2_8);
        this.leader2.setRunning();
        startQuestTimer("trasa_18", 9000L, this.leader2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_18")) && (this.leader2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_18", 9000L, this.leader2, null);
      }
      else
      {
        this.leader2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader2_9);
        this.leader2.setRunning();
        startQuestTimer("trasa_19", 9000L, this.leader2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_19")) && (this.leader2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_19", 9000L, this.leader2, null);
      }
      else
      {
        this.leader2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader2_10);
        this.leader2.setRunning();
        startQuestTimer("trasa_20", 9000L, this.leader2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_20")) && (this.leader2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_20", 9000L, this.leader2, null);
      }
      else
      {
        this.leader2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader2_11);
        this.leader2.setRunning();
        startQuestTimer("trasa_nova2", 4000L, this.leader2, null);
      }
    }
    else if ((event.equalsIgnoreCase("leader3_spawn")) && (this.leader3 == null))
    {
      this.leader3 = addSpawn(LEADER, 147331, 116621, -3704, 0, false, 0L);
      this.leader3.setIsNoRndWalk(true);
      this.leader3.setRunning();
      startQuestTimer("trasa_21", 9000L, this.leader3, null);
    }
    else if ((event.equalsIgnoreCase("trasa_nova3")) && (this.leader3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_nova3", 9000L, this.leader3, null);
      }
      else
      {
        this.leader3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader3_1);
        this.leader3.setRunning();
        startQuestTimer("trasa_21", 9000L, this.leader3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_21")) && (this.leader3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_21", 9000L, this.leader3, null);
      }
      else
      {
        if (!this.leader3.isInsideRadius(leader3_1.x, leader3_1.y, leader3_1.z, 100, true, false))
          this.leader3.teleToLocation(leader3_1.x, leader3_1.y, leader3_1.z);
        this.leader3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader3_2);
        this.leader3.setRunning();
        startQuestTimer("trasa_22", 9000L, this.leader3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_22")) && (this.leader3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_22", 9000L, this.leader3, null);
      }
      else
      {
        this.leader3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader3_3);
        this.leader3.setRunning();
        startQuestTimer("trasa_23", 9000L, this.leader3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_23")) && (this.leader3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_23", 9000L, this.leader3, null);
      }
      else
      {
        this.leader3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader3_4);
        this.leader3.setRunning();
        startQuestTimer("trasa_24", 9000L, this.leader3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_24")) && (this.leader3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_24", 9000L, this.leader3, null);
      }
      else
      {
        this.leader3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader3_5);
        this.leader3.setRunning();
        startQuestTimer("trasa_25", 9000L, this.leader3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_25")) && (this.leader3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_25", 9000L, this.leader3, null);
      }
      else
      {
        this.leader3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader3_6);
        this.leader3.setRunning();
        startQuestTimer("trasa_26", 9000L, this.leader3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_26")) && (this.leader3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_26", 9000L, this.leader3, null);
      }
      else
      {
        this.leader3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader3_7);
        this.leader3.setRunning();
        startQuestTimer("trasa_27", 9000L, this.leader3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_27")) && (this.leader3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_27", 9000L, this.leader3, null);
      }
      else
      {
        this.leader3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader3_8);
        this.leader3.setRunning();
        startQuestTimer("trasa_28", 9000L, this.leader3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_28")) && (this.leader3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_28", 9000L, this.leader3, null);
      }
      else
      {
        this.leader3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader3_9);
        this.leader3.setRunning();
        startQuestTimer("trasa_29", 9000L, this.leader3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_29")) && (this.leader3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_29", 9000L, this.leader3, null);
      }
      else
      {
        this.leader3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader3_10);
        this.leader3.setRunning();
        startQuestTimer("trasa_30", 9000L, this.leader3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_30")) && (this.leader3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_30", 9000L, this.leader3, null);
      }
      else
      {
        this.leader3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader3_11);
        this.leader3.setRunning();
        startQuestTimer("trasa_nova3", 4000L, this.leader3, null);
      }
    }
    else if ((event.equalsIgnoreCase("leader4_spawn")) && (this.leader4 == null))
    {
      this.leader4 = addSpawn(LEADER, 148579, 111993, -3710, 0, false, 0L);
      this.leader4.setIsNoRndWalk(true);
      this.leader4.setRunning();
      startQuestTimer("trasa_31", 9000L, this.leader4, null);
    }
    else if ((event.equalsIgnoreCase("trasa_nova4")) && (this.leader4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_nova4", 9000L, this.leader4, null);
      }
      else
      {
        this.leader4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader4_1);
        this.leader4.setRunning();
        startQuestTimer("trasa_31", 9000L, this.leader4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_31")) && (this.leader4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_31", 9000L, this.leader4, null);
      }
      else
      {
        if (!this.leader4.isInsideRadius(leader4_1.x, leader4_1.y, leader4_1.z, 100, true, false))
          this.leader4.teleToLocation(leader4_1.x, leader4_1.y, leader4_1.z);
        this.leader4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader4_2);
        this.leader4.setRunning();
        startQuestTimer("trasa_32", 9000L, this.leader4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_32")) && (this.leader4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_32", 9000L, this.leader4, null);
      }
      else
      {
        this.leader4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader4_3);
        this.leader4.setRunning();
        startQuestTimer("trasa_33", 9000L, this.leader4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_33")) && (this.leader4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_33", 9000L, this.leader4, null);
      }
      else
      {
        this.leader4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader4_4);
        this.leader4.setRunning();
        startQuestTimer("trasa_34", 9000L, this.leader4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_34")) && (this.leader4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_34", 9000L, this.leader4, null);
      }
      else
      {
        this.leader4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader4_5);
        this.leader4.setRunning();
        startQuestTimer("trasa_35", 9000L, this.leader4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_35")) && (this.leader4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_35", 9000L, this.leader4, null);
      }
      else
      {
        this.leader4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader4_6);
        this.leader4.setRunning();
        startQuestTimer("trasa_36", 9000L, this.leader4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_36")) && (this.leader4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_36", 9000L, this.leader4, null);
      }
      else
      {
        this.leader4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader4_7);
        this.leader4.setRunning();
        startQuestTimer("trasa_37", 9000L, this.leader4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_37")) && (this.leader4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_37", 9000L, this.leader4, null);
      }
      else
      {
        this.leader4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader4_8);
        this.leader4.setRunning();
        startQuestTimer("trasa_38", 9000L, this.leader4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_38")) && (this.leader4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_38", 9000L, this.leader4, null);
      }
      else
      {
        this.leader4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader4_9);
        this.leader4.setRunning();
        startQuestTimer("trasa_39", 9000L, this.leader4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_39")) && (this.leader4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_39", 9000L, this.leader4, null);
      }
      else
      {
        this.leader4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader4_10);
        this.leader4.setRunning();
        startQuestTimer("trasa_40", 9000L, this.leader4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_40")) && (this.leader4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_40", 9000L, this.leader4, null);
      }
      else
      {
        this.leader4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader4_11);
        this.leader4.setRunning();
        startQuestTimer("trasa_nova4", 4000L, this.leader4, null);
      }
    }
    else if ((event.equalsIgnoreCase("leader5_spawn")) && (this.leader5 == null))
    {
      this.leader5 = addSpawn(LEADER, 147495, 116305, -3711, 0, false, 0L);
      this.leader5.setIsNoRndWalk(true);
      this.leader5.setRunning();
      startQuestTimer("trasa_41", 9000L, this.leader5, null);
    }
    else if ((event.equalsIgnoreCase("trasa_nova5")) && (this.leader5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_nova5", 9000L, this.leader5, null);
      }
      else
      {
        this.leader5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader5_1);
        this.leader5.setRunning();
        startQuestTimer("trasa_41", 9000L, this.leader5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_41")) && (this.leader5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_41", 9000L, this.leader5, null);
      }
      else
      {
        if (!this.leader5.isInsideRadius(leader5_1.x, leader5_1.y, leader5_1.z, 100, true, false))
          this.leader5.teleToLocation(leader5_1.x, leader5_1.y, leader5_1.z);
        this.leader5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader5_2);
        this.leader5.setRunning();
        startQuestTimer("trasa_42", 9000L, this.leader5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_42")) && (this.leader5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_42", 9000L, this.leader5, null);
      }
      else
      {
        this.leader5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader5_3);
        this.leader5.setRunning();
        startQuestTimer("trasa_43", 9000L, this.leader5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_43")) && (this.leader5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_43", 9000L, this.leader5, null);
      }
      else
      {
        this.leader5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader5_4);
        this.leader5.setRunning();
        startQuestTimer("trasa_44", 9000L, this.leader5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_44")) && (this.leader5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_44", 9000L, this.leader5, null);
      }
      else
      {
        this.leader5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader5_5);
        this.leader5.setRunning();
        startQuestTimer("trasa_45", 9000L, this.leader5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_45")) && (this.leader5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_45", 9000L, this.leader5, null);
      }
      else
      {
        this.leader5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader5_6);
        this.leader5.setRunning();
        startQuestTimer("trasa_46", 9000L, this.leader5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_46")) && (this.leader5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_46", 9000L, this.leader5, null);
      }
      else
      {
        this.leader5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader5_7);
        this.leader5.setRunning();
        startQuestTimer("trasa_47", 9000L, this.leader5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_47")) && (this.leader5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_47", 9000L, this.leader5, null);
      }
      else
      {
        this.leader5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader5_8);
        this.leader5.setRunning();
        startQuestTimer("trasa_48", 9000L, this.leader5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_48")) && (this.leader5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_48", 9000L, this.leader5, null);
      }
      else
      {
        this.leader5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader5_9);
        this.leader5.setRunning();
        startQuestTimer("trasa_49", 9000L, this.leader5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_49")) && (this.leader5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_49", 9000L, this.leader5, null);
      }
      else
      {
        this.leader5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader5_10);
        this.leader5.setRunning();
        startQuestTimer("trasa_50", 9000L, this.leader5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_50")) && (this.leader5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_50", 9000L, this.leader5, null);
      }
      else
      {
        this.leader5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader5_11);
        this.leader5.setRunning();
        startQuestTimer("trasa_nova5", 4000L, this.leader5, null);
      }
    }
    else if ((event.equalsIgnoreCase("leader6_spawn")) && (this.leader6 == null))
    {
      this.leader6 = addSpawn(LEADER, 145868, 112717, -3725, 0, false, 0L);
      this.leader6.setIsNoRndWalk(true);
      this.leader6.setRunning();
      startQuestTimer("trasa_51", 9000L, this.leader6, null);
    }
    else if ((event.equalsIgnoreCase("trasa_nova6")) && (this.leader6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_nova6", 9000L, this.leader6, null);
      }
      else
      {
        this.leader6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader6_1);
        this.leader6.setRunning();
        startQuestTimer("trasa_51", 9000L, this.leader6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_51")) && (this.leader6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_51", 9000L, this.leader6, null);
      }
      else
      {
        if (!this.leader6.isInsideRadius(leader6_1.x, leader6_1.y, leader6_1.z, 100, true, false))
          this.leader6.teleToLocation(leader6_1.x, leader6_1.y, leader6_1.z);
        this.leader6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader6_2);
        this.leader6.setRunning();
        startQuestTimer("trasa_52", 9000L, this.leader6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_52")) && (this.leader6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_52", 9000L, this.leader6, null);
      }
      else
      {
        this.leader6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader6_3);
        this.leader6.setRunning();
        startQuestTimer("trasa_53", 9000L, this.leader6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_53")) && (this.leader6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_53", 9000L, this.leader6, null);
      }
      else
      {
        this.leader6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader6_4);
        this.leader6.setRunning();
        startQuestTimer("trasa_54", 9000L, this.leader6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_54")) && (this.leader6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_54", 9000L, this.leader6, null);
      }
      else
      {
        this.leader6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader6_5);
        this.leader6.setRunning();
        startQuestTimer("trasa_55", 9000L, this.leader6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_55")) && (this.leader6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_55", 9000L, this.leader6, null);
      }
      else
      {
        this.leader6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader6_6);
        this.leader6.setRunning();
        startQuestTimer("trasa_56", 9000L, this.leader6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_56")) && (this.leader6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_56", 9000L, this.leader6, null);
      }
      else
      {
        this.leader6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader6_7);
        this.leader6.setRunning();
        startQuestTimer("trasa_57", 9000L, this.leader6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_57")) && (this.leader6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_57", 9000L, this.leader6, null);
      }
      else
      {
        this.leader6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader6_8);
        this.leader6.setRunning();
        startQuestTimer("trasa_58", 9000L, this.leader6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_58")) && (this.leader6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_58", 9000L, this.leader6, null);
      }
      else
      {
        this.leader6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader6_9);
        this.leader6.setRunning();
        startQuestTimer("trasa_59", 9000L, this.leader6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_59")) && (this.leader6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_59", 9000L, this.leader6, null);
      }
      else
      {
        this.leader6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader6_10);
        this.leader6.setRunning();
        startQuestTimer("trasa_60", 9000L, this.leader6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_60")) && (this.leader6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_60", 9000L, this.leader6, null);
      }
      else
      {
        this.leader6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader6_11);
        this.leader6.setRunning();
        startQuestTimer("trasa_nova6", 4000L, this.leader6, null);
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
    if (npc.getNpcId() == LEADER)
    {
      if (npc == this.leader1)
      {
        startQuestTimer("leader1_spawn", 300000L, null, null);
        this.leader1 = null;
      }

      if (npc == this.leader2)
      {
        startQuestTimer("leader2_spawn", 300000L, null, null);
        this.leader2 = null;
      }

      if (npc == this.leader3)
      {
        startQuestTimer("leader3_spawn", 300000L, null, null);
        this.leader3 = null;
      }

      if (npc == this.leader4)
      {
        startQuestTimer("leader4_spawn", 300000L, null, null);
        this.leader4 = null;
      }

      if (npc == this.leader5)
      {
        startQuestTimer("leader5_spawn", 300000L, null, null);
        this.leader5 = null;
      }

      if (npc == this.leader6)
      {
        startQuestTimer("leader6_spawn", 300000L, null, null);
        this.leader6 = null;
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
    }
    return super.onAggroRangeEnter(npc, player, isPet);
  }

  public static void main(String[] args)
  {
    new DrakeLeader(-1, "DrakeLeader", "ai");
  }
}