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

public class DrakeScout extends L2AttackableAIScript
{
  private static final int HUNTER = 22849;
  private L2Npc hunter1;
  private L2Npc hunter2;
  private L2Npc hunter3;
  private L2Npc hunter4;
  private L2Npc hunter5;
  private L2Npc hunter6;
  private static final L2CharPosition hunter1_1 = new L2CharPosition(149292, 113881, -3725, 0);
  private static final L2CharPosition hunter1_2 = new L2CharPosition(148952, 115264, -3725, 0);
  private static final L2CharPosition hunter1_3 = new L2CharPosition(148087, 116152, -3725, 0);
  private static final L2CharPosition hunter1_4 = new L2CharPosition(147339, 116027, -3725, 0);
  private static final L2CharPosition hunter1_5 = new L2CharPosition(146439, 116247, -3725, 0);
  private static final L2CharPosition hunter1_6 = new L2CharPosition(145501, 115897, -3725, 0);
  private static final L2CharPosition hunter1_7 = new L2CharPosition(145121, 114863, -3725, 0);
  private static final L2CharPosition hunter1_8 = new L2CharPosition(145177, 113352, -3725, 0);
  private static final L2CharPosition hunter1_9 = new L2CharPosition(146074, 112690, -3725, 0);
  private static final L2CharPosition hunter1_10 = new L2CharPosition(147155, 112905, -3725, 0);
  private static final L2CharPosition hunter1_11 = new L2CharPosition(148134, 112679, -3725, 0);
  private static final L2CharPosition hunter1_12 = new L2CharPosition(148905, 112887, -3725, 0);

  private static final L2CharPosition hunter2_1 = new L2CharPosition(146439, 116247, -3725, 0);
  private static final L2CharPosition hunter2_2 = new L2CharPosition(145501, 115897, -3725, 0);
  private static final L2CharPosition hunter2_3 = new L2CharPosition(145121, 114863, -3725, 0);
  private static final L2CharPosition hunter2_4 = new L2CharPosition(145177, 113352, -3725, 0);
  private static final L2CharPosition hunter2_5 = new L2CharPosition(146074, 112690, -3725, 0);
  private static final L2CharPosition hunter2_6 = new L2CharPosition(147155, 112905, -3725, 0);
  private static final L2CharPosition hunter2_7 = new L2CharPosition(148134, 112679, -3725, 0);
  private static final L2CharPosition hunter2_8 = new L2CharPosition(148905, 112887, -3725, 0);
  private static final L2CharPosition hunter2_9 = new L2CharPosition(149292, 113881, -3725, 0);
  private static final L2CharPosition hunter2_10 = new L2CharPosition(148952, 115264, -3725, 0);
  private static final L2CharPosition hunter2_11 = new L2CharPosition(148087, 116152, -3725, 0);
  private static final L2CharPosition hunter2_12 = new L2CharPosition(147339, 116027, -3725, 0);

  private static final L2CharPosition hunter3_1 = new L2CharPosition(146074, 112690, -3725, 0);
  private static final L2CharPosition hunter3_2 = new L2CharPosition(147155, 112905, -3725, 0);
  private static final L2CharPosition hunter3_3 = new L2CharPosition(148134, 112679, -3725, 0);
  private static final L2CharPosition hunter3_4 = new L2CharPosition(148905, 112887, -3725, 0);
  private static final L2CharPosition hunter3_5 = new L2CharPosition(149292, 113881, -3725, 0);
  private static final L2CharPosition hunter3_6 = new L2CharPosition(148952, 115264, -3725, 0);
  private static final L2CharPosition hunter3_7 = new L2CharPosition(148087, 116152, -3725, 0);
  private static final L2CharPosition hunter3_8 = new L2CharPosition(147339, 116027, -3725, 0);
  private static final L2CharPosition hunter3_9 = new L2CharPosition(146439, 116247, -3725, 0);
  private static final L2CharPosition hunter3_10 = new L2CharPosition(145501, 115897, -3725, 0);
  private static final L2CharPosition hunter3_11 = new L2CharPosition(145121, 114863, -3725, 0);
  private static final L2CharPosition hunter3_12 = new L2CharPosition(145177, 113352, -3725, 0);

