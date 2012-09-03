package quests._10324_Quest5;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.Reflection;
import l2rt.gameserver.cache.Msg;
import javolution.util.FastMap;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.gameserver.network.serverpackets.MagicSkillUse;
import l2rt.gameserver.network.serverpackets.ExShowScreenMessage; 
import quests._10323_Quest4._10323_Quest4;
import l2rt.util.GArray;
import l2rt.util.Location;

public class _10324_Quest5 extends Quest implements ScriptFile
{
	int SHENON = 32974;
	int GALLINT = 32980;
	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

	public _10324_Quest5()
	{
		super(false);
		addStartNpc(SHENON);
		addTalkId(GALLINT);
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;
		L2Player player = st.getPlayer();
		if(event.equalsIgnoreCase("3.htm"))
		{
			st.set("cond", "1");
			st.setState(STARTED);
			st.playSound(SOUND_ACCEPT);
		}
		else if(event.equalsIgnoreCase("5.htm"))
		{
			st.giveItems(57, 11000);
			st.addExpAndSp(1700, 2000, true);
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
		Reflection r = player.getReflection();
		if(npcId == SHENON)
		{
			QuestState qs = player.getQuestState(_10323_Quest4.class);
			if(cond == 0 && qs != null && qs.getState() == COMPLETED)
				htmltext = "1.htm";
			else if(cond == 1)
				htmltext = "3.htm";
		}
		else if(npcId == GALLINT)
		{
			if(cond == 1) {
				htmltext = "4.htm";
			}
		}
		return htmltext;
	}

}
