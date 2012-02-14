package quests._10330_Quest11;

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
import quests._10329_Quest10._10329_Quest10;
import l2rt.util.GArray;
import l2rt.util.Location;

public class _10330_Quest11 extends Quest implements ScriptFile
{
	int ATRAN = 33448;
	int RAXIS = 32977;
	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

	public _10330_Quest11()
	{
		super(false);
		addStartNpc(ATRAN);
		addTalkId(RAXIS);
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;
		L2Player player = st.getPlayer();
		Reflection r = player.getReflection();
		if(event.equalsIgnoreCase("3.htm"))
		{
			st.set("cond", "1", true);
			st.setState(STARTED);
			st.playSound(SOUND_ACCEPT);
		} 
		else if(event.equalsIgnoreCase("6.htm"))
		{
			player.sendPacket(new ExShowScreenMessage("Open the inventory and check for armor.", 5000)); 
			st.giveItems(57, 62000);
			st.giveItems(29, 1);
			st.giveItems(22, 1);
			st.addExpAndSp(23000, 25000, true);
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
		if(npcId == ATRAN)
		{
			QuestState qs = player.getQuestState(_10329_Quest10.class);
			if(cond == 0 && qs != null && qs.getState() == COMPLETED)
				htmltext = "1.htm";
			else if(cond == 1)
				htmltext = "3.htm";
		}
		else if(npcId == RAXIS)
		{
			if(cond == 1) 
				htmltext = "4.htm";
		}
		return htmltext;
	}

}
