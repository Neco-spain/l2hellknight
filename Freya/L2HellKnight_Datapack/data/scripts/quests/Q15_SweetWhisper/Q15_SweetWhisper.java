package quests.Q15_SweetWhisper;

import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;

/**
 * @author l2.hellknightTeam
 */
public class Q15_SweetWhisper extends Quest
{
	// NPC
	private static final int VLADIMIR = 31302;
	private static final int HIERARCH = 31517;
	private static final int M_NECROMANCER = 31518;

	public Q15_SweetWhisper(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(VLADIMIR);
		addTalkId(VLADIMIR);
		addTalkId(HIERARCH);
		addTalkId(M_NECROMANCER);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null) 
			return event;
		
		if (event.equalsIgnoreCase("31302-1.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("31518-1.htm"))
		{
			st.set("cond", "2");
		}
		else if (event.equalsIgnoreCase("31517-1.htm"))
		{
			st.addExpAndSp(350531,28204);
			st.playSound("ItemSound.quest_finish");
			st.unset("cond");
			st.setState(State.COMPLETED);
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
		
		final int cond = st.getInt("cond");
		
		switch (st.getState())
		{
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg(player);
				break;
			case State.CREATED:
				if (st.getPlayer().getLevel() >= 60)
					htmltext = "31302-0.htm";
				else
				{
					htmltext = "31302-0a.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				switch (npc.getNpcId())
				{
					case VLADIMIR:
						if (cond >= 1)
							htmltext = "31302-1a.htm";		
						break;
					case M_NECROMANCER:
						switch (cond)
						{
							case 1:
								htmltext = "31518-0.htm";
								break;
							case 2:
								htmltext = "31518-1a.htm";
								break;
						}
						break;
					case HIERARCH:
						if (cond == 2)
							htmltext = "31517-0.htm";
						break;
				}
				break;
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new Q15_SweetWhisper(15, "15_SweetWhisper", "Sweet Whispers");    	
	}
}
