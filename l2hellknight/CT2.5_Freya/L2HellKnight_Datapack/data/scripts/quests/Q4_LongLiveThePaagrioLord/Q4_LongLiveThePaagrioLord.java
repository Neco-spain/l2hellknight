package quests.Q4_LongLiveThePaagrioLord;

import l2.hellknight.Config;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;

/**
 * @author l2.hellknightTeam
 */
public class Q4_LongLiveThePaagrioLord extends Quest
{
	// Tataru Zu Hestui; Varkees; Grookin; Uska; Kunai; Gantaki Zu Urutu
	// Honey Khandar; Bear Fur Cloak; Bloody Axe; Ancestor Skull; Spider Dust; Deep Sea Orb
	private final static int QUEST_GIFTS[][] = 
	{ 
		{ 30585, 30566, 30562, 30560, 30559, 30587 }, 
		{ 1541, 1542, 1543, 1544, 1545, 1546 } 
	};

	private final static int NAKUSIN = 30578;
	
	// Adena; Club
	private final static int QUEST_REWARD[] = { 57, 4 };

	public Q4_LongLiveThePaagrioLord(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(NAKUSIN);
		addTalkId(NAKUSIN);
		for (int npcId : QUEST_GIFTS[0])
			addTalkId(npcId);
		questItemIds = QUEST_GIFTS[1];
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if (qs == null)
			return null;
		
		if (event.equalsIgnoreCase("30578-03.htm"))
		{
			qs.set("cond","1");
			qs.set("id","1");
			qs.setState(State.STARTED);
			qs.playSound("ItemSound.quest_accept");
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
				if (npcId == NAKUSIN)
				{
					if (player.getRace().ordinal() != 3)
					{
						html = "30578-00.htm";
						qs.exitQuest(true);
					}
					else if (player.getLevel() >= 2)
					{
						html = "30578-02.htm";
					}
					else
					{
						html = "30578-01.htm";
						qs.exitQuest(true);
					}
				}
				break;
			case State.STARTED:
				switch (cond)
				{
					case 1:
						if (npcId == NAKUSIN)
							html = "30578-04.htm";
						else if (npcId == checkNpc(npcId))
						{
							int index = 0;
							for (int i = 0; i < QUEST_GIFTS[1].length; i++)
							{
								if (QUEST_GIFTS[0][i] == checkNpc(npcId))
								{
									index = i;
									break;
								}
							}
							
							if (qs.getQuestItemsCount(QUEST_GIFTS[1][index]) > 0)
								html = String.valueOf(npcId).concat("-02.htm");
							else
							{
								qs.giveItems(QUEST_GIFTS[1][index], 1);
								html = String.valueOf(npcId).concat("-01.htm");

								int count = 0;
								for (int item : QUEST_GIFTS[1])
								{
									if (qs.getQuestItemsCount(item) > 0)
										count += 1;
								}	
								
								if (count == 6)
								{
									qs.set("cond", "2");
									qs.set("id", "2");
									qs.playSound("ItemSound.quest_middle");
								}
								else
									qs.playSound("ItemSound.quest_itemget");
							}
						}
						break;
					case 2:
						if (npcId == NAKUSIN)
						{
							html = "30578-06.htm";
							qs.giveItems(QUEST_REWARD[1], (long) Config.RATE_QUEST_REWARD);
							qs.giveItems(QUEST_REWARD[0], Math.round(1850 * Config.RATE_QUEST_REWARD_ADENA));
							qs.addExpAndSp(4254,335);
							for (int item : QUEST_GIFTS[1])
								qs.takeItems(item, -1);
							qs.unset("cond");
							qs.exitQuest(false);
							qs.playSound("ItemSound.quest_finish");
						}
						break;
				}
				break;
		}
		return html;		
	}
	
	private int checkNpc(int npcId)
	{
		for (int id : QUEST_GIFTS[0])
		{
			if (id == npcId)
				return id;
		}		
		return 0;
	}
	
	public static void main(String[] args)
	{
		new Q4_LongLiveThePaagrioLord(4, "4_LongLiveThePaagrioLord", "Long Live the Paagrio Lord");
	}
}
