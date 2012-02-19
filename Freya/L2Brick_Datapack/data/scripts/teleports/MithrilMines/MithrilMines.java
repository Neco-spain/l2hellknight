package teleports.MithrilMines;

import l2.brick.Config;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;

public class MithrilMines extends Quest
{
	private static final int[][] data =
	{
		{171946, -173352, 3440},
		{175499, -181586, -904},
		{173462, -174011, 3480},
		{179299, -182831, -224},
		{178591, -184615, 360},
		{175499, -181586, -904}
	};
	
	private final static int npcId = 32652;
	
	public MithrilMines(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(npcId);
		addFirstTalkId(npcId);
		addTalkId(npcId);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		
		int loc = Integer.parseInt(event) - 1;
		if (data.length > loc)
		{
			int x = data[loc][0];
			int y = data[loc][1];
			int z = data[loc][2];
			
			player.teleToLocation(x, y, z);
			st.exitQuest(true);
		}
		
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		if (st == null)
			st = newQuestState(player);
		
		if (npc.isInsideRadius(173147, -173762, L2Npc.INTERACTION_DISTANCE, true))
			htmltext = "32652-01.htm";
		else if (npc.isInsideRadius(181941, -174614, L2Npc.INTERACTION_DISTANCE, true))
			htmltext = "32652-02.htm";
		else if (npc.isInsideRadius(179560, -182956, L2Npc.INTERACTION_DISTANCE, true))
			htmltext = "32652-03.htm";
		
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new MithrilMines(-1, "MithrilMines", "teleports");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Teleport: Mithril Mines");
	}
}