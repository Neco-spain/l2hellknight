package quests.Q2_WhatWomenWant;

import l2.hellknight.Config;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;

/**
 * @author l2.hellknightTeam
 */
public class Q2_WhatWomenWant extends Quest
{
	/* Arujien; Mirabel; Herbiel; Greenis */
	private final static int QUEST_NPC[] = { 30223, 30146, 30150, 30157 };
	/* Arujien's Letter; Arujien's Letter; Arujien's Letter; Poetry Book; Greenis's Letter */
	private final static int QUEST_ITEM[] = { 1092, 1093, 1094, 689, 693 };
	/* Adena; Mystic's Earring */
	private final static int QUEST_REWARD[] = { 57, 113 };

	public Q2_WhatWomenWant(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(QUEST_NPC[0]);
		for (int npcId : QUEST_NPC)
		{
			addTalkId(npcId);
		}
		questItemIds = QUEST_ITEM;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{		
		if (qs == null)
			return null;
		
		if (event.equalsIgnoreCase("30223-04.htm"))
		{
			qs.giveItems(QUEST_ITEM[0], 1);
			qs.set("cond", "1");
			qs.setState(State.STARTED);
			qs.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30223-08.htm"))
		{
			qs.takeItems(QUEST_ITEM[2], -1);
			qs.giveItems(QUEST_ITEM[3], 1);
			qs.set("cond", "4");
			qs.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30223-10.htm"))
		{
			qs.takeItems(QUEST_ITEM[2], -1);
			qs.giveItems(QUEST_REWARD[0], 2300 * Math.round(Config.RATE_QUEST_REWARD_ADENA));
			qs.addExpAndSp(4254, 335);
			qs.set("cond", "0");
			qs.exitQuest(false);
			qs.playSound("ItemSound.quest_finish");
			return null;
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
		final int npcId = npc.getNpcId();
		final int cond = qs.getInt("cond");
		
		switch (qs.getState())
		{
			case State.COMPLETED:
				html = getAlreadyCompletedMsg(player);
				break;
			case State.CREATED:
				if (npcId == QUEST_NPC[0])
				{
					if ((player.getRace().ordinal() == 1 || player.getRace().ordinal() == 0) && player.getLevel() >= 2)
						html = "30223-02.htm";
					else
					{
						html = "30223-01.htm";
						qs.exitQuest(true);
					}
				}
				break;
			case State.STARTED:
				if (npcId == QUEST_NPC[0])
				{
					switch (cond)
					{
						case 1:
							if (qs.getQuestItemsCount(QUEST_ITEM[0]) > 0)
								html = "30223-05.htm";
							break;
						case 2:
							if (qs.getQuestItemsCount(QUEST_ITEM[1]) > 0)
								html = "30223-06.htm";
							break;
						case 3:
							if (qs.getQuestItemsCount(QUEST_ITEM[2]) > 0)
								html = "30223-07.htm";
							break;
						case 4:
							if (qs.getQuestItemsCount(QUEST_ITEM[3]) > 0)
								html = "30223-11.htm";
							break;
						case 5:
							if (qs.getQuestItemsCount(QUEST_ITEM[4]) > 0)
							{
								html = "30223-10.htm";
								qs.takeItems(QUEST_ITEM[4], -1);
								qs.giveItems(QUEST_REWARD[0], 1850 * Math.round(Config.RATE_QUEST_REWARD_ADENA));
								qs.giveItems(QUEST_REWARD[1], (long) Config.RATE_QUEST_REWARD);
								qs.addExpAndSp(4254 * Math.round(Config.RATE_QUEST_REWARD_XP), 335 * Math.round(Config.RATE_QUEST_REWARD_SP));
								qs.set("cond", "0");
								qs.exitQuest(false);
								qs.playSound("ItemSound.quest_finish");
							}
							break;
					}
				}
				else if (npcId == QUEST_NPC[1])
				{
					switch (cond)
					{
						case 1:
					         if (qs.getQuestItemsCount(QUEST_ITEM[0]) > 0)
				        	 {
									html = "30146-01.htm";
									qs.takeItems(QUEST_ITEM[0], -1);
									qs.giveItems(QUEST_ITEM[1], 1);
									qs.set("cond", "2");
									qs.playSound("ItemSound.quest_middle");
				        	 }
					         break;
						case 2:
					         if (qs.getQuestItemsCount(QUEST_ITEM[1]) > 0)
					        	 html = "30146-02.htm";
					         break;
					}
				}
				else if (npcId == QUEST_NPC[2])
				{
					switch (cond)
					{
						case 2:
					         if (qs.getQuestItemsCount(QUEST_ITEM[1]) > 0)
				        	 {
					        	 html = "30150-01.htm";
					        	 qs.takeItems(QUEST_ITEM[1], -1);
					        	 qs.giveItems(QUEST_ITEM[2], 1);
					        	 qs.set("cond", "3");
					        	 qs.playSound("ItemSound.quest_middle");
				        	 }
					         break;
						case 3:
					         if (qs.getQuestItemsCount(QUEST_ITEM[2]) > 0)
					        	 html = "30150-02.htm";
					         break;
					}
				}
				else if (npcId == QUEST_NPC[3])
				{
					switch (cond)
					{
						case 4:
					         if (qs.getQuestItemsCount(QUEST_ITEM[3]) > 0)
				        	 {
					        	 html = "30157-01.htm";
					        	 qs.takeItems(QUEST_ITEM[3], -1);
					        	 qs.giveItems(QUEST_ITEM[4], 1);
					        	 qs.set("cond", "5");
					        	 qs.playSound("ItemSound.quest_middle");
				        	 }
					         break;
						case 5:
					         if (qs.getQuestItemsCount(QUEST_ITEM[4]) > 0)
					        	 html = "30157-02.htm";
					         break;
					}
				}
				break;
		}
		return html;
	}

	public static void main(String[] args)
	{
		new Q2_WhatWomenWant(2, "2_WhatWomenWant1", "What Women Want");
	}
}
