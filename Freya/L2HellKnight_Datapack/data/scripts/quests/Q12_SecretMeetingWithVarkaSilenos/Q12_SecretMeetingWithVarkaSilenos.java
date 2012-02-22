package quests.Q12_SecretMeetingWithVarkaSilenos;

import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;

/**
 * @author l2.hellknightTeam
 */
public class Q12_SecretMeetingWithVarkaSilenos extends Quest
{
	private static final int CADMON = 31296;
	private static final int HELMUT = 31258;
	private static final int NARAN_ASHANUK = 31378;

	private static final int MUNITIONS_BOX = 7232;

	public Q12_SecretMeetingWithVarkaSilenos(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(CADMON);
		addTalkId(CADMON);
		addTalkId(HELMUT);
		addTalkId(NARAN_ASHANUK);
		questItemIds = new int[] {MUNITIONS_BOX};
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		if (st == null)
			return null;
		
		if (event.equalsIgnoreCase("31296-03.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("31258-02.htm"))
		{
			st.giveItems(MUNITIONS_BOX, 1);
			st.set("cond", "2");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("31378-02.htm"))
		{
			st.takeItems(MUNITIONS_BOX, 1);
			st.addExpAndSp(233125, 18142);
			st.unset("cond");
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(false);
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
				if (npcId == CADMON)
				{
					if (qs.getPlayer().getLevel() >= 74)
						html = "31296-01.htm";
					else
					{
						html = "31296-02.htm";
						qs.exitQuest(true);
					}
				}
				break;
			case State.STARTED:
				switch (npcId)
				{
					case CADMON:
						switch (cond)
						{
							case 1:
								html = "31296-04.htm";
								break;
						}
						break;
					case HELMUT:
						switch (cond)
						{
							case 1:
								html = "31258-01.htm";
								break;
							case 2:
								html = "31258-03.htm";
								break;							
						}
						break;
					case NARAN_ASHANUK:
						switch (cond)
						{
							case 2:
								if (qs.getQuestItemsCount(MUNITIONS_BOX) > 0)
									html = "31378-01.htm";
								break;
						}
						break;
				}
				break;
		}
		return html;
	}

	public static void main(String[] args)
	{
		new Q12_SecretMeetingWithVarkaSilenos(12, "12_SecretMeetingWithVarkaSilenos", "Secret Meeting With Varka Silenos");    	
	}
}
