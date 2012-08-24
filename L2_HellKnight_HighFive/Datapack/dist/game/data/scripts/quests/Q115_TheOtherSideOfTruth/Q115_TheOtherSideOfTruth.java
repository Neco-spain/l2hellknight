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
package quests.Q115_TheOtherSideOfTruth;

import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;

/**
 * The Other Side Of Truth (115)
 * @author pmq
 * Update To High Five
 */
public class Q115_TheOtherSideOfTruth extends Quest
{
    private static final String qn = "Q115_TheOtherSideOfTruth";
    // NPCs
    private static int RAFFORTY = 32020;
    private static int MISA = 32018;
    private static int KIERRE = 32022;
    private static int ICE_SCULPTURE1 = 32021;
    private static int ICE_SCULPTURE2 = 32077;
    private static int ICE_SCULPTURE3 = 32078;
    private static int ICE_SCULPTURE4 = 32079;
    // Items
    private static int ADENA = 57;
    // Quest Items
    private static int MISAS_LETTER = 8079;
    private static int RAFFORTYS_LETTER = 8080;
    private static int PIECE_OF_TABLET = 8081;
    private static int REPORT_PIECE = 8082;
   
    @Override
    public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    {
        String htmltext = event;
        QuestState st = player.getQuestState(qn);
       
        if (st == null)
            return htmltext;
       
        if (event.equalsIgnoreCase("32020-02.htm") && st.getState() == State.CREATED)
        {
            st.setState(State.STARTED);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        }
        if (event.equalsIgnoreCase("32020-06.html") || event.equalsIgnoreCase("32020-08a.html"))
        {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        else if (event.equalsIgnoreCase("32020-05.html"))
        {
            st.set("cond", "3");
            st.takeItems(MISAS_LETTER, 1);
            st.playSound("ItemSound.quest_middle");
        }
        else if (event.equalsIgnoreCase("32020-08.html") || event.equalsIgnoreCase("32020-07a.html"))
        {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
        }
        else if (event.equalsIgnoreCase("32020-12.html"))
        {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
        }
        else if (event.equalsIgnoreCase("32018-04.html"))
        {
            st.set("cond", "7");
            st.takeItems(RAFFORTYS_LETTER, 1);
            st.playSound("ItemSound.quest_middle");
        }
        else if (event.equalsIgnoreCase("Sculpture-04a"))
        {
            st.set("cond", "8");
            st.playSound("ItemSound.quest_middle");
            if (st.getInt("32021") == 0 && st.getInt("32077") == 0)
                st.giveItems(PIECE_OF_TABLET, 1);
            htmltext = "Sculpture-04.html";
        }
        else if (event.equalsIgnoreCase("32022-02.html"))
        {
            st.set("cond", "9");
            st.giveItems(REPORT_PIECE, 1);
            st.playSound("ItemSound.quest_middle");
        }
        else if (event.equalsIgnoreCase("32020-16.html"))
        {
            st.set("cond", "10");
            st.takeItems(REPORT_PIECE, 1);
            st.playSound("ItemSound.quest_middle");
        }
        else if (event.equalsIgnoreCase("32020-18.html"))
        {
            if (st.hasQuestItems(PIECE_OF_TABLET))
            {
                st.giveItems(ADENA, 115673);
                st.addExpAndSp(493595, 40442);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(false);
            }
            else
            {
                st.set("cond", "11");
                st.playSound("ItemSound.quest_middle");
                htmltext = "32020-19.html";
            }
        }
        else if (event.equalsIgnoreCase("32020-19.html"))
        {
            st.set("cond", "11");
            st.playSound("ItemSound.quest_middle");
        }
        else if (event.startsWith("32021") || event.startsWith("32077"))
        {
            if (event.contains("-pick"))
            {
                st.set("talk", "1");
                event = event.replace("-pick", "");
            }
            st.set("talk", "1");
            st.set(event, "1");
            htmltext = "Sculpture-05.html";
        }
        return htmltext;
    }
   
    @Override
    public String onTalk(L2Npc npc, L2PcInstance player)
    {
        String htmltext = "<html><body>¥Ø«e¨S¦³°õ¦æ¥ô°È¡A©Î±ø¥ó¤£²Å¡C</body></html>";
        QuestState st = player.getQuestState(qn);
       
        int npcId = npc.getNpcId();
        if (st == null)
            return htmltext;
       
        if (npcId == RAFFORTY)
        {
            switch (st.getState())
            {
                case State.CREATED:
                    if (player.getLevel() >= 53)
                        htmltext = "32020-01.htm";
                    else
                    {
                        htmltext = "32020-00.htm";
                        st.exitQuest(true);
                    }
                    break;
                case State.STARTED:
                    if (st.getInt("cond") == 1)
                        htmltext = "32020-03.html";
                    else if (st.getInt("cond") == 2)
                        htmltext = "32020-04.html";
                    else if (st.getInt("cond") == 3)
                        htmltext = "32020-05.html";
                    else if (st.getInt("cond") == 4)
                        htmltext = "32020-11.html";
                    else if (st.getInt("cond") == 5)
                    {
                        htmltext = "32020-13.html";
                        st.giveItems(RAFFORTYS_LETTER, 1);
                        st.playSound("ItemSound.quest_middle");
                        st.set("cond", "6");
                    }
                    else if (st.getInt("cond") == 6)
                        htmltext = "32020-14.html";
                    else if (st.getInt("cond") == 7 || st.getInt("cond") == 8)
                        htmltext = "32020-14a.html";
                    else if (st.getInt("cond") == 9)
                        htmltext = "32020-15.html";
                    else if (st.getInt("cond") == 10)
                        htmltext = "32020-17.html";
                    else if (st.getInt("cond") == 11)
                        htmltext = "32020-20.html";
                    else if (st.getInt("cond") == 12)
                    {
                        htmltext = "32020-18.html";
                        st.giveItems(ADENA, 115673);
                        st.addExpAndSp(493595, 40442);
                        st.playSound("ItemSound.quest_finish");
                        st.exitQuest(false);
                    }
                    break;
                case State.COMPLETED:
                    htmltext = "<html><body>Congrats!</body></html>";
                    break;
            }
        }
        else if (npcId == MISA && st.getState() == State.STARTED)
        {
            if (st.getInt("cond") == 1)
            {
                htmltext = "32018-01.html";
                st.set("cond", "2");
                st.giveItems(MISAS_LETTER, 1);
                st.playSound("ItemSound.quest_middle");
            }
            else if (st.getInt("cond") == 2)
                htmltext = "32018-02.html";
            else if (st.getInt("cond") == 6)
                htmltext = "32018-03.html";
            else if (st.getInt("cond") == 7)
                htmltext = "32018-05.html";
        }
        else if (npcId == KIERRE && st.getState() == State.STARTED)
        {
            if (st.getInt("cond") == 8)
            {
                htmltext = "32022-02.html";
                st.set("cond", "9");
                st.giveItems(REPORT_PIECE, 1);
                st.playSound("ItemSound.quest_middle");
            }
            else if (st.getInt("cond") >= 9)
                htmltext = "";
        }
        else if ((npcId == ICE_SCULPTURE1 || npcId == ICE_SCULPTURE2 || npcId == ICE_SCULPTURE3 || npcId == ICE_SCULPTURE4) && st.getState() == State.STARTED)
        {
            if (st.getInt("cond") == 7)
            {
                String _npcId = String.valueOf(npcId);
                int npcId_flag = st.getInt(_npcId);
                if (npcId == ICE_SCULPTURE1 || npcId == ICE_SCULPTURE2)
                {
                    int talk_flag = st.getInt("talk");
                    return npcId_flag == 1 ? "Sculpture-02.html" : talk_flag == 1 ? "Sculpture-06.html" : "Sculpture-03-" + _npcId + ".html";
                }
                else if (npcId_flag == 1)
                    htmltext = "Sculpture-02.html";
                else
                {
                    st.set(_npcId, "1");
                    htmltext = "Sculpture-01.html";
                }
            }
            else if (st.getInt("cond") == 8)
                htmltext = "Sculpture-04.html";
            else if (st.getInt("cond") == 11)
            {
                htmltext = "Sculpture-07.html";
                st.set("cond", "12");
                st.giveItems(PIECE_OF_TABLET, 1);
                st.playSound("ItemSound.quest_middle");
            }
            else if (st.getInt("cond") == 12)
                htmltext = "Sculpture-08.html";
        }
        return htmltext;
    }
   
    public Q115_TheOtherSideOfTruth(int questId, String name, String descr)
    {
        super(questId, name, descr);
        addStartNpc(RAFFORTY);
        addTalkId(RAFFORTY);
        addTalkId(MISA);
        addTalkId(KIERRE);
        addTalkId(ICE_SCULPTURE1);
        addTalkId(ICE_SCULPTURE2);
        addTalkId(ICE_SCULPTURE3);
        addTalkId(ICE_SCULPTURE4);
       
        questItemIds = new int[] {MISAS_LETTER, RAFFORTYS_LETTER, PIECE_OF_TABLET, REPORT_PIECE};
    }
   
    public static void main(String[] args)
    {
        new Q115_TheOtherSideOfTruth(115, qn, "The Other Side Of Truth");
    }
} 