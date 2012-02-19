package quests.Q14_WhereaboutsOfTheArchaeologist;

import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;
import l2.brick.gameserver.model.quest.State;

/**
 * @author l2.brickTeam
 */
public class Q14_WhereaboutsOfTheArchaeologist extends Quest
{
	// NPC
	private static final int LIESEL = 31263;
	private static final int GHOST_OF_ADVENTURER = 31538;
	// QUEST ITEM
	private static final int LETTER_TO_ARCHAEOLOGIST = 7253;

	public Q14_WhereaboutsOfTheArchaeologist(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(LIESEL);
		addTalkId(LIESEL);
		addTalkId(GHOST_OF_ADVENTURER);
		
		questItemIds = new int[] {LETTER_TO_ARCHAEOLOGIST};
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null) 
			return event;
		
		if (event.equalsIgnoreCase("31263-2.htm"))
		{
			st.set("cond", "1");
			st.giveItems(LETTER_TO_ARCHAEOLOGIST, 1);
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("31538-1.htm"))
		{
			st.takeItems(LETTER_TO_ARCHAEOLOGIST, 1);
			st.addExpAndSp(325881, 32524);
			st.giveAdena(136928, true);
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
				if(st.getPlayer().getLevel() >= 74)
					htmltext = "31263-0.htm";
				else
				{
					htmltext = "31263-1.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				switch (npc.getNpcId())
				{
					case LIESEL:
						if (st.getInt("cond") == 1)
							htmltext = "31263-2.htm";		
						break;
					case GHOST_OF_ADVENTURER:
						if (st.getInt("cond") == 1 && st.getQuestItemsCount(LETTER_TO_ARCHAEOLOGIST) == 1)
							htmltext = "31538-0.htm";
						break;
				}
				break;
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new Q14_WhereaboutsOfTheArchaeologist(14, "14_WhereaboutsOfTheArchaeologist", "Where abouts of the Archaeologist");    	
	}
}
