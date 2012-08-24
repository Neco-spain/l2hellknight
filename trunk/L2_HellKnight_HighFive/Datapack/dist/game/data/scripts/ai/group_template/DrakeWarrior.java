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

public class DrakeWarrior extends L2AttackableAIScript
{
  private static final int WARLORD = 22850;
  private L2Npc warlord1;
  private L2Npc warlord2;
  private L2Npc warlord3;
  private L2Npc warlord4;
  private L2Npc warlord5;
  private L2Npc warlord6;
  private static final L2CharPosition warlord1_1 = new L2CharPosition(144937, 114885, -3725, 0);
  private static final L2CharPosition warlord1_2 = new L2CharPosition(145071, 113495, -3725, 0);
  private static final L2CharPosition warlord1_3 = new L2CharPosition(145815, 112778, -3725, 0);
  private static final L2CharPosition warlord1_4 = new L2CharPosition(146710, 112392, -3725, 0);
  private static final L2CharPosition warlord1_5 = new L2CharPosition(147766, 112466, -3725, 0);
  private static final L2CharPosition warlord1_6 = new L2CharPosition(149019, 113299, -3725, 0);
  private static final L2CharPosition warlord1_7 = new L2CharPosition(149321, 114526, -3725, 0);
  private static final L2CharPosition warlord1_8 = new L2CharPosition(148788, 115785, -3725, 0);
  private static final L2CharPosition warlord1_9 = new L2CharPosition(147606, 116500, -3704, 0);
  private static final L2CharPosition warlord1_10 = new L2CharPosition(146563, 116116, -3725, 0);
  private static final L2CharPosition warlord1_11 = new L2CharPosition(145817, 115812, -3697, 0);
  private static final L2CharPosition warlord1_12 = new L2CharPosition(145457, 115249, -3725, 0);

  private static final L2CharPosition warlord2_1 = new L2CharPosition(147766, 112466, -3725, 0);
  private static final L2CharPosition warlord2_2 = new L2CharPosition(149019, 113299, -3725, 0);
  private static final L2CharPosition warlord2_3 = new L2CharPosition(149321, 114526, -3725, 0);
  private static final L2CharPosition warlord2_4 = new L2CharPosition(148788, 115785, -3725, 0);
  private static final L2CharPosition warlord2_5 = new L2CharPosition(147606, 116500, -3704, 0);
  private static final L2CharPosition warlord2_6 = new L2CharPosition(146563, 116116, -3725, 0);
  private static final L2CharPosition warlord2_7 = new L2CharPosition(145817, 115812, -3697, 0);
  private static final L2CharPosition warlord2_8 = new L2CharPosition(145457, 115249, -3725, 0);
  private static final L2CharPosition warlord2_9 = new L2CharPosition(144937, 114885, -3725, 0);
  private static final L2CharPosition warlord2_10 = new L2CharPosition(145071, 113495, -3725, 0);
  private static final L2CharPosition warlord2_11 = new L2CharPosition(145815, 112778, -3725, 0);
  private static final L2CharPosition warlord2_12 = new L2CharPosition(146710, 112392, -3725, 0);

  private static final L2CharPosition warlord3_1 = new L2CharPosition(147606, 116500, -3704, 0);
  private static final L2CharPosition warlord3_2 = new L2CharPosition(146563, 116116, -3725, 0);
  private static final L2CharPosition warlord3_3 = new L2CharPosition(145817, 115812, -3697, 0);
  private static final L2CharPosition warlord3_4 = new L2CharPosition(145457, 115249, -3725, 0);
  private static final L2CharPosition warlord3_5 = new L2CharPosition(144937, 114885, -3725, 0);
  private static final L2CharPosition warlord3_6 = new L2CharPosition(145071, 113495, -3725, 0);
  private static final L2CharPosition warlord3_7 = new L2CharPosition(145815, 112778, -3725, 0);
  private static final L2CharPosition warlord3_8 = new L2CharPosition(146710, 112392, -3725, 0);
  private static final L2CharPosition warlord3_9 = new L2CharPosition(147766, 112466, -3725, 0);
  private static final L2CharPosition warlord3_10 = new L2CharPosition(149019, 113299, -3725, 0);
  private static final L2CharPosition warlord3_11 = new L2CharPosition(149321, 114526, -3725, 0);
  private static final L2CharPosition warlord3_12 = new L2CharPosition(148788, 115785, -3725, 0);

