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
package quests.Q10295_SevenSignsSolinasTomb;

import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;

/**
 * @author ??, SquiD
 *
 */

public class Q10295_SevenSignsSolinasTomb extends Quest
{
  private static final String qn = "Q10295_SevenSignsSolinasTomb";
  private boolean progress1 = false;
  private boolean progress2 = false;
  private boolean progress3 = false;
  private boolean progress4 = false;
  private static final int EVIL = 32792;
  private static final int ELCARDIA = 32787;
  private static final int SOLINA = 32793;
  private static final int TELEPORT_DEVICE = 32820;
  private static final int ALTAR_OF_HALLOWS_1 = 32857;
  private static final int ALTAR_OF_HALLOWS_2 = 32858;
  private static final int ALTAR_OF_HALLOWS_3 = 32859;
  private static final int ALTAR_OF_HALLOWS_4 = 32860;
  private static final int TELEPORT_DEVICE_2 = 32837;
  private static final int TELEPORT_DEVICE_3 = 32842;
  
  private static final int[] NPCs = { EVIL, ELCARDIA, TELEPORT_DEVICE, ALTAR_OF_HALLOWS_1, ALTAR_OF_HALLOWS_2, ALTAR_OF_HALLOWS_3, ALTAR_OF_HALLOWS_4, TELEPORT_DEVICE_2, TELEPORT_DEVICE_3, SOLINA };

  private static int SCROLL_OF_ABSTINENCE = 17228;
  private static int SHIELD_OF_SACRIFICE = 17229;
  private static int SWORD_OF_HOLYSPIRIT = 17230;
  private static int STAFF_OF_BLESSING = 17231;

  private static final int[] SolinaGuardians = { 18952, 18953, 18954, 18955 };

//  private static final int[][] TELEPORTS = { { 45512, -249832, -6760 }, { 120664, -86968, -3392 }, { 56033, -252944, -6760 }, { 56081, -250391, -6760 }, { 120664, -86968, -3392 } }; // //need finish

  private Q10295_SevenSignsSolinasTomb(int questId, String name, String descr)
  {
    super(questId, name, descr);

    addStartNpc(EVIL);

    for (int id : NPCs) {
      addTalkId(id);
    }
    for (int i : SolinaGuardians)
      addKillId(i);
  }

  @Override
public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
  {
    String htmltext = event;
    QuestState st = player.getQuestState(qn);
    if (st == null) {
      return htmltext;
    }
    if (event.equalsIgnoreCase("32792-05.htm"))
    {
      st.setState(State.STARTED);
      st.set("cond", "1");
      st.playSound("ItemSound.quest_accept");
    }
    else if (event.equalsIgnoreCase("32857-02.htm"))
    {
      if (st.getQuestItemsCount(STAFF_OF_BLESSING) == 0L)
        st.giveItems(STAFF_OF_BLESSING, 1L);
      else
        htmltext = "empty-atlar.htm";
    }
    else if (event.equalsIgnoreCase("32859-02.htm"))
    {
      if (st.getQuestItemsCount(SCROLL_OF_ABSTINENCE) == 0L)
        st.giveItems(SCROLL_OF_ABSTINENCE, 1L);
      else
        htmltext = "empty-atlar.htm";
    }
    else if (event.equalsIgnoreCase("32858-02.htm"))
    {
      if (st.getQuestItemsCount(SWORD_OF_HOLYSPIRIT) == 0L)
        st.giveItems(SWORD_OF_HOLYSPIRIT, 1L);
      else
        htmltext = "empty-atlar.htm";
    }
    else if (event.equalsIgnoreCase("32860-02.htm"))
    {
      if (st.getQuestItemsCount(SHIELD_OF_SACRIFICE) == 0L)
        st.giveItems(SHIELD_OF_SACRIFICE, 1L);
      else
        htmltext = "empty-atlar.htm";
    }
    else if (event.equalsIgnoreCase("32793-04.htm"))
    {
      st.set("cond", "2");
      st.playSound("ItemSound.quest_middle");
    }
    else if (event.equalsIgnoreCase("32793-08.htm"))
    {
      st.set("cond", "3");
      st.playSound("ItemSound.quest_middle");
    }
    return htmltext;
  }

