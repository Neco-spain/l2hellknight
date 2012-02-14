package quests._10283_RequestOfIceMerchant;

import quests._115_TheOtherSideOfTruth._115_TheOtherSideOfTruth;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;

public class _10283_RequestOfIceMerchant extends Quest implements ScriptFile
{
	private static final int rafforty = 32020;
	private static final int kier = 32022;
	private static final int jinia = 32760;

	public _10283_RequestOfIceMerchant()
	{
		super(false);

		addStartNpc(rafforty);
		addTalkId(kier);
		addTalkId(jinia);
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;

		if(event.equals("repre_q10283_05.htm"))
		{
			st.set("cond", "1");
			st.setState(STARTED);
			st.playSound(SOUND_ACCEPT);
		}
		else if(event.equals("repre_q10283_09.htm"))
		{
			st.set("cond", "2");
			st.playSound(SOUND_MIDDLE);
		}
		else if(event.equalsIgnoreCase("spawn"))
		{
			st.addSpawn(jinia, 104322, -107669, -3680, 60000);
			return null;
		}
		else if(event.equalsIgnoreCase("jinia_npc_q10283_03.htm"))
		{
			st.giveItems(57, 190000);
			st.addExpAndSp(627000, 50300);
			st.setState(COMPLETED);
			st.exitCurrentQuest(false);
			st.playSound(SOUND_FINISH);
			npc.deleteMe();
		}

		return htmltext;
	}

	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int id = st.getState();
		int cond = st.getCond();

		if(npcId == rafforty)
		{
			if(id == CREATED)
			{
				QuestState qs = st.getPlayer().getQuestState(_115_TheOtherSideOfTruth.class);
				if(qs != null && qs.isCompleted() && st.getPlayer().getLevel() >= 82)
					return "repre_q10283_01.htm";
				else
					return "repre_q10283_03.htm";
			}
			if(id == STARTED)
			{
				if(st.getInt("cond") == 1)
					return "repre_q10283_06.htm";
				else if(st.getInt("cond") == 2)
					return "repre_q10283_10.htm";
			}
			if(id == COMPLETED)
				return "repre_q10283_02.htm";
		}
		if(npcId == kier && cond == 2)
			return "keier_q10283_01.htm";
		if(npcId == jinia && cond == 2)
			return "jinia_npc_q10283_01.htm";
		return "noquest";
	}

	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

}