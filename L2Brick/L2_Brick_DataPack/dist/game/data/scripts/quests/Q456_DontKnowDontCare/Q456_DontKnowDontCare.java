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
package quests.Q456_DontKnowDontCare;
 
import l2.brick.gameserver.model.Location;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;
import l2.brick.gameserver.model.quest.State;
import l2.brick.gameserver.util.Util;
import l2.brick.util.Rnd;
 
public class Q456_DontKnowDontCare extends Quest
{
        private static final String qn = "Q456_DontKnowDontCare";
        private static final int[] SEPARATED_SOUL =
        {
                32864, 32865, 32866, 32867, 32868, 32869, 32870, 32891
        };
       
        // Item's
        private static final int DRAKE_LORD_ESSENCE = 17251;
        private static final int BEHEMOTH_LEADER_ESSENCE = 17252;
        private static final int DRAGON_BEAST_ESSENCE = 17253;
       
        // Raidboss
        private static final int DRAKE_LEADER = 25725;
        private static final int BEHEMOTH_LEADER = 25726;
        private static final int DRAGON_BEAST = 25727;
       
        // Raid Npcs
        private static final int DRAKE_LEADER_NPC = 32884;
        private static final int BEHEMOTH_LEADER_NPC = 32885;
        private static final int DRAGON_BEAST_NPC = 32886;
       
        // Reward
        private static final int[][] REWARD =
        {
                {
                        15558, 15559, 15560, 15561, 15562, 15563, 15564, 15565, 15566, 15567, 15567, 15569, 15570, 15571
                },
                {
                        15750, 15753, 15756, 15745, 15748, 15751, 15754, 15757, 15759, 15743, 15746, 15749, 15752, 15755, 15758, 15744, 15747
                },
                {
                        15765, 15764, 15763
                },
                {
                        9552, 9553, 9554, 9555, 9557, 9556, 6577, 6578, 959, 2134
                }
        };
       
        @Override
        public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
        {
                String htmltext = event;
                QuestState st = player.getQuestState(qn);
               
                if (st == null)
                        return htmltext;
               
                if (event.equalsIgnoreCase("accept"))
                {
                        st.setState(State.STARTED);
                        st.set("cond", "1");
                        st.playSound("ItemSound.quest_accept");
                        htmltext = "DontKnowDontCare-07.htm";
                }
                else if (event.equalsIgnoreCase("reward"))
                {
                        switch (npc.getNpcId())
                        {
                                case DRAKE_LEADER_NPC:
                                        if (st.hasQuestItems(DRAKE_LORD_ESSENCE))
                                                player.sendMessage("You already have this Essence");
                                        else
                                        {
                                                st.playSound("ItemSound.quest_itemget");
                                                st.giveItems(DRAKE_LORD_ESSENCE, 1);
                                        }
                                        break;
                                case BEHEMOTH_LEADER_NPC:
                                        if (st.hasQuestItems(BEHEMOTH_LEADER_ESSENCE))
                                                player.sendMessage("You already have this Essence");
                                        else
                                        {
                                                st.playSound("ItemSound.quest_itemget");
                                                st.giveItems(BEHEMOTH_LEADER_ESSENCE, 1);
                                        }
                                        break;
                                case DRAGON_BEAST_NPC:
                                        if (st.hasQuestItems(DRAGON_BEAST_ESSENCE))
                                                player.sendMessage("You already have this Essence");
                                        else
                                        {
                                                st.playSound("ItemSound.quest_itemget");
                                                st.giveItems(DRAGON_BEAST_ESSENCE, 1);
                                        }
                                        break;
                        }
                       
                        if (st.hasQuestItems(BEHEMOTH_LEADER_ESSENCE) && st.hasQuestItems(DRAGON_BEAST_ESSENCE) && st.hasQuestItems(DRAKE_LORD_ESSENCE))
                        {
                                st.playSound("ItemSound.quest_middle");
                                st.set("cond", "2");
                        }
                        htmltext = null;
                }
                return htmltext;
        }
       