  private static final L2CharPosition hunter4_1 = new L2CharPosition(144877, 114966, -3725, 0);
  private static final L2CharPosition hunter4_2 = new L2CharPosition(145063, 113865, -3725, 0);
  private static final L2CharPosition hunter4_3 = new L2CharPosition(145680, 112840, -3725, 0);
  private static final L2CharPosition hunter4_4 = new L2CharPosition(146766, 112688, -3725, 0);
  private static final L2CharPosition hunter4_5 = new L2CharPosition(148000, 112039, -3725, 0);
  private static final L2CharPosition hunter4_6 = new L2CharPosition(148694, 112319, -3725, 0);
  private static final L2CharPosition hunter4_7 = new L2CharPosition(148852, 113264, -3725, 0);
  private static final L2CharPosition hunter4_8 = new L2CharPosition(149161, 114305, -3725, 0);
  private static final L2CharPosition hunter4_9 = new L2CharPosition(148896, 115364, -3725, 0);
  private static final L2CharPosition hunter4_10 = new L2CharPosition(148031, 115980, -3725, 0);
  private static final L2CharPosition hunter4_11 = new L2CharPosition(146716, 116319, -3725, 0);
  private static final L2CharPosition hunter4_12 = new L2CharPosition(146031, 115749, -3725, 0);

  private static final L2CharPosition hunter5_1 = new L2CharPosition(148000, 112039, -3725, 0);
  private static final L2CharPosition hunter5_2 = new L2CharPosition(148694, 112319, -3725, 0);
  private static final L2CharPosition hunter5_3 = new L2CharPosition(148852, 113264, -3725, 0);
  private static final L2CharPosition hunter5_4 = new L2CharPosition(149161, 114305, -3725, 0);
  private static final L2CharPosition hunter5_5 = new L2CharPosition(148896, 115364, -3725, 0);
  private static final L2CharPosition hunter5_6 = new L2CharPosition(148031, 115980, -3725, 0);
  private static final L2CharPosition hunter5_7 = new L2CharPosition(146716, 116319, -3725, 0);
  private static final L2CharPosition hunter5_8 = new L2CharPosition(146031, 115749, -3725, 0);
  private static final L2CharPosition hunter5_9 = new L2CharPosition(144877, 114966, -3725, 0);
  private static final L2CharPosition hunter5_10 = new L2CharPosition(145063, 113865, -3725, 0);
  private static final L2CharPosition hunter5_11 = new L2CharPosition(145680, 112840, -3725, 0);
  private static final L2CharPosition hunter5_12 = new L2CharPosition(146766, 112688, -3725, 0);

  private static final L2CharPosition hunter6_1 = new L2CharPosition(148896, 115364, -3725, 0);
  private static final L2CharPosition hunter6_2 = new L2CharPosition(148031, 115980, -3725, 0);
  private static final L2CharPosition hunter6_3 = new L2CharPosition(146716, 116319, -3725, 0);
  private static final L2CharPosition hunter6_4 = new L2CharPosition(146031, 115749, -3725, 0);
  private static final L2CharPosition hunter6_5 = new L2CharPosition(144877, 114966, -3725, 0);
  private static final L2CharPosition hunter6_6 = new L2CharPosition(145063, 113865, -3725, 0);
  private static final L2CharPosition hunter6_7 = new L2CharPosition(145680, 112840, -3725, 0);
  private static final L2CharPosition hunter6_8 = new L2CharPosition(146766, 112688, -3725, 0);
  private static final L2CharPosition hunter6_9 = new L2CharPosition(148000, 112039, -3725, 0);
  private static final L2CharPosition hunter6_10 = new L2CharPosition(148694, 112319, -3725, 0);
  private static final L2CharPosition hunter6_11 = new L2CharPosition(148852, 113264, -3725, 0);
  private static final L2CharPosition hunter6_12 = new L2CharPosition(149161, 114305, -3725, 0);

