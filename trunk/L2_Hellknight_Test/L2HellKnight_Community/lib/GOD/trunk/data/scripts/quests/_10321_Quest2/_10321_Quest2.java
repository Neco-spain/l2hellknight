package quests._10321_Quest2;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;
import quests._10320_Quest1._10320_Quest1;

public class _10321_Quest2 extends Quest implements ScriptFile
{
	int TEODOR = 32975;
	int SHENON = 32974;
	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

	public _10321_Quest2()
	{
		super(false);
		addStartNpc(TEODOR);
		addTalkId(SHENON);
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("3.htm"))
		{
			st.set("cond", "1");
			st.setState(STARTED);
			st.playSound(SOUND_ACCEPT);
		}
		else if(event.equalsIgnoreCase("5.htm"))
		{
			st.giveItems(57, 5000);
			st.addExpAndSp(40, 500, true);
			st.playSound(SOUND_FINISH);
			st.exitCurrentQuest(false);
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getInt("cond");
		L2Player player = st.getPlayer();
		if(npcId == TEODOR)
		{
			QuestState qs = player.getQuestState(_10320_Quest1.class);
			if(cond == 0 && qs != null && qs.getState() == COMPLETED)
				htmltext = "1.htm";
			else if(cond == 1)
				htmltext = "3.htm";
		}
		else if(npcId == SHENON)
		{
			if(cond == 1)
				htmltext = "4.htm";
		}
		return htmltext;
	}
}
