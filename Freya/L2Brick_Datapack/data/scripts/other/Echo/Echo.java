package other.Echo;

import l2.brick.Config;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;
import l2.brick.gameserver.util.Util;

public class Echo extends Quest
{
    	private static final String qn = "Echo";
    
    	private static final int[] NPCS = {31042, 31043};
    	private static final int ADENA = 57;
    	private static final int COST = 200;
    
    	private static final String[][] LIST = 
    	{
        	{"4410", "4411", "01", "02", "03"},
        	{"4409", "4412", "04", "05", "06"},
        	{"4408", "4413", "07", "08", "09"},
        	{"4420", "4414", "10", "11", "12"},
        	{"4421", "4415", "13", "14", "15"},
        	{"4419", "4417", "16", "05", "06"},
        	{"4418", "4416", "17", "05", "06"}
    	};

    	public Echo(int id, String name, String descr)
    	{
        	super(id, name, descr);

        	for (int i : NPCS)
        	{
            		addStartNpc(i);
            		addTalkId(i);
			addFirstTalkId(i);
        	}
    	}

    	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		if (st == null || !Util.isDigit(event))
			return htmltext;
        
		final int npcId = npc.getNpcId();
        	final int score = Integer.parseInt(event);
        	for (String[] val : LIST)
        	{
            		if (score != Integer.parseInt(val[0]))
            			continue;

            		if (st.getQuestItemsCount(score) == 0)
                		htmltext = npcId + "-" + val[4] + ".htm";
            		else if (st.getQuestItemsCount(ADENA) < COST)
                		htmltext = npcId + "-" + val[3] + ".htm";
            		else
            		{
                		st.takeItems(ADENA, COST);
                		st.giveItems(Integer.parseInt(val[1]), 1);
                		htmltext = npcId + "-" + val[2] + ".htm";
            		}
            		break;
        	}
        	st.exitQuest(true);
        	return htmltext;
    	}

    	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
    	{
		int npcId = npc.getNpcId();

		if(npcId == 31042)
        		return "1.htm";

		if(npcId == 31043) 
			return "2.htm";
		return null;
    	}

    	public static void main(String[] args)
    	{
        	new Echo(-1, qn, "other");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Other: Echo");
    	}
}