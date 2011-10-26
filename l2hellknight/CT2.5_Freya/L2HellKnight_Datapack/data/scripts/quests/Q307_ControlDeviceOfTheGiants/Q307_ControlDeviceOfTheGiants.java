package quests.Q307_ControlDeviceOfTheGiants;

import l2.hellknight.Config;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;
import l2.hellknight.gameserver.network.serverpackets.RadarControl;

public class Q307_ControlDeviceOfTheGiants extends Quest
{
    // NPC
    private final static int DROPH = 32711;
    // RB
    private final static int GORGOLOS = 25681;
    private final static int LAST_TITAN_UTENUS = 25684;
    private final static int GIANT_MARPANAK = 25680;
    private final static int HEKATON_PRIME = 25687;
    // Itemy
    private final static int SUPPORT_ITEMS = 14850;
    private final static int CET_1_SHEET = 14851;
    private final static int CET_2_SHEET = 14852;
    private final static int CET_3_SHEET = 14853;
    
    private final static int respawnDelay = 3600000; // 1 hour
    
    @Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    {
		QuestState st = player.getQuestState(getName());
		String htmltext = event;
		
		if (st == null) 
			return null;
		
		if (event.equalsIgnoreCase("32711-04.htm"))
        {
			if (st.getPlayer().getLevel() >= 79)
            {
				st.setState(State.STARTED);
				st.set("cond","1");
				st.playSound("ItemSound.quest_accept");
				if (st.getQuestItemsCount(CET_1_SHEET) < 1 || st.getQuestItemsCount(CET_2_SHEET) < 1 || st.getQuestItemsCount(CET_3_SHEET) < 1)
					htmltext = "32711-04.htm";
				else if (st.getQuestItemsCount(CET_1_SHEET) >= 1 && st.getQuestItemsCount(CET_2_SHEET) >= 1 && st.getQuestItemsCount(CET_3_SHEET) >= 1)
					htmltext = "32711-04a.htm";
            }
        }
		else if (event.equalsIgnoreCase("32711-05a.htm"))
		{
			player.sendPacket(new RadarControl(0, 2, 186210, 61479, -4000));
		}
		else if (event.equalsIgnoreCase("32711-05b.htm"))
		{
			player.sendPacket(new RadarControl(0, 2, 187554, 60800, -4979));
		}
		else if (event.equalsIgnoreCase("32711-05c.htm"))
		{
			player.sendPacket(new RadarControl(0, 2, 193585, 54506, -4365));
		}
		// Hekaton Prime spawn
		else if (event.equalsIgnoreCase("spawn"))
		{
			String test = st.getGlobalQuestVar("Hekaton respawn");
			
			if (test.isEmpty())
			{
				st.takeItems(CET_1_SHEET, 1);
				st.takeItems(CET_2_SHEET, 1);
				st.takeItems(CET_3_SHEET, 1);
				addSpawn(HEKATON_PRIME, 191777, 56197, -7624, 1000, false, 0);
				st.set("spawned", "1");
				htmltext = "32711-09.htm";
			}
			else
			{
				long remain = Long.parseLong(test) - System.currentTimeMillis();
				
				if (remain > 0)
					htmltext = "32711-09a.htm";
				else
				{
					st.takeItems(CET_1_SHEET, 1);
					st.takeItems(CET_2_SHEET, 1);
					st.takeItems(CET_3_SHEET, 1);
					addSpawn(HEKATON_PRIME, 191777, 56197, -7624, 1000, false, 0);
					st.set("spawned", "1");
					htmltext = "32711-09.htm";
				}
			}
		}
		return htmltext;
    }

    @Override
	public String onTalk(L2Npc npc, L2PcInstance player)
    {
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(getName());
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 79)
					htmltext = "32711-01.htm";
				else
					htmltext = "32711-02.htm";
		        break;
			case State.STARTED:
				if (st.getInt("spawned") == 1)
					htmltext = "32711-09.htm";
				else if (st.getInt("cond") == 1)
        		{
        			if (st.getQuestItemsCount(CET_1_SHEET) < 1 || st.getQuestItemsCount(CET_2_SHEET) < 1 || st.getQuestItemsCount(CET_3_SHEET) < 1)
        				htmltext = "32711-07.htm";
        			else
        				htmltext = "32711-08.htm";
        		}
        		else if (st.getInt("cond") == 2)
        		{
        			st.giveItems(SUPPORT_ITEMS, 1);
        			st.exitQuest(true);
        			st.playSound("ItemSound.quest_finish");
					htmltext = "32711-10.htm";
        		}
        			
		        break;
		}
		return htmltext;
    }

    @Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
    {
    	L2PcInstance partyMember = getRandomPartyMember(player, "1");
		if (partyMember == null) 
			return null;
    	final QuestState st = partyMember.getQuestState(getName());
		
		switch (npc.getNpcId())
		{
			case GORGOLOS:
			{
				st.giveItems(CET_1_SHEET, 1);
				st.playSound("ItemSound.quest_itemget");
				break;
			}
			case LAST_TITAN_UTENUS:
			{
				st.giveItems(CET_2_SHEET, 1);
				st.playSound("ItemSound.quest_itemget");
				break;
			}
			case GIANT_MARPANAK:
			{
				st.giveItems(CET_3_SHEET,1);
				st.playSound("ItemSound.quest_itemget");
				break;
			}
			case HEKATON_PRIME:
			{
				if (player.getParty() != null)
				{
					for (L2PcInstance party : player.getParty().getPartyMembers())
						rewardPlayer(party);
				}
				else
					rewardPlayer(player);
				break;
			}
		}
		return null;
    }

	private void rewardPlayer(L2PcInstance player)
    {
 		QuestState st = player.getQuestState(getName());
 		
 		if (st.getInt("spawned") == 1)
 		{
 			st.playSound("ItemSound.quest_middle");
 			st.unset("spawned");
 			st.set("cond","2");
 			st.saveGlobalQuestVar("Hekaton respawn",  Long.toString(System.currentTimeMillis() + respawnDelay));
 		}
    }
    
    public Q307_ControlDeviceOfTheGiants(int id, String name, String descr)
    {
        super(id, name, descr);

        addStartNpc(DROPH);
        addTalkId(DROPH);
        addKillId(GORGOLOS);
        addKillId(LAST_TITAN_UTENUS);
        addKillId(GIANT_MARPANAK);
        addKillId(HEKATON_PRIME);
    }
    
    public static void main(String[] args)
    {
        new Q307_ControlDeviceOfTheGiants(307, "Q307_ControlDeviceOfTheGiants", "Control Device Of The Giants");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Quest: Control Device Of The Giants");
    }
}