  private static final L2CharPosition warlord4_1 = new L2CharPosition(144767, 114327, -3719, 0);
  private static final L2CharPosition warlord4_2 = new L2CharPosition(145257, 113427, -3725, 0);
  private static final L2CharPosition warlord4_3 = new L2CharPosition(145945, 112702, -3725, 0);
  private static final L2CharPosition warlord4_4 = new L2CharPosition(147311, 112476, -3725, 0);
  private static final L2CharPosition warlord4_5 = new L2CharPosition(148287, 112057, -3725, 0);
  private static final L2CharPosition warlord4_6 = new L2CharPosition(148913, 113315, -3725, 0);
  private static final L2CharPosition warlord4_7 = new L2CharPosition(149102, 114690, -3725, 0);
  private static final L2CharPosition warlord4_8 = new L2CharPosition(148838, 115555, -3725, 0);
  private static final L2CharPosition warlord4_9 = new L2CharPosition(147709, 116165, -3725, 0);
  private static final L2CharPosition warlord4_10 = new L2CharPosition(146585, 116263, -3725, 0);
  private static final L2CharPosition warlord4_11 = new L2CharPosition(145644, 115434, -3725, 0);
  private static final L2CharPosition warlord4_12 = new L2CharPosition(144910, 115093, -3725, 0);

  private static final L2CharPosition warlord5_1 = new L2CharPosition(148287, 112057, -3725, 0);
  private static final L2CharPosition warlord5_2 = new L2CharPosition(148913, 113315, -3725, 0);
  private static final L2CharPosition warlord5_3 = new L2CharPosition(149102, 114690, -3725, 0);
  private static final L2CharPosition warlord5_4 = new L2CharPosition(148838, 115555, -3725, 0);
  private static final L2CharPosition warlord5_5 = new L2CharPosition(147709, 116165, -3725, 0);
  private static final L2CharPosition warlord5_6 = new L2CharPosition(146585, 116263, -3725, 0);
  private static final L2CharPosition warlord5_7 = new L2CharPosition(145644, 115434, -3725, 0);
  private static final L2CharPosition warlord5_8 = new L2CharPosition(144910, 115093, -3725, 0);
  private static final L2CharPosition warlord5_9 = new L2CharPosition(144767, 114327, -3719, 0);
  private static final L2CharPosition warlord5_10 = new L2CharPosition(145257, 113427, -3725, 0);
  private static final L2CharPosition warlord5_11 = new L2CharPosition(145945, 112702, -3725, 0);
  private static final L2CharPosition warlord5_12 = new L2CharPosition(147311, 112476, -3725, 0);

  private static final L2CharPosition warlord6_1 = new L2CharPosition(147709, 116165, -3725, 0);
  private static final L2CharPosition warlord6_2 = new L2CharPosition(146585, 116263, -3725, 0);
  private static final L2CharPosition warlord6_3 = new L2CharPosition(145644, 115434, -3725, 0);
  private static final L2CharPosition warlord6_4 = new L2CharPosition(144910, 115093, -3725, 0);
  private static final L2CharPosition warlord6_5 = new L2CharPosition(144767, 114327, -3719, 0);
  private static final L2CharPosition warlord6_6 = new L2CharPosition(145257, 113427, -3725, 0);
  private static final L2CharPosition warlord6_7 = new L2CharPosition(145945, 112702, -3725, 0);
  private static final L2CharPosition warlord6_8 = new L2CharPosition(147311, 112476, -3725, 0);
  private static final L2CharPosition warlord6_9 = new L2CharPosition(148287, 112057, -3725, 0);
  private static final L2CharPosition warlord6_10 = new L2CharPosition(148913, 113315, -3725, 0);
  private static final L2CharPosition warlord6_11 = new L2CharPosition(149102, 114690, -3725, 0);
  private static final L2CharPosition warlord6_12 = new L2CharPosition(148838, 115555, -3725, 0);

