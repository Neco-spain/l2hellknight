package quests.Q7_ATripBegins;

import l2.brick.Config;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;
import l2.brick.gameserver.model.quest.State;

/**
 * @author l2.brickTeam
 */
public class Q7_ATripBegins extends Quest
{
	// Mirabel; Ariel; Asterios
	private final static int QUEST_NPC[] = { 30146, 30148, 30154 };
	// Ariel's Recommendation
	private final static int QUEST_ITEM[] = { 7572 };
	// SoE: Giran; Mark of Traveler
	private final static int QUEST_REWARD[] = { 7559, 7570 };

	public Q7_ATripBegins(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(QUEST_NPC[0]);
		for (int npcId : QUEST_NPC)
			addTalkId(npcId);
		questItemIds = QUEST_ITEM;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if (qs == null)
			return null;
		
		if (event.equalsIgnoreCase("30146-03.htm"))
		{
			qs.set("cond", "1");
			qs.setState(State.STARTED);
			qs.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30148-02.htm"))
		{
			qs.set("cond", "2");
			qs.giveItems(QUEST_ITEM[0], 1);
			qs.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30154-02.htm"))
		{
			qs.set("cond", "3");
			qs.takeItems(QUEST_ITEM[0], 1);
			qs.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30146-06.htm"))
		{
			qs.giveItems(QUEST_REWARD[0], (long) Config.RATE_QUEST_REWARD);
			qs.giveItems(QUEST_REWARD[1], 1);
			qs.unset("cond");
			qs.exitQuest(false);
			qs.playSound("ItemSound.quest_finish");
		}
		return event;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState qs = player.getQuestState(getName());
		if (qs == null)
			qs = newQuestState(player);
		String html = "";		
		final int cond = qs.getInt("cond");
		final int npcId = npc.getNpcId();
		
		switch (qs.getState())
		{
			case State.COMPLETED:
				html = getAlreadyCompletedMsg(player);
				break;
			case State.CREATED:
				if (npcId == QUEST_NPC[0])
				{
		            if (player.getRace().ordinal() == 1 && player.getLevel() >= 3)
		                html = "30146-02.htm";
		            else
		            {
		                html = "30146-01.htm";
		                qs.exitQuest(true);
		            }
				}
				break;
			case State.STARTED:
				switch (cond)
				{
					case 1:
						if (npcId == QUEST_NPC[0])
						{
							html = "30146-04.htm";
						}
						else if (npcId == QUEST_NPC[1])
						{
							if (qs.getQuestItemsCount(QUEST_ITEM[0]) == 0)
								html = "30148-01.htm";
						}
						break;
					case 2:
						if (npcId == QUEST_NPC[1])
						{
							html = "30148-03.htm";
						}
						else if (npcId == QUEST_NPC[2])
						{
							if (qs.getQuestItemsCount(QUEST_ITEM[0]) == 0)
								html = "30154-01.htm";
						}
						break;
					case 3:
						if (npcId == QUEST_NPC[0])
						{
							html = "30146-05.htm";
						}
						else if (npcId == QUEST_NPC[2])
						{
							html = "30154-03.htm";
						}
						break;
				}
				break;
		}
		return html;	
	}

	public static void main(String[] args)
	{
		new Q7_ATripBegins(7, "7_ATripBegins", "A Trip Begins");
	}
}
