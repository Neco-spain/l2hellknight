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
package quests.Q10505_JewelOfValakas;

import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;

public class Q10505_JewelOfValakas extends Quest
{
        private static final String qn = "Q10505_JewelOfValakas";

        // NPC's
        private static final int KLEIN = 31540;
         private static final int VALAKAS = 21098;

         // Item's
        private static final int EMPTY_CRYSTAL = 21906;
        private static final int FILLED_CRYSTAL_VALAKAS = 21908;
        private static final int VAC_FLOATING_STONE = 7267;
        private static final int JEWEL_OF_VALAKAS = 21896;

        
        @Override
        public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
        {
                String htmltext = event;
                final QuestState st = player.getQuestState(qn);

                if (st == null)
                {
                        return htmltext;
                }
                
                switch (event)
                {
                        case "31540-04.htm":
                                st.set("cond", "1");
                                st.setState(State.STARTED);
                                st.playSound("ItemSound.quest_accept");
                                st.giveItems(EMPTY_CRYSTAL, 1);
                                break;
                        case "31540-07.htm":
                                st.takeItems(FILLED_CRYSTAL_VALAKAS,1);
                                st.giveItems(JEWEL_OF_VALAKAS, 1);
                                st.playSound("ItemSound.quest_finish");
                                st.setState(State.COMPLETED);
                                st.exitQuest(false);
                                break;
                        case "31540-08.htm":
                                st.giveItems(EMPTY_CRYSTAL, 1);
                                break;
                }
                return htmltext;
        }

        @Override
        public String onTalk(L2Npc npc, L2PcInstance player)
        {
                String htmltext = getNoQuestMsg(player);
                QuestState st = player.getQuestState(qn);

                if (st == null)
                {
                        return htmltext;
                }
                
                if(npc.getNpcId() == KLEIN)
                {
                        switch (st.getInt("cond"))
                        {
                        case 0:
                                if(st.getPlayer().getLevel() < 84)
                                        htmltext = "31540-00.htm";
                                else if(st.getQuestItemsCount(VAC_FLOATING_STONE) < 1)
                                        htmltext = "31540-00a.htm";
                                else if(st.isNowAvailable())
                                        htmltext = "31540-01.htm";
                                else
                                        htmltext = "31540-09.htm";
                                break;
                        case 1:
                                if(st.getQuestItemsCount(EMPTY_CRYSTAL) < 1)
                                        htmltext = "31540-08.htm";
                                else
                                        htmltext = "31540-05.htm";
                                break;
                        case 2:
                                if(st.getQuestItemsCount(FILLED_CRYSTAL_VALAKAS) >= 1)
                                        htmltext = "31540-07.htm";
                                else
                                        htmltext = "31540-06.htm";
                                break;
                        }
                }
                return htmltext;
        }

        public String onKill(L2Npc npc, QuestState st)
        {
                if((st.getInt("cond") == 1) && (npc.getNpcId() == VALAKAS))
                {
                        st.takeItems(EMPTY_CRYSTAL,1);
                        st.giveItems(FILLED_CRYSTAL_VALAKAS, 1);
                        st.set("cond", "2");
                }
                return null;
        }
        public Q10505_JewelOfValakas(int questId, String name, String descr)
        {
                super(questId, name, descr);
                addStartNpc(KLEIN);
                addTalkId(KLEIN, EMPTY_CRYSTAL, FILLED_CRYSTAL_VALAKAS);
                addKillId(VALAKAS);
        }
        
        public static void main(String[] args)
        {
                new Q10505_JewelOfValakas(10505, qn, "Jewel Of Valakas");
        }
}