  public DrakeWarrior(int questId, String name, String descr)
  {
    super(questId, name, descr);

    addKillId(WARLORD);
    addAggroRangeEnterId(WARLORD);

    startQuestTimer("warlord1_spawn", 1000L, null, null);
    startQuestTimer("warlord2_spawn", 2000L, null, null);
    startQuestTimer("warlord3_spawn", 3000L, null, null);
    startQuestTimer("warlord4_spawn", 14000L, null, null);
    startQuestTimer("warlord5_spawn", 15000L, null, null);
    startQuestTimer("warlord6_spawn", 16000L, null, null);
  }

  @Override
public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
  {
    if ((event.equalsIgnoreCase("warlord1_spawn")) && (this.warlord1 == null))
    {
      this.warlord1 = addSpawn(WARLORD, 144937, 114885, -3725, 0, false, 0L);
      this.warlord1.setIsNoRndWalk(true);
      this.warlord1.setRunning();
      startQuestTimer("trasa_1", 7000L, this.warlord1, null);
    }
    else if ((event.equalsIgnoreCase("trasa_nova")) && (this.warlord1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_nova", 7000L, this.warlord1, null);
      }
      else
      {
        this.warlord1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord1_1);
        this.warlord1.setRunning();
        startQuestTimer("trasa_1", 7000L, this.warlord1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_1")) && (this.warlord1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_1", 7000L, this.warlord1, null);
      }
      else
      {
        if (!this.warlord1.isInsideRadius(warlord1_1.x, warlord1_1.y, warlord1_1.z, 100, true, false))
          this.warlord1.teleToLocation(warlord1_1.x, warlord1_1.y, warlord1_1.z);
        this.warlord1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord1_2);
        this.warlord1.setRunning();
        startQuestTimer("trasa_2", 7000L, this.warlord1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_2")) && (this.warlord1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_2", 7000L, this.warlord1, null);
      }
      else
      {
        this.warlord1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord1_3);
        this.warlord1.setRunning();
        startQuestTimer("trasa_3", 7000L, this.warlord1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_3")) && (this.warlord1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_3", 7000L, this.warlord1, null);
      }
      else
      {
        this.warlord1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord1_4);
        this.warlord1.setRunning();
        startQuestTimer("trasa_4", 7000L, this.warlord1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_4")) && (this.warlord1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_4", 7000L, this.warlord1, null);
      }
      else
      {
        this.warlord1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord1_5);
        this.warlord1.setRunning();
        startQuestTimer("trasa_5", 7000L, this.warlord1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_5")) && (this.warlord1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_5", 7000L, this.warlord1, null);
      }
      else
      {
        this.warlord1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord1_6);
        this.warlord1.setRunning();
        startQuestTimer("trasa_6", 7000L, this.warlord1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_6")) && (this.warlord1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_6", 7000L, this.warlord1, null);
      }
      else
      {
        this.warlord1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord1_7);
        this.warlord1.setRunning();
        startQuestTimer("trasa_7", 7000L, this.warlord1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_7")) && (this.warlord1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_7", 7000L, this.warlord1, null);
      }
      else
      {
        this.warlord1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord1_8);
        this.warlord1.setRunning();
        startQuestTimer("trasa_8", 7000L, this.warlord1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_8")) && (this.warlord1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_8", 7000L, this.warlord1, null);
      }
      else
      {
        this.warlord1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord1_9);
        this.warlord1.setRunning();
        startQuestTimer("trasa_9", 7000L, this.warlord1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_9")) && (this.warlord1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_9", 7000L, this.warlord1, null);
      }
      else
      {
        this.warlord1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord1_10);
        this.warlord1.setRunning();
        startQuestTimer("trasa_10", 7000L, this.warlord1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_10")) && (this.warlord1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_10", 7000L, this.warlord1, null);
      }
      else
      {
        this.warlord1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord1_11);
        this.warlord1.setRunning();
        startQuestTimer("trasa_11", 4000L, this.warlord1, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_11")) && (this.warlord1 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_11", 7000L, this.warlord1, null);
      }
      else
      {
        this.warlord1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord1_12);
        this.warlord1.setRunning();
        startQuestTimer("trasa_nova", 7000L, this.warlord1, null);
      }
    }
    else if ((event.equalsIgnoreCase("warlord2_spawn")) && (this.warlord2 == null))
    {
      this.warlord2 = addSpawn(WARLORD, 147766, 112466, -3725, 0, false, 0L);
      this.warlord2.setIsNoRndWalk(true);
      this.warlord2.setRunning();
      startQuestTimer("trasa_12", 7000L, this.warlord2, null);
    }
    else if ((event.equalsIgnoreCase("trasa_nova2")) && (this.warlord2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_nova2", 7000L, this.warlord2, null);
      }
      else
      {
        this.warlord2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord2_1);
        this.warlord2.setRunning();
        startQuestTimer("trasa_12", 7000L, this.warlord2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_12")) && (this.warlord2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_12", 7000L, this.warlord2, null);
      }
      else
      {
        if (!this.warlord2.isInsideRadius(warlord2_1.x, warlord2_1.y, warlord2_1.z, 100, true, false))
          this.warlord2.teleToLocation(warlord2_1.x, warlord2_1.y, warlord2_1.z);
        this.warlord2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord2_2);
        this.warlord2.setRunning();
        startQuestTimer("trasa_13", 7000L, this.warlord2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_13")) && (this.warlord2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_13", 7000L, this.warlord2, null);
      }
      else
      {
        this.warlord2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord2_3);
        this.warlord2.setRunning();
        startQuestTimer("trasa_14", 7000L, this.warlord2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_14")) && (this.warlord2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_14", 7000L, this.warlord2, null);
      }
      else
      {
        this.warlord2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord2_4);
        this.warlord2.setRunning();
        startQuestTimer("trasa_15", 7000L, this.warlord2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_15")) && (this.warlord2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_15", 7000L, this.warlord2, null);
      }
      else
      {
        this.warlord2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord2_5);
        this.warlord2.setRunning();
        startQuestTimer("trasa_16", 7000L, this.warlord2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_16")) && (this.warlord2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_16", 7000L, this.warlord2, null);
      }
      else
      {
        this.warlord2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord2_6);
        this.warlord2.setRunning();
        startQuestTimer("trasa_17", 7000L, this.warlord2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_17")) && (this.warlord2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_17", 7000L, this.warlord2, null);
      }
      else
      {
        this.warlord2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord2_7);
        this.warlord2.setRunning();
        startQuestTimer("trasa_18", 7000L, this.warlord2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_18")) && (this.warlord2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_18", 7000L, this.warlord2, null);
      }
      else
      {
        this.warlord2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord2_8);
        this.warlord2.setRunning();
        startQuestTimer("trasa_19", 7000L, this.warlord2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_19")) && (this.warlord2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_19", 7000L, this.warlord2, null);
      }
      else
      {
        this.warlord2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord2_9);
        this.warlord2.setRunning();
        startQuestTimer("trasa_20", 7000L, this.warlord2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_20")) && (this.warlord2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_20", 7000L, this.warlord2, null);
      }
      else
      {
        this.warlord2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord2_10);
        this.warlord2.setRunning();
        startQuestTimer("trasa_21", 7000L, this.warlord2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_21")) && (this.warlord2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_21", 7000L, this.warlord2, null);
      }
      else
      {
        this.warlord2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord2_11);
        this.warlord2.setRunning();
        startQuestTimer("trasa_22", 4000L, this.warlord2, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_22")) && (this.warlord2 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_22", 7000L, this.warlord2, null);
      }
      else
      {
        this.warlord2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord2_12);
        this.warlord2.setRunning();
        startQuestTimer("trasa_nova2", 7000L, this.warlord2, null);
      }
    }
    else if ((event.equalsIgnoreCase("warlord3_spawn")) && (this.warlord3 == null))
    {
      this.warlord3 = addSpawn(WARLORD, 147606, 116500, -3704, 0, false, 0L);
      this.warlord3.setIsNoRndWalk(true);
      this.warlord3.setRunning();
      startQuestTimer("trasa_23", 7000L, this.warlord3, null);
    }
    else if ((event.equalsIgnoreCase("trasa_nova3")) && (this.warlord3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_nova3", 7000L, this.warlord3, null);
      }
      else
      {
        this.warlord3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord3_1);
        this.warlord3.setRunning();
        startQuestTimer("trasa_23", 7000L, this.warlord3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_23")) && (this.warlord3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_23", 7000L, this.warlord3, null);
      }
      else
      {
        if (!this.warlord3.isInsideRadius(warlord3_1.x, warlord3_1.y, warlord3_1.z, 100, true, false))
          this.warlord3.teleToLocation(warlord3_1.x, warlord3_1.y, warlord3_1.z);
        this.warlord3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord3_2);
        this.warlord3.setRunning();
        startQuestTimer("trasa_24", 7000L, this.warlord3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_24")) && (this.warlord3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_24", 7000L, this.warlord3, null);
      }
      else
      {
        this.warlord3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord3_3);
        this.warlord3.setRunning();
        startQuestTimer("trasa_25", 7000L, this.warlord3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_25")) && (this.warlord3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_25", 7000L, this.warlord3, null);
      }
      else
      {
        this.warlord3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord3_4);
        this.warlord3.setRunning();
        startQuestTimer("trasa_26", 7000L, this.warlord3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_26")) && (this.warlord3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_26", 7000L, this.warlord3, null);
      }
      else
      {
        this.warlord3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord3_5);
        this.warlord3.setRunning();
        startQuestTimer("trasa_27", 7000L, this.warlord3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_27")) && (this.warlord3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_27", 7000L, this.warlord3, null);
      }
      else
      {
        this.warlord3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord3_6);
        this.warlord3.setRunning();
        startQuestTimer("trasa_28", 7000L, this.warlord3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_28")) && (this.warlord3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_28", 7000L, this.warlord3, null);
      }
      else
      {
        this.warlord3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord3_7);
        this.warlord3.setRunning();
        startQuestTimer("trasa_29", 7000L, this.warlord3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_29")) && (this.warlord3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_29", 7000L, this.warlord3, null);
      }
      else
      {
        this.warlord3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord3_8);
        this.warlord3.setRunning();
        startQuestTimer("trasa_30", 7000L, this.warlord3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_30")) && (this.warlord3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_30", 7000L, this.warlord3, null);
      }
      else
      {
        this.warlord3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord3_9);
        this.warlord3.setRunning();
        startQuestTimer("trasa_31", 7000L, this.warlord3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_31")) && (this.warlord3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_31", 7000L, this.warlord3, null);
      }
      else
      {
        this.warlord3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord3_10);
        this.warlord3.setRunning();
        startQuestTimer("trasa_32", 7000L, this.warlord3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_32")) && (this.warlord3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_32", 7000L, this.warlord3, null);
      }
      else
      {
        this.warlord3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord3_11);
        this.warlord3.setRunning();
        startQuestTimer("trasa_33", 4000L, this.warlord3, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_33")) && (this.warlord3 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_33", 7000L, this.warlord3, null);
      }
      else
      {
        this.warlord3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord3_12);
        this.warlord3.setRunning();
        startQuestTimer("trasa_nova3", 7000L, this.warlord3, null);
      }
    }
    else if ((event.equalsIgnoreCase("warlord4_spawn")) && (this.warlord4 == null))
    {
      this.warlord4 = addSpawn(WARLORD, 144767, 114327, -3719, 0, false, 0L);
      this.warlord4.setIsNoRndWalk(true);
      this.warlord4.setRunning();
      startQuestTimer("trasa_34", 7000L, this.warlord4, null);
    }
    else if ((event.equalsIgnoreCase("trasa_nova4")) && (this.warlord4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_nova4", 7000L, this.warlord4, null);
      }
      else
      {
        this.warlord4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord4_1);
        this.warlord4.setRunning();
        startQuestTimer("trasa_34", 7000L, this.warlord4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_34")) && (this.warlord4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_34", 7000L, this.warlord4, null);
      }
      else
      {
        if (!this.warlord4.isInsideRadius(warlord4_1.x, warlord4_1.y, warlord4_1.z, 100, true, false))
          this.warlord4.teleToLocation(warlord4_1.x, warlord4_1.y, warlord4_1.z);
        this.warlord4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord4_2);
        this.warlord4.setRunning();
        startQuestTimer("trasa_35", 7000L, this.warlord4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_35")) && (this.warlord4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_35", 7000L, this.warlord4, null);
      }
      else
      {
        this.warlord4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord4_3);
        this.warlord4.setRunning();
        startQuestTimer("trasa_36", 7000L, this.warlord4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_36")) && (this.warlord4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_36", 7000L, this.warlord4, null);
      }
      else
      {
        this.warlord4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord4_4);
        this.warlord4.setRunning();
        startQuestTimer("trasa_37", 7000L, this.warlord4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_37")) && (this.warlord4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_37", 7000L, this.warlord4, null);
      }
      else
      {
        this.warlord4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord4_5);
        this.warlord4.setRunning();
        startQuestTimer("trasa_38", 7000L, this.warlord4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_38")) && (this.warlord4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_38", 7000L, this.warlord4, null);
      }
      else
      {
        this.warlord4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord4_6);
        this.warlord4.setRunning();
        startQuestTimer("trasa_39", 7000L, this.warlord4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_39")) && (this.warlord4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_39", 7000L, this.warlord4, null);
      }
      else
      {
        this.warlord4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord4_7);
        this.warlord4.setRunning();
        startQuestTimer("trasa_40", 7000L, this.warlord4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_40")) && (this.warlord4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_40", 7000L, this.warlord4, null);
      }
      else
      {
        this.warlord4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord4_8);
        this.warlord4.setRunning();
        startQuestTimer("trasa_41", 7000L, this.warlord4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_41")) && (this.warlord4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_41", 7000L, this.warlord4, null);
      }
      else
      {
        this.warlord4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord4_9);
        this.warlord4.setRunning();
        startQuestTimer("trasa_42", 7000L, this.warlord4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_42")) && (this.warlord4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_42", 7000L, this.warlord4, null);
      }
      else
      {
        this.warlord4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord4_10);
        this.warlord4.setRunning();
        startQuestTimer("trasa_43", 7000L, this.warlord4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_43")) && (this.warlord4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_43", 7000L, this.warlord4, null);
      }
      else
      {
        this.warlord4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord4_11);
        this.warlord4.setRunning();
        startQuestTimer("trasa_44", 4000L, this.warlord4, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_44")) && (this.warlord4 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_44", 7000L, this.warlord4, null);
      }
      else
      {
        this.warlord4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord4_12);
        this.warlord4.setRunning();
        startQuestTimer("trasa_nova4", 7000L, this.warlord4, null);
      }
    }
    else if ((event.equalsIgnoreCase("warlord5_spawn")) && (this.warlord5 == null))
    {
      this.warlord5 = addSpawn(WARLORD, 147766, 112466, -3725, 0, false, 0L);
      this.warlord5.setIsNoRndWalk(true);
      this.warlord5.setRunning();
      startQuestTimer("trasa_45", 7000L, this.warlord5, null);
    }
    else if ((event.equalsIgnoreCase("trasa_nova5")) && (this.warlord5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_nova5", 7000L, this.warlord5, null);
      }
      else
      {
        this.warlord5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord5_1);
        this.warlord5.setRunning();
        startQuestTimer("trasa_45", 7000L, this.warlord5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_45")) && (this.warlord5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_45", 7000L, this.warlord5, null);
      }
      else
      {
        if (!this.warlord5.isInsideRadius(warlord5_1.x, warlord5_1.y, warlord5_1.z, 100, true, false))
          this.warlord5.teleToLocation(warlord5_1.x, warlord5_1.y, warlord5_1.z);
        this.warlord5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord5_2);
        this.warlord5.setRunning();
        startQuestTimer("trasa_46", 7000L, this.warlord5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_46")) && (this.warlord5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_46", 7000L, this.warlord5, null);
      }
      else
      {
        this.warlord5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord5_3);
        this.warlord5.setRunning();
        startQuestTimer("trasa_47", 7000L, this.warlord5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_47")) && (this.warlord5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_47", 7000L, this.warlord5, null);
      }
      else
      {
        this.warlord5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord5_4);
        this.warlord5.setRunning();
        startQuestTimer("trasa_48", 7000L, this.warlord5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_48")) && (this.warlord5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_48", 7000L, this.warlord5, null);
      }
      else
      {
        this.warlord5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord5_5);
        this.warlord5.setRunning();
        startQuestTimer("trasa_49", 7000L, this.warlord5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_49")) && (this.warlord5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_49", 7000L, this.warlord5, null);
      }
      else
      {
        this.warlord5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord5_6);
        this.warlord5.setRunning();
        startQuestTimer("trasa_50", 7000L, this.warlord5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_50")) && (this.warlord5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_50", 7000L, this.warlord5, null);
      }
      else
      {
        this.warlord5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord5_7);
        this.warlord5.setRunning();
        startQuestTimer("trasa_51", 7000L, this.warlord5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_51")) && (this.warlord5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_51", 7000L, this.warlord5, null);
      }
      else
      {
        this.warlord5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord5_8);
        this.warlord5.setRunning();
        startQuestTimer("trasa_52", 7000L, this.warlord5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_52")) && (this.warlord5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_52", 7000L, this.warlord5, null);
      }
      else
      {
        this.warlord5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord5_9);
        this.warlord5.setRunning();
        startQuestTimer("trasa_53", 7000L, this.warlord5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_53")) && (this.warlord5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_53", 7000L, this.warlord5, null);
      }
      else
      {
        this.warlord5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord5_10);
        this.warlord5.setRunning();
        startQuestTimer("trasa_54", 7000L, this.warlord5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_54")) && (this.warlord5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_54", 7000L, this.warlord5, null);
      }
      else
      {
        this.warlord5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord5_11);
        this.warlord5.setRunning();
        startQuestTimer("trasa_55", 4000L, this.warlord5, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_55")) && (this.warlord5 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_55", 7000L, this.warlord5, null);
      }
      else
      {
        this.warlord5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord5_12);
        this.warlord5.setRunning();
        startQuestTimer("trasa_nova5", 7000L, this.warlord5, null);
      }
    }
    else if ((event.equalsIgnoreCase("warlord6_spawn")) && (this.warlord6 == null))
    {
      this.warlord6 = addSpawn(WARLORD, 147606, 116500, -3704, 0, false, 0L);
      this.warlord6.setIsNoRndWalk(true);
      this.warlord6.setRunning();
      startQuestTimer("trasa_56", 7000L, this.warlord6, null);
    }
    else if ((event.equalsIgnoreCase("trasa_nova6")) && (this.warlord6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_nova6", 7000L, this.warlord6, null);
      }
      else
      {
        this.warlord6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord6_1);
        this.warlord6.setRunning();
        startQuestTimer("trasa_56", 7000L, this.warlord6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_56")) && (this.warlord6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_56", 7000L, this.warlord6, null);
      }
      else
      {
        if (!this.warlord6.isInsideRadius(warlord6_1.x, warlord6_1.y, warlord6_1.z, 100, true, false))
          this.warlord6.teleToLocation(warlord6_1.x, warlord6_1.y, warlord6_1.z);
        this.warlord6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord6_2);
        this.warlord6.setRunning();
        startQuestTimer("trasa_57", 7000L, this.warlord6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_57")) && (this.warlord6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_57", 7000L, this.warlord6, null);
      }
      else
      {
        this.warlord6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord6_3);
        this.warlord6.setRunning();
        startQuestTimer("trasa_58", 7000L, this.warlord6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_58")) && (this.warlord6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_58", 7000L, this.warlord6, null);
      }
      else
      {
        this.warlord6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord6_4);
        this.warlord6.setRunning();
        startQuestTimer("trasa_59", 7000L, this.warlord6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_59")) && (this.warlord6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_59", 7000L, this.warlord6, null);
      }
      else
      {
        this.warlord6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord6_5);
        this.warlord6.setRunning();
        startQuestTimer("trasa_60", 7000L, this.warlord6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_60")) && (this.warlord6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_60", 7000L, this.warlord6, null);
      }
      else
      {
        this.warlord6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord6_6);
        this.warlord6.setRunning();
        startQuestTimer("trasa_61", 7000L, this.warlord6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_61")) && (this.warlord6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_61", 7000L, this.warlord6, null);
      }
      else
      {
        this.warlord6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord6_7);
        this.warlord6.setRunning();
        startQuestTimer("trasa_62", 7000L, this.warlord6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_62")) && (this.warlord6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_62", 7000L, this.warlord6, null);
      }
      else
      {
        this.warlord6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord6_8);
        this.warlord6.setRunning();
        startQuestTimer("trasa_63", 7000L, this.warlord6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_63")) && (this.warlord6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_63", 7000L, this.warlord6, null);
      }
      else
      {
        this.warlord6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord6_9);
        this.warlord6.setRunning();
        startQuestTimer("trasa_64", 7000L, this.warlord6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_64")) && (this.warlord6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_64", 7000L, this.warlord6, null);
      }
      else
      {
        this.warlord6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord6_10);
        this.warlord6.setRunning();
        startQuestTimer("trasa_65", 7000L, this.warlord6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_65")) && (this.warlord6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_65", 7000L, this.warlord6, null);
      }
      else
      {
        this.warlord6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord6_11);
        this.warlord6.setRunning();
        startQuestTimer("trasa_66", 4000L, this.warlord6, null);
      }
    }
    else if ((event.equalsIgnoreCase("trasa_66")) && (this.warlord6 != null))
    {
      if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
      {
        startQuestTimer("trasa_66", 7000L, this.warlord6, null);
      }
      else
      {
        this.warlord6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, warlord6_12);
        this.warlord6.setRunning();
        startQuestTimer("trasa_nova6", 7000L, this.warlord6, null);
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
    if (npc.getNpcId() == WARLORD)
    {
      if (npc == this.warlord1)
      {
        startQuestTimer("warlord1_spawn", 300000L, null, null);
        this.warlord1 = null;
      }

      if (npc == this.warlord2)
      {
        startQuestTimer("warlord2_spawn", 300000L, null, null);
        this.warlord2 = null;
      }

      if (npc == this.warlord3)
      {
        startQuestTimer("warlord3_spawn", 300000L, null, null);
        this.warlord3 = null;
      }

      if (npc == this.warlord4)
      {
        startQuestTimer("warlord4_spawn", 300000L, null, null);
        this.warlord4 = null;
      }

      if (npc == this.warlord5)
      {
        startQuestTimer("warlord5_spawn", 300000L, null, null);
        this.warlord5 = null;
      }

      if (npc == this.warlord6)
      {
        startQuestTimer("warlord6_spawn", 300000L, null, null);
        this.warlord6 = null;
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
    new DrakeWarrior(-1, "DrakeWarrior", "ai");
  }
}