  public DrakeScout(int questId, String name, String descr)
  {
    super(questId, name, descr);

    addKillId(HUNTER);
    addAggroRangeEnterId(HUNTER);

    startQuestTimer("hunter1_spawn", 7000L, null, null);
    startQuestTimer("hunter2_spawn", 7000L, null, null);
    startQuestTimer("hunter3_spawn", 7000L, null, null);
    startQuestTimer("hunter4_spawn", 7000L, null, null);
    startQuestTimer("hunter5_spawn", 7000L, null, null);
    startQuestTimer("hunter6_spawn", 7000L, null, null);
  }

  @Override
public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
  {
    if ((event.equalsIgnoreCase("hunter1_spawn")) && (this.hunter1 == null))
    {
      this.hunter1 = addSpawn(HUNTER, 149292, 113881, -3725, 0, false, 0L);
      this.hunter1.setIsNoRndWalk(true);
      this.hunter1.setRunning();
      startQuestTimer("trasa_1", 7000L, this.hunter1, null);
    }
    else if ((event.equalsIgnoreCase("trasa_nova")) && (this.hunter1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_nova", 7000L, this.hunter1, null);
      }
      else
      {
        this.hunter1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter1_1);
        this.hunter1.setRunning();
        startQuestTimer("trasa_1", 7000L, this.hunter1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_1")) && (this.hunter1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_1", 7000L, this.hunter1, null);
      }
      else
      {
        if (!this.hunter1.isInsideRadius(hunter1_1.x, hunter1_1.y, hunter1_1.z, 100, true, false))
          this.hunter1.teleToLocation(hunter1_1.x, hunter1_1.y, hunter1_1.z);
        this.hunter1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter1_2);
        this.hunter1.setRunning();
        startQuestTimer("trasa_2", 7000L, this.hunter1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_2")) && (this.hunter1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_2", 7000L, this.hunter1, null);
      }
      else
      {
        this.hunter1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter1_3);
        this.hunter1.setRunning();
        startQuestTimer("trasa_3", 7000L, this.hunter1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_3")) && (this.hunter1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_3", 7000L, this.hunter1, null);
      }
      else
      {
        this.hunter1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter1_4);
        this.hunter1.setRunning();
        startQuestTimer("trasa_4", 7000L, this.hunter1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_4")) && (this.hunter1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_4", 7000L, this.hunter1, null);
      }
      else
      {
        this.hunter1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter1_5);
        this.hunter1.setRunning();
        startQuestTimer("trasa_5", 7000L, this.hunter1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_5")) && (this.hunter1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_5", 7000L, this.hunter1, null);
      }
      else
      {
        this.hunter1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter1_6);
        this.hunter1.setRunning();
        startQuestTimer("trasa_6", 7000L, this.hunter1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_6")) && (this.hunter1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_6", 7000L, this.hunter1, null);
      }
      else
      {
        this.hunter1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter1_7);
        this.hunter1.setRunning();
        startQuestTimer("trasa_7", 7000L, this.hunter1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_7")) && (this.hunter1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_7", 7000L, this.hunter1, null);
      }
      else
      {
        this.hunter1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter1_8);
        this.hunter1.setRunning();
        startQuestTimer("trasa_8", 7000L, this.hunter1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_8")) && (this.hunter1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_8", 7000L, this.hunter1, null);
      }
      else
      {
        this.hunter1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter1_9);
        this.hunter1.setRunning();
        startQuestTimer("trasa_9", 7000L, this.hunter1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_9")) && (this.hunter1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_9", 7000L, this.hunter1, null);
      }
      else
      {
        this.hunter1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter1_10);
        this.hunter1.setRunning();
        startQuestTimer("trasa_10", 7000L, this.hunter1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_10")) && (this.hunter1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_10", 7000L, this.hunter1, null);
      }
      else
      {
        this.hunter1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter1_11);
        this.hunter1.setRunning();
        startQuestTimer("trasa_11", 7000L, this.hunter1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_11")) && (this.hunter1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_11", 7000L, this.hunter1, null);
      }
      else
      {
        this.hunter1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter1_12);
        this.hunter1.setRunning();
        startQuestTimer("trasa_nova", 7000L, this.hunter1, null);
      }
    }
    else if ((event.equalsIgnoreCase("hunter2_spawn")) && (this.hunter2 == null))
    {
      this.hunter2 = addSpawn(HUNTER, 146439, 116247, -3725, 0, false, 0L);
      this.hunter2.setIsNoRndWalk(true);
      this.hunter2.setRunning();
      startQuestTimer("trasa_12", 7000L, this.hunter2, null);
    }
    else if ((event.equalsIgnoreCase("trasa_nova2")) && (this.hunter2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_nova2", 7000L, this.hunter2, null);
      }
      else
      {
        this.hunter2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter2_1);
        this.hunter2.setRunning();
        startQuestTimer("trasa_12", 7000L, this.hunter2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_12")) && (this.hunter2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_12", 7000L, this.hunter2, null);
      }
      else
      {
        if (!this.hunter2.isInsideRadius(hunter2_1.x, hunter2_1.y, hunter2_1.z, 100, true, false))
          this.hunter2.teleToLocation(hunter2_1.x, hunter2_1.y, hunter2_1.z);
        this.hunter2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter2_2);
        this.hunter2.setRunning();
        startQuestTimer("trasa_13", 7000L, this.hunter2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_13")) && (this.hunter2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_13", 7000L, this.hunter2, null);
      }
      else
      {
        this.hunter2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter2_3);
        this.hunter2.setRunning();
        startQuestTimer("trasa_14", 7000L, this.hunter2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_14")) && (this.hunter2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_14", 7000L, this.hunter2, null);
      }
      else
      {
        this.hunter2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter2_4);
        this.hunter2.setRunning();
        startQuestTimer("trasa_15", 7000L, this.hunter2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_15")) && (this.hunter2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_15", 7000L, this.hunter2, null);
      }
      else
      {
        this.hunter2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter2_5);
        this.hunter2.setRunning();
        startQuestTimer("trasa_16", 7000L, this.hunter2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_16")) && (this.hunter2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_16", 7000L, this.hunter2, null);
      }
      else
      {
        this.hunter2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter2_6);
        this.hunter2.setRunning();
        startQuestTimer("trasa_17", 7000L, this.hunter2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_17")) && (this.hunter2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_17", 7000L, this.hunter2, null);
      }
      else
      {
        this.hunter2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter2_7);
        this.hunter2.setRunning();
        startQuestTimer("trasa_18", 7000L, this.hunter2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_18")) && (this.hunter2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_18", 7000L, this.hunter2, null);
      }
      else
      {
        this.hunter2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter2_8);
        this.hunter2.setRunning();
        startQuestTimer("trasa_19", 7000L, this.hunter2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_19")) && (this.hunter2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_19", 7000L, this.hunter2, null);
      }
      else
      {
        this.hunter2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter2_9);
        this.hunter2.setRunning();
        startQuestTimer("trasa_20", 7000L, this.hunter2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_20")) && (this.hunter2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_20", 7000L, this.hunter2, null);
      }
      else
      {
        this.hunter2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter2_10);
        this.hunter2.setRunning();
        startQuestTimer("trasa_21", 7000L, this.hunter2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_21")) && (this.hunter2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_21", 7000L, this.hunter2, null);
      }
      else
      {
        this.hunter2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter2_11);
        this.hunter2.setRunning();
        startQuestTimer("trasa_22", 7000L, this.hunter2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_22")) && (this.hunter2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_22", 7000L, this.hunter2, null);
      }
      else
      {
        this.hunter2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter2_12);
        this.hunter2.setRunning();
        startQuestTimer("trasa_nova2", 7000L, this.hunter2, null);
      }
    }
    else if ((event.equalsIgnoreCase("hunter3_spawn")) && (this.hunter3 == null))
    {
      this.hunter3 = addSpawn(HUNTER, 146074, 112690, -3725, 0, false, 0L);
      this.hunter3.setIsNoRndWalk(true);
      this.hunter3.setRunning();
      startQuestTimer("trasa_23", 7000L, this.hunter3, null);
    }
    else if ((event.equalsIgnoreCase("trasa_nova3")) && (this.hunter3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_nova3", 7000L, this.hunter3, null);
      }
      else
      {
        this.hunter3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter3_1);
        this.hunter3.setRunning();
        startQuestTimer("trasa_23", 7000L, this.hunter3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_23")) && (this.hunter3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_23", 7000L, this.hunter3, null);
      }
      else
      {
        if (!this.hunter3.isInsideRadius(hunter3_1.x, hunter3_1.y, hunter3_1.z, 100, true, false))
          this.hunter3.teleToLocation(hunter3_1.x, hunter3_1.y, hunter3_1.z);
        this.hunter3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter3_2);
        this.hunter3.setRunning();
        startQuestTimer("trasa_24", 7000L, this.hunter3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_24")) && (this.hunter3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_24", 7000L, this.hunter3, null);
      }
      else
      {
        this.hunter3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter3_3);
        this.hunter3.setRunning();
        startQuestTimer("trasa_25", 7000L, this.hunter3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_25")) && (this.hunter3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_25", 7000L, this.hunter3, null);
      }
      else
      {
        this.hunter3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter3_4);
        this.hunter3.setRunning();
        startQuestTimer("trasa_26", 7000L, this.hunter3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_26")) && (this.hunter3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_26", 7000L, this.hunter3, null);
      }
      else
      {
        this.hunter3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter3_5);
        this.hunter3.setRunning();
        startQuestTimer("trasa_27", 7000L, this.hunter3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_27")) && (this.hunter3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_27", 7000L, this.hunter3, null);
      }
      else
      {
        this.hunter3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter3_6);
        this.hunter3.setRunning();
        startQuestTimer("trasa_28", 7000L, this.hunter3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_28")) && (this.hunter3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_28", 7000L, this.hunter3, null);
      }
      else
      {
        this.hunter3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter3_7);
        this.hunter3.setRunning();
        startQuestTimer("trasa_29", 7000L, this.hunter3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_29")) && (this.hunter3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_29", 7000L, this.hunter3, null);
      }
      else
      {
        this.hunter3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter3_8);
        this.hunter3.setRunning();
        startQuestTimer("trasa_30", 7000L, this.hunter3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_30")) && (this.hunter3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_30", 7000L, this.hunter3, null);
      }
      else
      {
        this.hunter3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter3_9);
        this.hunter3.setRunning();
        startQuestTimer("trasa_31", 7000L, this.hunter3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_31")) && (this.hunter3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_31", 7000L, this.hunter3, null);
      }
      else
      {
        this.hunter3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter3_10);
        this.hunter3.setRunning();
        startQuestTimer("trasa_32", 7000L, this.hunter3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_32")) && (this.hunter3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_32", 7000L, this.hunter3, null);
      }
      else
      {
        this.hunter3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter3_11);
        this.hunter3.setRunning();
        startQuestTimer("trasa_33", 7000L, this.hunter3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_33")) && (this.hunter3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_33", 7000L, this.hunter3, null);
      }
      else
      {
        this.hunter3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter3_12);
        this.hunter3.setRunning();
        startQuestTimer("trasa_nova3", 7000L, this.hunter3, null);
      }
    }
    else if ((event.equalsIgnoreCase("hunter4_spawn")) && (this.hunter4 == null))
    {
      this.hunter4 = addSpawn(HUNTER, 144877, 114966, -3725, 0, false, 0L);
      this.hunter4.setIsNoRndWalk(true);
      this.hunter4.setRunning();
      startQuestTimer("trasa_34", 7000L, this.hunter4, null);
    }
    else if ((event.equalsIgnoreCase("trasa_nova4")) && (this.hunter4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_nova4", 7000L, this.hunter4, null);
      }
      else
      {
        this.hunter4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter4_1);
        this.hunter4.setRunning();
        startQuestTimer("trasa_34", 7000L, this.hunter4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_34")) && (this.hunter4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_34", 7000L, this.hunter4, null);
      }
      else
      {
        if (!this.hunter4.isInsideRadius(hunter4_1.x, hunter4_1.y, hunter4_1.z, 100, true, false))
          this.hunter4.teleToLocation(hunter4_1.x, hunter4_1.y, hunter4_1.z);
        this.hunter4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter4_2);
        this.hunter4.setRunning();
        startQuestTimer("trasa_35", 7000L, this.hunter4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_35")) && (this.hunter4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_35", 7000L, this.hunter4, null);
      }
      else
      {
        this.hunter4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter4_3);
        this.hunter4.setRunning();
        startQuestTimer("trasa_36", 7000L, this.hunter4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_36")) && (this.hunter4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_36", 7000L, this.hunter4, null);
      }
      else
      {
        this.hunter4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter4_4);
        this.hunter4.setRunning();
        startQuestTimer("trasa_37", 7000L, this.hunter4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_37")) && (this.hunter4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_37", 7000L, this.hunter4, null);
      }
      else
      {
        this.hunter4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter4_5);
        this.hunter4.setRunning();
        startQuestTimer("trasa_38", 7000L, this.hunter4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_38")) && (this.hunter4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_38", 7000L, this.hunter4, null);
      }
      else
      {
        this.hunter4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter4_6);
        this.hunter4.setRunning();
        startQuestTimer("trasa_39", 7000L, this.hunter4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_39")) && (this.hunter4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_39", 7000L, this.hunter4, null);
      }
      else
      {
        this.hunter4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter4_7);
        this.hunter4.setRunning();
        startQuestTimer("trasa_40", 7000L, this.hunter4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_40")) && (this.hunter4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_40", 7000L, this.hunter4, null);
      }
      else
      {
        this.hunter4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter4_8);
        this.hunter4.setRunning();
        startQuestTimer("trasa_41", 7000L, this.hunter4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_41")) && (this.hunter4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_41", 7000L, this.hunter4, null);
      }
      else
      {
        this.hunter4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter4_9);
        this.hunter4.setRunning();
        startQuestTimer("trasa_42", 7000L, this.hunter4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_42")) && (this.hunter4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_42", 7000L, this.hunter4, null);
      }
      else
      {
        this.hunter4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter4_10);
        this.hunter4.setRunning();
        startQuestTimer("trasa_43", 7000L, this.hunter4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_43")) && (this.hunter4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_43", 7000L, this.hunter4, null);
      }
      else
      {
        this.hunter4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter4_11);
        this.hunter4.setRunning();
        startQuestTimer("trasa_44", 7000L, this.hunter4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_44")) && (this.hunter4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_44", 7000L, this.hunter4, null);
      }
      else
      {
        this.hunter4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter4_12);
        this.hunter4.setRunning();
        startQuestTimer("trasa_nova4", 7000L, this.hunter4, null);
      }
    }
    else if ((event.equalsIgnoreCase("hunter5_spawn")) && (this.hunter5 == null))
    {
      this.hunter5 = addSpawn(HUNTER, 148000, 112039, -3725, 0, false, 0L);
      this.hunter5.setIsNoRndWalk(true);
      this.hunter5.setRunning();
      startQuestTimer("trasa_45", 7000L, this.hunter5, null);
    }
    else if ((event.equalsIgnoreCase("trasa_nova5")) && (this.hunter5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_nova5", 7000L, this.hunter5, null);
      }
      else
      {
        this.hunter5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter5_1);
        this.hunter5.setRunning();
        startQuestTimer("trasa_45", 7000L, this.hunter5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_45")) && (this.hunter5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_45", 7000L, this.hunter5, null);
      }
      else
      {
        if (!this.hunter5.isInsideRadius(hunter5_1.x, hunter5_1.y, hunter5_1.z, 100, true, false))
          this.hunter5.teleToLocation(hunter5_1.x, hunter5_1.y, hunter5_1.z);
        this.hunter5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter5_2);
        this.hunter5.setRunning();
        startQuestTimer("trasa_46", 7000L, this.hunter5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_46")) && (this.hunter5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_46", 7000L, this.hunter5, null);
      }
      else
      {
        this.hunter5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter5_3);
        this.hunter5.setRunning();
        startQuestTimer("trasa_47", 7000L, this.hunter5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_47")) && (this.hunter5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_47", 7000L, this.hunter5, null);
      }
      else
      {
        this.hunter5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter5_4);
        this.hunter5.setRunning();
        startQuestTimer("trasa_48", 7000L, this.hunter5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_48")) && (this.hunter5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_48", 7000L, this.hunter5, null);
      }
      else
      {
        this.hunter5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter5_5);
        this.hunter5.setRunning();
        startQuestTimer("trasa_49", 7000L, this.hunter5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_49")) && (this.hunter5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_49", 7000L, this.hunter5, null);
      }
      else
      {
        this.hunter5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter5_6);
        this.hunter5.setRunning();
        startQuestTimer("trasa_50", 7000L, this.hunter5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_50")) && (this.hunter5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_50", 7000L, this.hunter5, null);
      }
      else
      {
        this.hunter5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter5_7);
        this.hunter5.setRunning();
        startQuestTimer("trasa_51", 7000L, this.hunter5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_51")) && (this.hunter5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_51", 7000L, this.hunter5, null);
      }
      else
      {
        this.hunter5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter5_8);
        this.hunter5.setRunning();
        startQuestTimer("trasa_52", 7000L, this.hunter5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_52")) && (this.hunter5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_52", 7000L, this.hunter5, null);
      }
      else
      {
        this.hunter5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter5_9);
        this.hunter5.setRunning();
        startQuestTimer("trasa_53", 7000L, this.hunter5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_53")) && (this.hunter5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_53", 7000L, this.hunter5, null);
      }
      else
      {
        this.hunter5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter5_10);
        this.hunter5.setRunning();
        startQuestTimer("trasa_54", 7000L, this.hunter5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_54")) && (this.hunter5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_54", 7000L, this.hunter5, null);
      }
      else
      {
        this.hunter5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter5_11);
        this.hunter5.setRunning();
        startQuestTimer("trasa_55", 7000L, this.hunter5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_55")) && (this.hunter5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_55", 7000L, this.hunter5, null);
      }
      else
      {
        this.hunter5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter5_12);
        this.hunter5.setRunning();
        startQuestTimer("trasa_nova5", 7000L, this.hunter5, null);
      }
    }
    else if ((event.equalsIgnoreCase("hunter6_spawn")) && (this.hunter6 == null))
    {
      this.hunter6 = addSpawn(HUNTER, 148896, 115364, -3725, 0, false, 0L);
      this.hunter6.setIsNoRndWalk(true);
      this.hunter6.setRunning();
      startQuestTimer("trasa_56", 7000L, this.hunter6, null);
    }
    else if ((event.equalsIgnoreCase("trasa_nova6")) && (this.hunter6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_nova6", 7000L, this.hunter6, null);
      }
      else
      {
        this.hunter6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter6_1);
        this.hunter6.setRunning();
        startQuestTimer("trasa_56", 7000L, this.hunter6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_56")) && (this.hunter6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_56", 7000L, this.hunter6, null);
      }
      else
      {
        if (!this.hunter6.isInsideRadius(hunter6_1.x, hunter6_1.y, hunter6_1.z, 100, true, false))
          this.hunter6.teleToLocation(hunter6_1.x, hunter6_1.y, hunter6_1.z);
        this.hunter6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter6_2);
        this.hunter6.setRunning();
        startQuestTimer("trasa_57", 7000L, this.hunter6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_57")) && (this.hunter6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_57", 7000L, this.hunter6, null);
      }
      else
      {
        this.hunter6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter6_3);
        this.hunter6.setRunning();
        startQuestTimer("trasa_58", 7000L, this.hunter6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_58")) && (this.hunter6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_58", 7000L, this.hunter6, null);
      }
      else
      {
        this.hunter6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter6_4);
        this.hunter6.setRunning();
        startQuestTimer("trasa_59", 7000L, this.hunter6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_59")) && (this.hunter6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_59", 7000L, this.hunter6, null);
      }
      else
      {
        this.hunter6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter6_5);
        this.hunter6.setRunning();
        startQuestTimer("trasa_60", 7000L, this.hunter6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_60")) && (this.hunter6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_60", 7000L, this.hunter6, null);
      }
      else
      {
        this.hunter6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter6_6);
        this.hunter6.setRunning();
        startQuestTimer("trasa_61", 7000L, this.hunter6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_61")) && (this.hunter6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_61", 7000L, this.hunter6, null);
      }
      else
      {
        this.hunter6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter6_7);
        this.hunter6.setRunning();
        startQuestTimer("trasa_62", 7000L, this.hunter6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_62")) && (this.hunter6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_62", 7000L, this.hunter6, null);
      }
      else
      {
        this.hunter6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter6_8);
        this.hunter6.setRunning();
        startQuestTimer("trasa_63", 7000L, this.hunter6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_63")) && (this.hunter6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_63", 7000L, this.hunter6, null);
      }
      else
      {
        this.hunter6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter6_9);
        this.hunter6.setRunning();
        startQuestTimer("trasa_64", 7000L, this.hunter6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_64")) && (this.hunter6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_64", 7000L, this.hunter6, null);
      }
      else
      {
        this.hunter6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter6_10);
        this.hunter6.setRunning();
        startQuestTimer("trasa_65", 7000L, this.hunter6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_65")) && (this.hunter6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_65", 7000L, this.hunter6, null);
      }
      else
      {
        this.hunter6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter6_11);
        this.hunter6.setRunning();
        startQuestTimer("trasa_66", 7000L, this.hunter6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_66")) && (this.hunter6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_66", 7000L, this.hunter6, null);
      }
      else
      {
        this.hunter6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, hunter6_12);
        this.hunter6.setRunning();
        startQuestTimer("trasa_nova6", 7000L, this.hunter6, null);
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
    if (npc.getNpcId() == HUNTER)
    {
      if (npc == this.hunter1)
      {
        startQuestTimer("hunter1_spawn", 300000L, null, null);
        this.hunter1 = null;
      }

      if (npc == this.hunter2)
      {
        startQuestTimer("hunter2_spawn", 300000L, null, null);
        this.hunter2 = null;
      }

      if (npc == this.hunter3)
      {
        startQuestTimer("hunter3_spawn", 300000L, null, null);
        this.hunter3 = null;
      }

      if (npc == this.hunter4)
      {
        startQuestTimer("hunter4_spawn", 300000L, null, null);
        this.hunter4 = null;
      }

      if (npc == this.hunter5)
      {
        startQuestTimer("hunter5_spawn", 300000L, null, null);
        this.hunter5 = null;
      }

      if (npc == this.hunter6)
      {
        startQuestTimer("hunter6_spawn", 300000L, null, null);
        this.hunter6 = null;
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
    new DrakeScout(-1, "DrakeScout", "ai");
  }
}
