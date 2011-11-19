package quests.Q690_JudeRequest;

import l2.brick.Config;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;
import l2.brick.gameserver.model.quest.State;
import l2.brick.util.Rnd;

import java.util.Map;
import javolution.util.FastMap;

/**
 * Jude's Request (690)
 * @author malyelfik
 */

public class Q690_JudeRequest extends Quest
{
	private static final String qn = "690_JudeRequest";
	// NPC
	private static final int JUDE = 32356;
	private static final int LESSER_EVIL = 22398;
	private static final int GREATER_EVIL = 22399;

	// Item
	private static final int EVIL_WEAPON = 10327;
	
	private static final int[][] REWARDS = 
	{
		{ 10373, 10374, 10375, 10376, 10377, 10378, 10379, 10380, 10381 },
		{ 10397, 10398, 10399, 10400, 10401, 10402, 10403, 10404, 10405 }		
	};

	private static final Map<Integer, Integer> REWARD_CHANCES = new FastMap<Integer, Integer>();
	
	static
	{
		REWARD_CHANCES.put(LESSER_EVIL, 173);
		REWARD_CHANCES.put(GREATER_EVIL, 246);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
			return getNoQuestMsg(player);
		
		if (event.equalsIgnoreCase("32356-03.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("32356-07.htm"))
		{
			if (st.getQuestItemsCount(EVIL_WEAPON) >= 200)
			{
				st.giveItems(REWARDS[0][Rnd.get(REWARDS[0].length)], 1);
				st.takeItems(EVIL_WEAPON, 200);
				st.playSound("ItemSound.quest_middle");
				htmltext = "32356-07.htm";
			}
			else
			{
				htmltext = "32356-07a.htm";
			}
		}
		else if (event.equalsIgnoreCase("32356-08.htm"))
		{
			st.takeItems(EVIL_WEAPON, -1);
			st.exitQuest(true);
			st.playSound("ItemSound.quest_finish");
		}
		else if (event.equalsIgnoreCase("32356-09.htm"))
		{
			if (st.getQuestItemsCount(EVIL_WEAPON) >= 5)
			{
				st.giveItems(REWARDS[1][Rnd.get(REWARDS[1].length)], 1);				
				st.takeItems(EVIL_WEAPON, 5);
				st.playSound("ItemSound.quest_middle");
				htmltext = "32356-09.htm";
			}
			else
			{
				htmltext = "32356-09a.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 78)
					htmltext = "32356-01.htm";
				else
					htmltext = "32356-02.htm";
				break;
			case State.STARTED:
				if (st.getQuestItemsCount(EVIL_WEAPON) >= 200)
					htmltext = "32356-04.htm";
				else if (st.getQuestItemsCount(EVIL_WEAPON) < 5)
					htmltext = "32356-05a.htm";
				else
					htmltext = "32356-05.htm";
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		L2PcInstance partyMember = getRandomPartyMember(player, "1");
		if (partyMember == null)
			return null;
		final QuestState st = partyMember.getQuestState(qn);
		
		int chance = (int)(Config.RATE_QUEST_DROP * REWARD_CHANCES.get(npc.getNpcId()));
		int numItems = Math.max((int) (chance / 1000), 1);
		chance = chance % 1000;
		if (Rnd.get(1000) <= chance)
			{
				st.giveItems(EVIL_WEAPON, numItems);
				st.playSound("ItemSound.quest_itemget"); 
			}
		
		return null;
	}
	
	public Q690_JudeRequest(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(JUDE);
		addTalkId(JUDE);
		addKillId(LESSER_EVIL);
		addKillId(GREATER_EVIL);
	}
	
	public static void main(String[] args)
	{
		new Q690_JudeRequest(690, qn, "Jude's Request");
	}
}
