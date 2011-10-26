package quests.Q6_StepIntoTheFuture;

import l2.hellknight.Config;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;

/**
 * @author l2.hellknightTeam
 */
public class Q6_StepIntoTheFuture extends Quest
{
	// Roxxy; Baulro; Sir Collin Windawood
	private final static int QUEST_NPC[] = { 30006, 30033, 30311 };
	// Baulro's Letter
	private final static int QUEST_ITEM[] = { 7571 };
	// SoE: Giran; Mark of Traveler
	private final static int QUEST_REWARD[] = { 7559, 7570 };

	public Q6_StepIntoTheFuture(int questId, String name, String descr)
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
		
		if (event.equalsIgnoreCase("30006-03.htm") )
		{
			qs.set("cond", "1");
			qs.setState(State.STARTED);
			qs.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30033-02.htm"))
		{
			qs.set("cond", "2");
			qs.giveItems(QUEST_ITEM[0], 1);
			qs.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30311-03.htm"))
		{
			qs.set("cond", "3");
			qs.takeItems(QUEST_ITEM[0], 1);
			qs.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30006-06.htm"))
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
				html = "<html><body>I can't supply you with another Giran Scroll of Escape. Sorry traveller.</body></html>" ;
				break;
			case State.CREATED:
				if (npcId == QUEST_NPC[0])
				{
		            if (player.getRace().ordinal() == 0 && player.getLevel() >= 3)
		                html = "30006-02.htm";
		            else
		            {
		                html = "30006-01.htm";
		                qs.exitQuest(true);
		            }
				}
				break;
			case State.STARTED:
				switch (cond)
				{
					case 1:
						if (npcId == QUEST_NPC[0])
							html = "30006-04.htm";
						else if (npcId == QUEST_NPC[1])
							html = "30033-01.htm";
						break;
					case 2:
						if (npcId == QUEST_NPC[1])
						{
							if (qs.getQuestItemsCount(QUEST_ITEM[0]) > 0)
								html = "30033-03.htm"; 
						}
						else if (npcId == QUEST_NPC[2])
						{
							if (qs.getQuestItemsCount(QUEST_ITEM[0]) > 0)
								html = "30311-02.htm";
						}
						break;
					case 3:
						if (npcId == QUEST_NPC[0])
							html = "30006-05.htm";
						break;
				}
				break;
		}
		return html;	
	}

	public static void main(String[] args)
	{
		new Q6_StepIntoTheFuture(6, "6_StepIntoTheFuture", "Step into the Future");
	}
}
