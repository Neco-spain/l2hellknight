package quests._10325_Quest6;

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
import quests._10324_Quest5._10324_Quest5;
import l2rt.util.GArray;
import l2rt.util.Location;

public class _10325_Quest6 extends Quest implements ScriptFile
{
	int GALLINT = 32980;
	int ELF = 32148;
	int HUM = 32156;
	int DELF = 32161;
	int GNOM = 32159;
	int KAM = 32144;
	int ORK = 32151;
	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

	public _10325_Quest6()
	{
		super(false);
		addStartNpc(GALLINT);
		addTalkId(ELF);
		addTalkId(HUM);
		addTalkId(DELF);
		addTalkId(GNOM);
		addTalkId(KAM);
		addTalkId(ORK);
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;
		L2Player player = st.getPlayer();
		if(event.equalsIgnoreCase("3.htm"))
		{
			if (player.getRace().ordinal() == 0) {
				st.set("cond", "2", true);
				htmltext = "hum.htm";
			}
			if (player.getRace().ordinal() == 1) {
				st.set("cond", "3", true);
				htmltext = "elf.htm";
			}
			if (player.getRace().ordinal() == 2) {
				st.set("cond", "4", true);
				htmltext = "delf.htm";
			}
			if (player.getRace().ordinal() == 3) {
				st.set("cond", "5", true);
				htmltext = "ork.htm";
			}
			if (player.getRace().ordinal() == 4) {
				st.set("cond", "6", true);
				htmltext = "gnom.htm";
			}
			if (player.getRace().ordinal() == 5) {
				st.set("cond", "7", true);
				htmltext = "kam.htm";
			}
			st.setState(STARTED);
			st.playSound(SOUND_ACCEPT);
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
			QuestState qs = player.getQuestState(_10324_Quest5.class);
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
		else if(npcId == HUM)
		{
			if(cond == 2) {
				htmltext = "hum1.htm";
				st.set("cond", "8", true);
			} else 
			htmltext = "no.htm";
		}
		else if(npcId == ELF)
		{
			if(cond == 3) {
				htmltext = "elf1.htm";
				st.set("cond", "9", true);
			} else 
			htmltext = "no.htm";
		}
		else if(npcId == DELF)
		{
			if(cond == 4) {
				htmltext = "delf1.htm";
				st.set("cond", "10", true);
			} else 
			htmltext = "no.htm";
		}
		else if(npcId == ORK)
		{
			if(cond == 5) {
				htmltext = "ork1.htm";
				st.set("cond", "11", true);
			} else 
			htmltext = "no.htm";
		}
		else if(npcId == GNOM)
		{
			if(cond == 6) {
				htmltext = "gnom1.htm";
				st.set("cond", "12", true);
			} else 
			htmltext = "no.htm";
		}
		else if(npcId == KAM)
		{
			if(cond == 7) {
				htmltext = "kam1.htm";
				st.set("cond", "13", true);
			} else 
			htmltext = "no.htm";
		}
		return htmltext;
	}

}