  @Override
public String onTalk(L2Npc npc, L2PcInstance player)
  {
    String htmltext = getNoQuestMsg(player);
    QuestState st = player.getQuestState(qn);
    if (st == null) {
      return htmltext;
    }
    int npcId = npc.getNpcId();
    int cond = st.getInt("cond");
    int ac = st.getInt("active");

    if (st.getPlayer().isSubClassActive()) {
      return "no_subclass-allowed.htm";
    }
    if (st.getState() == 0)
    {
      if (npcId == EVIL)
      {
        QuestState qs = player.getQuestState(qn);
        if (cond == 0)
        {
          if ((player.getLevel() >= 81) && (qs != null) && (qs.isCompleted())) {
            htmltext = "32792-01.htm";
          }
          else {
            htmltext = "32792-00a.htm";
            st.exitQuest(true);
          }
        }
      }
    }
    else if (st.getState() == 1)
    {
      if (npcId == EVIL)
      {
        if (cond == 1)
          htmltext = "32792-06.htm";
        else if (cond == 2)
          htmltext = "32792-07.htm";
        else if (cond == 3)
        {
          if (player.getLevel() >= 81)
          {
            htmltext = "32792-08.htm";
            st.addExpAndSp(125000000, 12500000);
            st.setState(State.COMPLETED);
            st.unset("cond");
            st.unset("active");
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
          }
          else {
            htmltext = "32792-00.htm";
          }
        }
      } else if (npcId == ELCARDIA)
      {
        htmltext = "32787-01.htm";
      }
      else if (npcId == TELEPORT_DEVICE)
      {
        if (ac == 1)
          htmltext = "32820-02.htm";
        else {
          htmltext = "32820-01.htm";
        }
      }
      else if (npcId == TELEPORT_DEVICE_2)
      {
        htmltext = "32837-01.htm";
      }
      else if (npcId == TELEPORT_DEVICE_3)
      {
        htmltext = "32842-01.htm";
      }
      else if (npcId == ALTAR_OF_HALLOWS_1)
      {
        htmltext = "32857-01.htm";
      }
      else if (npcId == ALTAR_OF_HALLOWS_2)
      {
        htmltext = "32858-01.htm";
      }
      else if (npcId == ALTAR_OF_HALLOWS_3)
      {
        htmltext = "32859-01.htm";
      }
      else if (npcId == ALTAR_OF_HALLOWS_4)
      {
        htmltext = "32860-01.htm";
      }
      else if (npcId == SOLINA)
      {
        if (cond == 1)
          htmltext = "32793-01.htm";
        else if (cond == 2)
          htmltext = "32793-04.htm";
        else if (cond == 3)
          htmltext = "32793-08.htm";
      }
    }
    return htmltext;
  }

  @Override
public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
  {
    QuestState st = player.getQuestState(qn);
    if (st == null) {
      return super.onKill(npc, player, isPet);
    }
    int npcId = npc.getNpcId();
    //int ac = st.getInt("active");

	if (ArrayContains(SolinaGuardians, npcId))
    {
      switch (npcId)
      {
      case 18952:
        this.progress1 = true;
        break;
      case 18953:
        this.progress2 = true;
        break;
      case 18954:
        this.progress3 = true;
        break;
      case 18955:
        this.progress4 = true;
      }

      if ((this.progress1) && (this.progress2) && (this.progress3) && (this.progress4))
      {
        player.showQuestMovie(27);
        st.set("active", "1");
      }
    }
    return null;
  }

  public static boolean ArrayContains(int[] paramArrayOfInt, int paramInt)
  {
    for (int k : paramArrayOfInt)
      if (k == paramInt)
        return true;
    return true;
  }
  
  public static void main(String[] args)
  {
    new Q10295_SevenSignsSolinasTomb(10295, qn, "Q10295_SevenSignsSolinasTomb");
  }
}
