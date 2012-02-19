package quests.Q13_ParcelDelivery;

import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;
import l2.brick.gameserver.model.quest.State;

/**
 * @author l2.brickTeam
 */
public class Q13_ParcelDelivery extends Quest
{
	//NPC
	private static final int FUNDIN = 31274;
	private static final int VULCAN = 31539;
	private static final int PACKAGE = 7263;

	public Q13_ParcelDelivery(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(FUNDIN);
		addTalkId(FUNDIN);
		addTalkId(VULCAN);
		
		questItemIds = new int[] {PACKAGE};
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null) 
			return event;
		
		if (event.equalsIgnoreCase("31274-2.htm"))
		{
			st.set("cond", "1");
			st.giveItems(PACKAGE, 1);
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("31539-1.htm"))
		{
			st.takeItems(PACKAGE, 1);
			st.addExpAndSp(589092, 58794);
			st.giveAdena(157834, true);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(false);
		}
		return event;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg(player);
				break;
			case State.CREATED:
				if (st.getPlayer().getLevel() >= 74)
					htmltext = "31274-0.htm";
				else
				{
					htmltext = "31274-1.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				switch (npc.getNpcId())
				{
					case FUNDIN:
						if (st.getInt("cond") == 1)
							htmltext = "31274-2.htm";		
						break;
					case VULCAN:
						if (st.getInt("cond") == 1 && st.getQuestItemsCount(PACKAGE) == 1)
							htmltext = "31539-0.htm";	
						break;
				}
				break;
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new Q13_ParcelDelivery(13, "13_ParcelDelivery", "Parcel Delivery");    	
	}
}
