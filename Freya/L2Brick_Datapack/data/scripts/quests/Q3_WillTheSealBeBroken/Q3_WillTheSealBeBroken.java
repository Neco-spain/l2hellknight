package quests.Q3_WillTheSealBeBroken;

import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;
import l2.brick.gameserver.model.quest.State;

/**
 *
 * @author l2.brickTeam
 */
public class Q3_WillTheSealBeBroken extends Quest
{
	private final static int TALLOTH = 30141;
	private final static int[] MONSTERS = { 20031, 20041, 20046, 20048, 20052, 20057 };

	private final static int ONYX_BEAST_EYE = 1081;
	private final static int TAINT_STONE = 1082;
	private final static int SUCCUBUS_BLOOD = 1083;
	private final static int SCROLL_ENCHANT_ARMOR_D = 956;

	public Q3_WillTheSealBeBroken(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(TALLOTH);
		addTalkId(TALLOTH);
		for (int mobId : MONSTERS)
			addKillId(mobId);

		questItemIds = new int[]{ONYX_BEAST_EYE, TAINT_STONE, SUCCUBUS_BLOOD};
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null) 
			return event;

		if(event.equalsIgnoreCase("30141-03.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		return event;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		if (npc.getNpcId() == TALLOTH)
			return "";
			
		QuestState qs = player.getQuestState(getName());
		if (qs == null)
			qs = newQuestState(player);
		String html = "";		
		final int cond = qs.getInt("cond");

		switch (qs.getState())
		{
			case State.COMPLETED:
				html = getAlreadyCompletedMsg(player);
				break;
			case State.CREATED:
				if (qs.getPlayer().getRace().ordinal() != 2)
				{
					html = "30141-00.htm";
					qs.exitQuest(true);
				}
				else if (qs.getPlayer().getLevel() >= 16)
				{
					html = "30141-02.htm";
				}
				else
				{
					html = "30141-01.htm";
					qs.exitQuest(true);
				}
				break;
			case State.STARTED:
				switch (cond)
				{
					case 2:
						if (qs.getQuestItemsCount(ONYX_BEAST_EYE) > 0 && qs.getQuestItemsCount(TAINT_STONE) > 0 && qs.getQuestItemsCount(SUCCUBUS_BLOOD) > 0)
						{
							html = "30141-06.htm";
							qs.takeItems(ONYX_BEAST_EYE, 1);
							qs.takeItems(TAINT_STONE, 1);
							qs.takeItems(SUCCUBUS_BLOOD, 1);
							qs.giveItems(SCROLL_ENCHANT_ARMOR_D, 1);
							qs.playSound("ItemSound.quest_finish");
							qs.unset("cond");
							qs.setState(State.COMPLETED);
							qs.exitQuest(false);
						}
						else
							html = "30141-04.htm";
						break;
				}
				break;
		}
		return html;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null) 
			return null;
		
		final int npcId = npc.getNpcId();
		if (st.getState() == State.STARTED && st.getInt("cond") == 1)
		{
			if (npcId == MONSTERS[0])
			{
				if (st.getQuestItemsCount(ONYX_BEAST_EYE) == 0)
				{
					st.giveItems(ONYX_BEAST_EYE, 1);
					st.playSound("Itemsound.quest_itemget");
				}
			}
			else if (npcId == MONSTERS[1] || npcId == MONSTERS[2])
			{
				if (st.getQuestItemsCount(TAINT_STONE) == 0)
				{
					st.giveItems(TAINT_STONE, 1);
					st.playSound("Itemsound.quest_itemget");
				}
			}
			else if (npcId == MONSTERS[3] || npcId == MONSTERS[4] || npcId == MONSTERS[5])
			{
				if (st.getQuestItemsCount(SUCCUBUS_BLOOD) == 0)
				{
					st.giveItems(SUCCUBUS_BLOOD, 1);
					st.playSound("Itemsound.quest_itemget");
				}
			}
			
			if(st.getQuestItemsCount(ONYX_BEAST_EYE) == 1 && st.getQuestItemsCount(TAINT_STONE) == 1 && st.getQuestItemsCount(SUCCUBUS_BLOOD) == 1)
			{
				st.set("cond", "2");
				st.playSound("ItemSound.quest_middle");
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q3_WillTheSealBeBroken(3, "3_WillTheSealBeBroken", "Will the Seal be Broken?");
	}
}
