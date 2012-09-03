package quests._700_CursedLife;

import l2rt.Config;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;
import quests._10273_GoodDayToFly._10273_GoodDayToFly;

/**
 * Version: Gracia Epilogue
 * Development team: L2Reload
 * @author Magister
 */

public class _700_CursedLife extends Quest implements ScriptFile
{
	private static int Orbyu = 32560;
	private static int[] Mobs = { 22602, 22603, 22604, 22605 };
	private static int Rok = 25624;

	private static int Swallowed_Skull = 13872;
	private static int Swallowed_Sternum = 13873;
	private static int Swallowed_Bones = 13874;

	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

	public _700_CursedLife()
	{
		super(false);
		addStartNpc(Orbyu);
		addTalkId(Orbyu);
		addKillId(Mobs);
		addKillId(Rok);
		addQuestItem(Swallowed_Skull);
		addQuestItem(Swallowed_Sternum);
		addQuestItem(Swallowed_Bones);
	}

	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("32560-03.htm"))
		{
			st.set("cond", "1");
			st.setState(STARTED);
			st.playSound(SOUND_ACCEPT);
		}
		else if(event.equalsIgnoreCase("32560-quit.htm"))
		{
			st.playSound(SOUND_FINISH);
			st.exitCurrentQuest(true);
		}
		return htmltext;
	}

	public String onTalk(L2NpcInstance npc, L2Player player, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getInt("cond");

		if(npcId == Orbyu)
		{
			QuestState qs = player.getQuestState(_10273_GoodDayToFly.class);
			if((qs != null) && (qs.getState() == 2) && (st.getState() == 0) && (player.getLevel() >= 75))
				htmltext = "32560-01.htm";
			else if(cond == 1)
			{
				long Skull = st.getQuestItemsCount(Swallowed_Skull);
				long Sternum = st.getQuestItemsCount(Swallowed_Sternum);
				long Bones = st.getQuestItemsCount(Swallowed_Bones);
				if(Skull + Sternum + Bones > 0)
				{
					st.giveItems(ADENA_ID, 50 * Skull + 100 * Sternum + 150 * Bones);
					st.takeItems(Swallowed_Skull, -1);
					st.takeItems(Swallowed_Sternum, -1);
					st.takeItems(Swallowed_Bones, -1);
					htmltext = "32560-06.htm";
				}
				else
					htmltext = "32560-04.htm";
			}
			else if(cond == 0)
				htmltext = "32560-00.htm";
		}
		return htmltext;
	}

	public String onKill(L2NpcInstance npc, L2Player player, QuestState st)
	{
		if(st.getState() != STARTED)
			return null;
		int npcId = npc.getNpcId();

		if(npcId == Rok)
		{
			if(st.getQuestItemsCount(Swallowed_Sternum) == 0)
			{
				st.rollAndGive(Swallowed_Sternum, 1, 80);
				st.playSound(SOUND_ITEMGET);
			}
			else if(st.getQuestItemsCount(Swallowed_Skull) == 0)
			{
				st.rollAndGive(Swallowed_Skull, 1, 80);
				st.playSound(SOUND_ITEMGET);
			}
		}
		else if(contains(Mobs, npcId))
		{
			st.rollAndGive(Swallowed_Bones, (int) (1 * Config.RATE_QUESTS_REWARD), 80);
			st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}
}