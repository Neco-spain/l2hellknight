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
package quests.Q193_SevenSignsDyingMessage;
 
import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.datatables.SkillTable;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2MonsterInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;
import l2.hellknight.gameserver.network.NpcStringId;
import l2.hellknight.gameserver.network.serverpackets.NpcSay;
 
/**
 * @author Plim
 * Update to H5 by pmq (29-09-2011)
 */
 
public class Q193_SevenSignsDyingMessage extends Quest
{
    private static final String qn = "193_SevenSignsDyingMessage";
   
    //NPCs
    private static final int HOLLINT = 30191;
    private static final int CAIN = 32569;
    private static final int ERIC = 32570;
    private static final int ATHEBALDT = 30760;
    private static final int SHILENSEVIL = 27343;
   
    //ITEMS
    private static final int JACOB_NECK = 13814;
    private static final int DEADMANS_HERB = 13816;
    private static final int SCULPTURE = 14353;
   
    private boolean ShilensevilOnSpawn = false;
 
    @Override
    public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    {
        String htmltext = event;
        QuestState st = player.getQuestState(qn);
       
        if (st == null)
            return htmltext;
       
        if (npc.getNpcId() == HOLLINT)
        {
            if (event.equalsIgnoreCase("30191-02.htm"))
            {
                st.setState(State.STARTED);
                st.set("cond", "1");
                st.giveItems(JACOB_NECK, 1);
                st.playSound("ItemSound.quest_accept");
            }
        }
       
        else if (npc.getNpcId() == CAIN)
        {
            if (event.equalsIgnoreCase("32569-05.htm"))
            {
                st.set("cond", "2");
                st.takeItems(JACOB_NECK, 1);
                st.playSound("ItemSound.quest_middle");
            }
           
            else if (event.equalsIgnoreCase("9"))
            {
                st.takeItems(DEADMANS_HERB, 1);
                st.set("cond", "4");
                st.playSound("ItemSound.quest_middle");
                player.showQuestMovie(9);
                return null;
            }
           
            else if (event.equalsIgnoreCase("32569-09.htm"))
            {
                if (ShilensevilOnSpawn)
                    htmltext = "32569-09a.htm";
                else
                {
                    NpcSay packet = new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), NpcStringId.S1_THAT_STRANGER_MUST_BE_DEFEATED_HERE_IS_THE_ULTIMATE_HELP);
                    packet.addStringParameter(player.getName().toString());
                    npc.broadcastPacket(packet);
                    L2MonsterInstance monster = (L2MonsterInstance) addSpawn(SHILENSEVIL, 82624, 47422, -3220, 0, false, 300000, true);
                    monster.broadcastPacket(new NpcSay(monster.getObjectId(), 0, monster.getNpcId(), NpcStringId.YOU_ARE_NOT_THE_OWNER_OF_THAT_ITEM));
                    monster.setRunning();
                    monster.addDamageHate(player, 0, 999);
                    monster.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, st.getPlayer());
                    ShilensevilOnSpawn = true;
                    startQuestTimer("spawnS", 300000, npc, player);
                    startQuestTimer("aiplayer", 20000, npc, player);
                }
            }
            else if (event.equalsIgnoreCase("spawnS"))
            {
                if (ShilensevilOnSpawn)
                {
                    ShilensevilOnSpawn = false;
                    npc.broadcastPacket(new NpcSay(SHILENSEVIL, 0, SHILENSEVIL, NpcStringId.NEXT_TIME_YOU_WILL_NOT_ESCAPE));
                    htmltext = null;
                }
                else
                    htmltext = null;
            }
           
            else if (event.equalsIgnoreCase("aiplayer"))
            {
                if (ShilensevilOnSpawn == true)
                {
                    if (!npc.isInsideRadius(player, 600, true, true))
                    {
                        NpcSay packet = new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), NpcStringId.LOOK_HERE_S1_DONT_FALL_TOO_FAR_BEHIND);
                        packet.addStringParameter(player.getName().toString());
                        npc.broadcastPacket(packet);
                        startQuestTimer("aiplayer", 20000, npc, player);
                        return null;
                    }
					npc.setTarget(player);
					npc.doCast(SkillTable.getInstance().getInfo(1011, 18));  // Guess Skill
					startQuestTimer("aiplayer", 20000, npc, player);
					return null;
                }
				cancelQuestTimer("aiplayer", npc, player);
				return "";
            }
           
            else if (event.equalsIgnoreCase("32569-13.htm"))
            {
                st.set("cond", "6");
                st.takeItems(SCULPTURE, 1);
                st.playSound("ItemSound.quest_middle");
            }
        }
       
        else if (npc.getNpcId() == ERIC)
        {
            if (event.equalsIgnoreCase("32570-02.htm"))
            {
                st.set("cond", "3");
                st.giveItems(DEADMANS_HERB, 1);
                st.playSound("ItemSound.quest_middle");
            }
        }
       
        else if (npc.getNpcId() == ATHEBALDT)
        {
            if (event.equalsIgnoreCase("30760-02.htm"))
            {
                st.addExpAndSp(25000000, 2500000);
                st.unset("cond");
                st.setState(State.COMPLETED);
                st.exitQuest(false);
                st.playSound("ItemSound.quest_finish");
            }
        }
       
        return htmltext;
    }
   
    @Override
    public String onTalk(L2Npc npc, L2PcInstance player)
    {
        String htmltext = getNoQuestMsg(player);
        QuestState st = player.getQuestState(qn);
        QuestState first = player.getQuestState("192_SevenSignsSeriesOfDoubt");
       
        if (st == null)
            return htmltext;
       
        if (npc.getNpcId() == HOLLINT)
        {
            switch (st.getState())
            {
                case State.CREATED :
                    if(first != null && first.getState() == State.COMPLETED && player.getLevel() >= 79)
                        htmltext = "30191-01.htm";
                    else
                    {
                        htmltext = "30191-00.htm";
                        st.exitQuest(true);
                    }
                    break;
               
                case State.STARTED :
                    if (st.getInt("cond") == 1)
                        htmltext = "30191-03.htm";
                    break;
                   
                case State.COMPLETED :
                    htmltext = Quest.getAlreadyCompletedMsg(player);
            }
        }
       
        else if (npc.getNpcId() == CAIN)
        {
            if (st.getState() == State.STARTED)
            {
                switch (st.getInt("cond"))
                {
                    case 1 :
                        htmltext = "32569-01.htm";
                        break;
               
                    case 2 :
                        htmltext = "32569-06.htm";
                        break;
               
                    case 3 :
                        htmltext = "32569-07.htm";
                        break;
                       
                    case 4 :
                        htmltext = "32569-08.htm";
                        break;
                       
                    case 5 :
                        htmltext = "32569-10.htm";
                        break;
                }
            }
        }
       
        else if (npc.getNpcId() == ERIC)
        {
            if (st.getState() == State.STARTED)
            {
                switch (st.getInt("cond"))
                {
                    case 2 :
                        htmltext = "32570-01.htm";
                        break;
                   
                    case 3 :
                        htmltext = "32570-03.htm";
                        break;
                }
            }
        }
       
        else if (npc.getNpcId() == ATHEBALDT)
        {
            if (st.getState() == State.STARTED)
            {
                if (st.getInt("cond") == 6)
                    htmltext = "30760-01.htm";
            }
        }
       
        return htmltext;
    }
   
    @Override
    public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
    {
        QuestState st = player.getQuestState(qn);
       
        if (st == null)
            return super.onKill(npc, player, isPet);
       
        if (npc.getNpcId() == SHILENSEVIL && st.getInt("cond") == 4)
        {

            ShilensevilOnSpawn = false;
            NpcSay packet = new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), NpcStringId.S1_YOU_MAY_HAVE_WON_THIS_TIME_BUT_NEXT_TIME_I_WILL_SURELY_CAPTURE_YOU);
            packet.addStringParameter(player.getName().toString());
            npc.broadcastPacket(packet);
            NpcSay packet1 = new NpcSay(CAIN, 0, CAIN, NpcStringId.WELL_DONE_S1_YOUR_HELP_IS_MUCH_APPRECIATED);
            packet1.addStringParameter(player.getName().toString());
            npc.broadcastPacket(packet1);
            st.giveItems(SCULPTURE, 1);
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
        }
       
        return super.onKill(npc, player, isPet);
    }
   
    public Q193_SevenSignsDyingMessage(int questId, String name, String descr)
    {
        super(questId, name, descr);
       
        addStartNpc(HOLLINT);
        addTalkId(HOLLINT);
        addTalkId(CAIN);
        addTalkId(ERIC);
        addTalkId(ATHEBALDT);
        addKillId(SHILENSEVIL);
       
        questItemIds = new int[]
        { JACOB_NECK, DEADMANS_HERB, SCULPTURE };
    }
   
    public static void main(String[] args)
    {
        new Q193_SevenSignsDyingMessage(193, qn, "Seven Signs, Dying Message");
    }
}