package other.MissQueen;

import javolution.util.FastList;

import l2.brick.Config;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;

public class MissQueen extends Quest
{
	private static final String qn = "MissQueen";
	
	private static FastList<Integer>npcIds = new FastList<Integer>();
	
	private static final int COUPON_ONE = 7832;
	private static final int COUPON_TWO = 7833;

	private static boolean QUEEN_ENABLED = false;
	
	private static final int NEWBIE_REWARD = 16;
	private static final int TRAVELER_REWARD = 32;
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
	    if (!QUEEN_ENABLED)
	    	return "";
	    QuestState st = player.getQuestState(qn);
	    if (st == null)
	    	return "";
	    int newbie = player.getNewbie();
	    int level = player.getLevel();
	    int occupation_level = player.getClassId().level();
	    int pkkills = player.getPkKills();
	    if (event.equalsIgnoreCase("newbie_give_coupon"))
	    {
	    	if (6 <= level && level <= 25 && pkkills == 0 && occupation_level == 0)
	    	{
	    		if ((newbie | NEWBIE_REWARD) != newbie)
	    		{
	    			player.setNewbie(newbie|NEWBIE_REWARD);
	    			st.giveItems(COUPON_ONE,1);
	    			return "31760-2.htm";
	    		}
	    		else
	    			return "31760-1.htm";
	    	}
	    	else
	    		return "31760-3.htm";
	    }
	    else if (event.equalsIgnoreCase("traveller_give_coupon"))
	    {
	       if (6 <= level && level <= 25 && pkkills  == 0 && occupation_level == 1)
	          if ((newbie | TRAVELER_REWARD) != newbie)
	          {
	             player.setNewbie(newbie|TRAVELER_REWARD);
	             st.giveItems(COUPON_TWO,1);
	             return "31760-5.htm";
	          }
	          else
	             return "31760-4.htm";
	       else
	          return "31760-6.htm";
	    }
	    return "";
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
			st = newQuestState(player);
		return "31760.htm";
	}

	public MissQueen(int id, String name, String desc)
	{
		super(id,name,desc);
		for (int i = 31760; i <= 31767; i++)
		{
			addStartNpc(i);
			addFirstTalkId(i);
		    addTalkId(i);
		    npcIds.add(i);
		}
	}

	public static void main(String[] args)
	{
		new MissQueen(-1,qn,"other");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Other: Miss Queen");
	}
}