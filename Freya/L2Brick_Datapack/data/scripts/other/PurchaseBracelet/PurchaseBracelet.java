package other.PurchaseBracelet;

import l2.brick.Config;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;

public class PurchaseBracelet extends Quest
{
	private static final String qn = "PurchaseBracelet";
	private static final int Angel_Bracelet = 10320;
	private static final int Devil_Bracelet = 10326;
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
	    QuestState st = player.getQuestState(qn);
	    if (st == null)
	    	return htmltext;
	    if (st.getQuestItemsCount(6471) >= 20 && st.getQuestItemsCount(5094) >= 50 &&
	    		st.getQuestItemsCount(9814) >= 4 && st.getQuestItemsCount(9816) >= 5 &&
	    		st.getQuestItemsCount(9817) >= 5 && st.getQuestItemsCount(9815) >= 3 &&
	    		st.getQuestItemsCount(57) >= 7500000)
	    {
	        st.takeItems(6471,25);
	        st.takeItems(5094,50);
	        st.takeItems(9814,4);
	        st.takeItems(9816,5);
	        st.takeItems(9817,5);
	        st.takeItems(9815,3);
	        st.takeItems(57,7500000);
	        if (event.equalsIgnoreCase("Little_Devil"))
	           st.giveItems(Devil_Bracelet,1);
	        else if (event.equalsIgnoreCase("Little_Angel"))
	           st.giveItems(Angel_Bracelet,1);
	    }
	    else
	        htmltext = "30098-no.htm";
	    st.exitQuest(true);
	    return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
	    QuestState st = player.getQuestState(qn);
	    if (st == null)
	    	st = newQuestState(player);
	    return "30098.htm";
	}

	public PurchaseBracelet(int id, String name, String desc)
	{
		super(id, name, desc);
		addStartNpc(30098);
		addTalkId(30098);
	}

	public static void main(String[] args)
	{
		new PurchaseBracelet(-1, qn, "other");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Other: Purchase Bracelet");
	}
}