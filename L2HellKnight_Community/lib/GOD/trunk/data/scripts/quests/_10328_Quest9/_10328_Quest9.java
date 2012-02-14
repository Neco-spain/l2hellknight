package quests._10328_Quest9;

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
import quests._10327_Quest8._10327_Quest8;
import l2rt.util.GArray;
import l2rt.util.Location;

public class _10328_Quest9 extends Quest implements ScriptFile
{
	int PANTEON = 32972;
	int KEKIY = 30565;
	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

	public _10328_Quest9()
	{
		super(false);
		addStartNpc(PANTEON);
		addTalkId(KEKIY);
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;
		L2Player player = st.getPlayer();
		Reflection r = player.getReflection();
		if(event.equalsIgnoreCase("4.htm"))
		{
			st.set("cond", "1", true);
			st.setState(STARTED);
			st.playSound(SOUND_ACCEPT);
		} 
		else if(event.equalsIgnoreCase("7.htm"))
		{
			st.giveItems(57, 20000);
			st.addExpAndSp(13000, 4000, true);
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
		if(npcId == PANTEON)
		{
			QuestState qs = player.getQuestState(_10327_Quest8.class);
			if(cond == 0 && qs != null && qs.getState() == COMPLETED)
				htmltext = "1.htm";
			else if(cond == 1)
				htmltext = "4.htm";
		}
		else if(npcId == KEKIY)
		{
			if(cond == 1) 
				htmltext = "5.htm";
		}
		return htmltext;
	}

}
