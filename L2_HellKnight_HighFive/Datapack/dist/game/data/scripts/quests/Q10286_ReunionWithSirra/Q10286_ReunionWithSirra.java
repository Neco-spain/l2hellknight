package quests.Q10286_ReunionWithSirra;

import l2.hellknight.gameserver.instancemanager.InstanceManager;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.entity.Instance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;

public class Q10286_ReunionWithSirra extends Quest
{
	private static final String qn = "10286_ReunionWithSirra";

	// NPC's
	private static final int _rafforty = 32020;
	private static final int _jinia = 32760;
	private static final int _sirra = 32762;
	private static final int _jinia2 = 32781;
	
	private static final int _blackCore = 15470;

	public Q10286_ReunionWithSirra(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(_rafforty);
		addTalkId(_rafforty);
		addFirstTalkId(_rafforty);
		addTalkId(_jinia);
		addTalkId(_jinia2);
		addTalkId(_sirra);
	}

  @Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
			return htmltext;

		if (npc.getNpcId() == _rafforty)
		{
			if (event.equalsIgnoreCase("32020-04.htm"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.set("progress", "1");
				st.playSound("ItemSound.quest_accept");
			}
			
			else if (event.equalsIgnoreCase("32020-05.htm") && st.getInt("progress") == 1)
				st.set("Ex", "0");
		}
		
		else if (npc.getNpcId() == _jinia)
		{
			if (event.equalsIgnoreCase("32760-06.htm"))
			{
				addSpawn(_sirra, -23905,-8790,-5384,56238, false, 0, false, npc.getInstanceId());
				st.set("Ex", "1");
				st.set("cond", "3");
				st.playSound("ItemSound.quest_middle");
			}
			
			else if (event.equalsIgnoreCase("32760-09.htm") && st.getInt("progress") == 1 && st.getInt("Ex") == 2)
			{
				st.set("progress", "2");
				// destroy instance after 1 min
				Instance inst = InstanceManager.getInstance().getInstance(npc.getInstanceId());
				inst.setDuration(60000);
				inst.setEmptyDestroyTime(0);
			}
		}

		else if (npc.getNpcId() == _sirra)
		{
			if (event.equalsIgnoreCase("32762-04.htm") && st.getInt("progress") == 1 && st.getInt("Ex") == 1)
			{
				if (st.getQuestItemsCount(_blackCore) == 0)
					st.giveItems(_blackCore, 5);

				st.set("Ex", "2");
				st.set("cond", "4");
				st.playSound("ItemSound.quest_middle");
			}
		}

		return htmltext;		
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		QuestState _prev = player.getQuestState("10285_MeetingSirra");

		if (npc.getNpcId() == _rafforty && _prev != null && _prev.getState() == State.COMPLETED && st == null && player.getLevel() >= 82)
			return "32020-00.htm";
		npc.showChatWindow(player);
		
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
			return htmltext;

		if (npc.getNpcId() == _rafforty)
		{
			switch (st.getState())
			{
				case State.CREATED:
					QuestState _prev = player.getQuestState("10285_MeetingSirra");
					if (_prev != null && _prev.getState() == State.COMPLETED && player.getLevel() >= 82)
						htmltext = "32020-01.htm";
					else
						htmltext = "32020-03.htm";
					break;
				case State.STARTED:
					if (st.getInt("progress") == 1)
						htmltext = "32020-06.htm";
					else if (st.getInt("progress") == 2)
						htmltext = "32020-09.htm";
					break;
				case State.COMPLETED:
					htmltext = "32020-02.htm";
					break;
			}
		}

		else if (npc.getNpcId() == _jinia && st.getInt("progress") == 1)
		{
			switch (st.getInt("Ex"))
			{
				case 0:
					return "32760-01.htm";
				case 1:
					return "32760-07.htm";
				case 2:
					return "32760-08.htm";
			}
		}

		else if (npc.getNpcId() == _sirra && st.getInt("progress") == 1)
		{
			switch (st.getInt("Ex"))
			{
				case 1:
					return "32762-01.htm";
				case 2:
					return "32762-05.htm";
			}
		}
		
		else if (npc.getNpcId() == _jinia2 && st.getInt("progress") == 2)
		{
			htmltext = "32781-01.htm";
		}
		
		else if (npc.getNpcId() == _jinia2)
		{
			if (st.getInt("progress") == 2)
			{
				htmltext = "32781-01.htm";
			}
			else if (st.getInt("progress") == 3)
			{
				st.addExpAndSp(2152200, 181070);
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(false);
				htmltext = "32781-08.htm";
			}
		}
		return htmltext;
	}
	
		
	public static void main(String[] args)
	{
		new Q10286_ReunionWithSirra(10286, qn, "Reunion With Sirra");
	}
}