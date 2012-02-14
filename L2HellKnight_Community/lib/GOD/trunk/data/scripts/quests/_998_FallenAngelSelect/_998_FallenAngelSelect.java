package quests._998_FallenAngelSelect;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;

public class _998_FallenAngelSelect extends Quest implements ScriptFile
{
	private static final int NATOOLS = 30894;

	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

	public _998_FallenAngelSelect()
	{
		super(false);
		addStartNpc(NATOOLS);
		addTalkId(NATOOLS);
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("dawn"))
		{
			QuestState q1 = st.getPlayer().getQuestState("_142_FallenAngelRequestOfDawn");
			if(q1.isStarted() == false)
			{
				q1.setState(STARTED);
				htmltext = "30894-01.htm";
				st.exitCurrentQuest(false);
				st.setState(COMPLETED);
			}
		}
		else if(event.equalsIgnoreCase("dusk"))
		{
			QuestState q2 = st.getPlayer().getQuestState("_143_FallenAngelRequestOfDusk");
			if(q2.isStarted() == false)
			{
				q2.setState(STARTED);
				htmltext = "30894-01.htm";
				st.exitCurrentQuest(false);
				st.setState(COMPLETED);
			}
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		QuestState qs998 = st.getPlayer().getQuestState("_998_FallenAngelSelect");
		if(qs998.getState() == STARTED)
			htmltext = "30894-01.htm";

		return htmltext;
	}
}