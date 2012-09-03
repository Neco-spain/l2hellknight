package quests._10326_Quest7;

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
import quests._10325_Quest6._10325_Quest6;
import l2rt.util.GArray;
import l2rt.util.Location;

public class _10326_Quest7 extends Quest implements ScriptFile
{
	int GALLINT = 32980;
	int PANTEON = 32972;
	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

	public _10326_Quest7()
	{
		super(false);
		addStartNpc(GALLINT);
		addTalkId(PANTEON);
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;
		L2Player player = st.getPlayer();
		if(event.equalsIgnoreCase("3.htm"))
		{
			st.set("cond", "1", true);
			st.setState(STARTED);
			st.playSound(SOUND_ACCEPT);
		} else if(event.equalsIgnoreCase("5.htm"))
		{
			st.giveItems(57, 14000);
			st.addExpAndSp(5300, 2800, true);
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
		if(npcId == GALLINT)
		{
			QuestState qs = player.getQuestState(_10325_Quest6.class);
			if(cond == 0 && qs != null && qs.getState() == COMPLETED)
				htmltext = "1.htm";
			else if(cond >=8) {
				htmltext = "3.htm";
				st.giveItems(57, 12000);
				if (player.isMageClass())
					st.giveItems(2509, 1000);
				else
					st.giveItems(1835, 1000);
				st.addExpAndSp(3254, 2400, true);
				st.playSound(SOUND_FINISH);
				st.exitCurrentQuest(false);
			}
		}
		else if(npcId == PANTEON)
		{
			if(cond == 1) 
				htmltext = "4.htm";
		}
		return htmltext;
	}

}
