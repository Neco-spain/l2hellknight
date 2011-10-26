package quests.Q130_PathToHellbound;

import l2.hellknight.Config;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;

public class Q130_PathToHellbound extends Quest
{
    // NPCs
    public static final int GALATE = 32292;
    public static final int CASIAN = 30612;

    // Items
    public static final int CASIAN_BLUE_CRY = 12823;

    public Q130_PathToHellbound (int id, String name, String descr)
    {
        super(id, name, descr);

        addStartNpc(CASIAN);
        addTalkId(CASIAN);
        addTalkId(GALATE);

        questItemIds = new int[] {CASIAN_BLUE_CRY};
    }

    @Override
	public String onEvent (String event, QuestState st)
    {
        if (event.equals("30612-03.htm"))
        {
            st.set("cond","1");
            st.setState(State.STARTED);
        }
        else if (event.equals("32292-03.htm"))
        {
            st.set("cond","2");
        }
        else if (event.equals("30612-05.htm"))
        {
            st.set("cond","3");
            st.giveItems(CASIAN_BLUE_CRY,1);
        }
        else if (event.equals("32292-06.htm"))
        {
            st.takeItems(CASIAN_BLUE_CRY,-1);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        }
        return event;
    }

    @Override
	public String onTalk (L2Npc npc, L2PcInstance player)
    {
		String htmltext = msgNotHaveMinimumRequirements();
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return htmltext;
		
		final int npcId = npc.getNpcId();
		final int cond = st.getInt("cond");
		
		switch (st.getState())
		{
			case State.COMPLETED:
				htmltext = msgQuestCompleted();
				break;
			case State.CREATED:
		        if (npcId == CASIAN)
		        {
	                if (player.getLevel() >= 78)
	                	htmltext = "30612-01.htm";
	                else
	                {
	                    st.exitQuest(true);
	                    htmltext = "30612-00.htm";
	                }
		        }
		        break;
			case State.STARTED:
		        switch (npcId)
		        {
		        	case CASIAN:
		        		switch (cond)
		        		{
		        			case 1:
		        				htmltext = "30612-03a.htm";
		        				break;
		        			case 2:
		        				htmltext = "30612-04.htm";
		        				break;
		        			case 3:
		        				htmltext = "30612-05a.htm";
		        				break;
		        		}
		        		break;
		        	case GALATE:
		        		switch (cond)
		        		{
		        			case 1:
		        				htmltext = "32292-01.htm";
		        				break;
		        			case 2:
		        				htmltext = "32292-03a.htm";
		        				break;
		        			case 3:
		                        if (st.getQuestItemsCount(CASIAN_BLUE_CRY) == 1)
		                        	htmltext = "32292-04.htm";
		                        else
		                        	htmltext = "Incorrect item count";
		        				break;
		        		}
		        		break;		        		
		        }
		        
		        break;
		}
		return htmltext;
    }

    /**
	 * @return
	 */
	private String msgQuestCompleted()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return
	 */
	private String msgNotHaveMinimumRequirements()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public static void main(String[] args)
    {
        new Q130_PathToHellbound(130, "Q130_PathToHellbound", "Path to Hellbound");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Quest: Path to Hellbound");
    }
}