        @Override
        public String onTalk(L2Npc npc, L2PcInstance player)
        {
                String htmltext = getNoQuestMsg(player);
                QuestState st = player.getQuestState(qn);
               
                if (st == null)
                        return htmltext;
               
                if (Util.contains(new int[]     {BEHEMOTH_LEADER_NPC, DRAGON_BEAST_NPC, DRAKE_LEADER_NPC}, npc.getNpcId()))
                {
                        if (st.getInt("cond") == 1)
                                htmltext = "takereward.htm";
                        else
                                htmltext = "notakereward.htm";
                }
                else if (Util.contains(SEPARATED_SOUL, npc.getNpcId()))
                {
                        switch (st.getState())
                        {
                                case State.CREATED:
                                        if (player.getLevel() >= 80)
                                                htmltext = "DontKnowDontCare-01.htm";
                                        else
                                                htmltext = "DontKnowDontCare-03.htm";
                                        break;
                                case State.STARTED:
                                        if (st.getInt("cond") == 1)
                                                htmltext = "DontKnowDontCare-08.htm";
                                        else if (st.getInt("cond") == 2)
                                        {
                                                st.playSound("ItemSound.quest_finish");
                                                st.takeItems(DRAKE_LORD_ESSENCE, 1);
                                                st.takeItems(BEHEMOTH_LEADER_ESSENCE, 1);
                                                st.takeItems(DRAGON_BEAST_ESSENCE, 1);
                                                rewardPlayer(player);
                                                htmltext = "DontKnowDontCare-10.htm";
                                                st.unset("cond");
                                                st.exitQuest(false);
                                        }
                                        else
                                                htmltext = "DontKnowDontCare-09.htm";
                                        break;
                                case State.COMPLETED:
                                        htmltext = "DontKnowDontCare-02.htm";
                                        break;
                        }
                }
                return htmltext;
        }
       
        private void rewardPlayer(L2PcInstance player)
        {
                QuestState st = player.getQuestState(qn);
               
                int itemId = 0, count = 1, random = Rnd.get(100);
               
                if (random < 10)
                        itemId = REWARD[0][Rnd.get(REWARD[0].length)];
                else if (random < 30)
                        itemId = REWARD[1][Rnd.get(REWARD[1].length)];
                else if (random < 50)
                        itemId = REWARD[2][Rnd.get(REWARD[2].length)];
                else
                {
                        itemId = REWARD[3][Rnd.get(REWARD[3].length)];
                        count = Rnd.get(1, 2);
                }
                st.giveItems(itemId, count);
        }
       
        @Override
        public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
        {
                QuestState st = player.getQuestState(qn);
               
                if (st == null)
                        return null;
               
                if (st.getInt("cond") == 1)
                {
                        Location loc = npc.getLocation();
                       
                        switch (npc.getNpcId())
                        {
                                case DRAKE_LEADER:
                                        addSpawn(DRAKE_LEADER_NPC, loc, false, 60000, true);
                                        npc.broadcastNpcSay("Take you reward stranger!");
                                        break;
                                case BEHEMOTH_LEADER:
                                        addSpawn(BEHEMOTH_LEADER_NPC, loc, false, 60000, true);
                                        npc.broadcastNpcSay("Take you reward stranger!");
                                        break;
                                case DRAGON_BEAST:
                                        addSpawn(DRAGON_BEAST_NPC, loc, false, 60000, true);
                                        npc.broadcastNpcSay("Take you reward stranger!");
                                        break;
                        }
                }
                return null;
        }
       
        public Q456_DontKnowDontCare(int questId, String name, String descr)
        {
                super(questId, name, descr);
                for (int npc : SEPARATED_SOUL)
                {
                        addStartNpc(npc);
                        addTalkId(npc);
                }
                addTalkId(DRAKE_LEADER_NPC, BEHEMOTH_LEADER_NPC, DRAGON_BEAST_NPC);
                addKillId(DRAKE_LEADER, BEHEMOTH_LEADER, DRAGON_BEAST);
        }
       
        public static void main(String[] args)
        {
                new Q456_DontKnowDontCare(456, qn, "Dont Know, Dont Care");
        }
}
