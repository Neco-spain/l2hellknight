package quests._042_HelpTheUncle;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;

public class _042_HelpTheUncle extends Quest implements ScriptFile
{
	private static final int WATERS = 30828;
	private static final int SOPHYA = 30735;

	private static final int TRIDENT = 291;
	private static final int MAP_PIECE = 7548;
	private static final int MAP = 7549;
	private static final int PET_TICKET = 7583;

	private static final int MONSTER_EYE_DESTROYER = 20068;
	private static final int MONSTER_EYE_GAZER = 20266;

	private static final int MAX_COUNT = 30;

	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

	public _042_HelpTheUncle()
	{
		super(false);

		addStartNpc(WATERS);

		addTalkId(WATERS);
		addTalkId(SOPHYA);

		addKillId(MONSTER_EYE_DESTROYER);
		addKillId(MONSTER_EYE_GAZER);
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("1"))
		{
			htmltext = "pet_manager_waters_q0042_0104.htm";
			st.set("cond", "1");
			st.setState(STARTED);
			st.playSound(SOUND_ACCEPT);
		}
		else if(event.equals("3") && st.getQuestItemsCount(TRIDENT) > 0)
		{
			htmltext = "pet_manager_waters_q0042_0201.htm";
			st.takeItems(TRIDENT, 1);
			st.set("cond", "2");
		}
		else if(event.equals("4") && st.getQuestItemsCount(MAP_PIECE) >= MAX_COUNT)
		{
			htmltext = "pet_manager_waters_q0042_0301.htm";
			st.takeItems(MAP_PIECE, MAX_COUNT);
			st.giveItems(MAP, 1);
			st.set("cond", "4");
		}
		else if(event.equals("5") && st.getQuestItemsCount(MAP) > 0)
		{
			htmltext = "sophia_q0042_0401.htm";
			st.takeItems(MAP, 1);
			st.set("cond", "5");
		}
		else if(event.equals("7"))
		{
			htmltext = "pet_manager_waters_q0042_0501.htm";
			st.giveItems(PET_TICKET, 1);
			st.unset("cond");
			st.exitCurrentQuest(false);
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int id = st.getState();
		int cond = st.getInt("cond");
		if(id == CREATED)
		{
			if(st.getPlayer().getLevel() >= 25)
				htmltext = "pet_manager_waters_q0042_0101.htm";
			else
			{
				htmltext = "pet_manager_waters_q0042_0103.htm";
				st.exitCurrentQuest(true);
			}
		}
		else if(id == STARTED)
			if(npcId == WATERS)
			{
				if(cond == 1)
					if(st.getQuestItemsCount(TRIDENT) == 0)
						htmltext = "pet_manager_waters_q0042_0106.htm";
					else
						htmltext = "pet_manager_waters_q0042_0105.htm";
				else if(cond == 2)
					htmltext = "pet_manager_waters_q0042_0204.htm";
				else if(cond == 3)
					htmltext = "pet_manager_waters_q0042_0203.htm";
				else if(cond == 4)
					htmltext = "pet_manager_waters_q0042_0303.htm";
				else if(cond == 5)
					htmltext = "pet_manager_waters_q0042_0401.htm";
			}
			else if(npcId == SOPHYA)
				if(cond == 4 && st.getQuestItemsCount(MAP) > 0)
					htmltext = "sophia_q0042_0301.htm";
				else if(cond == 5)
					htmltext = "sophia_q0042_0402.htm";
		return htmltext;
	}

	@Override
	public String onKill(L2NpcInstance npc, QuestState st)
	{
		int cond = st.getInt("cond");
		if(cond == 2)
		{
			long pieces = st.getQuestItemsCount(MAP_PIECE);
			if(pieces < MAX_COUNT - 1)
			{
				st.giveItems(MAP_PIECE, 1);
				st.playSound(SOUND_ITEMGET);
			}
			else if(pieces == MAX_COUNT - 1)
			{
				st.giveItems(MAP_PIECE, 1);
				st.playSound(SOUND_MIDDLE);
				st.set("cond", "3");
			}
		}
		return null;
	}
}