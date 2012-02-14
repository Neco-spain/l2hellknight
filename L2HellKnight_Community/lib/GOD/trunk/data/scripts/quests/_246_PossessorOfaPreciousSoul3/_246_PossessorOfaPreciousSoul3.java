package quests._246_PossessorOfaPreciousSoul3;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.util.Rnd;
import quests._242_PossessorOfaPreciousSoul2._242_PossessorOfaPreciousSoul2;

public class _246_PossessorOfaPreciousSoul3 extends Quest implements ScriptFile
{
	private static final int CARADINES_LETTER_2_PART = 7678;
	private static final int RING_OF_GODDESS_WATERBINDER = 7591;
	private static final int NECKLACE_OF_GODDESS_EVERGREEN = 7592;
	private static final int STAFF_OF_GODDESS_RAIN_SONG = 7593;
	private static final int CARADINES_LETTER = 7679;
	private static final int RELIC_BOX = 7594;

	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

	public _246_PossessorOfaPreciousSoul3()
	{
		super(true);

		addStartNpc(31740);

		addTalkId(31741);
		addTalkId(30721);

		addKillId(21541);
		addKillId(21544);
		addKillId(25325);

		addQuestItem(new int[] { RING_OF_GODDESS_WATERBINDER, NECKLACE_OF_GODDESS_EVERGREEN, STAFF_OF_GODDESS_RAIN_SONG });
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("caradine_q0246_0104.htm"))
		{
			st.set("cond", "1");
			st.takeItems(CARADINES_LETTER_2_PART, 1);
			st.setState(STARTED);
			st.playSound(SOUND_ACCEPT);
		}
		else if(event.equals("ossian_q0246_0201.htm"))
		{
			st.set("cond", "2");
			st.playSound(SOUND_MIDDLE);
		}
		else if(event.equals("ossian_q0246_0301.htm"))
		{
			st.set("cond", "4");
			st.playSound(SOUND_MIDDLE);
		}
		else if(event.equals("ossian_q0246_0401.htm"))
		{
			st.takeItems(RING_OF_GODDESS_WATERBINDER, 1);
			st.takeItems(NECKLACE_OF_GODDESS_EVERGREEN, 1);
			st.takeItems(STAFF_OF_GODDESS_RAIN_SONG, 1);
			st.set("cond", "6");
			st.giveItems(RELIC_BOX, 1);
			st.playSound(SOUND_MIDDLE);
		}
		else if(event.equals("magister_ladd_q0246_0501.htm"))
		{
			st.takeItems(RELIC_BOX, 1);
			st.giveItems(CARADINES_LETTER, 1);
			st.addExpAndSp(719843, 0);
			st.unset("cond");
			st.exitCurrentQuest(false);
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		if(!st.getPlayer().isSubClassActive())
			return "Subclass only!";

		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getInt("cond");
		if(npcId == 31740)
		{
			if(cond == 0)
			{
				QuestState previous = st.getPlayer().getQuestState(_242_PossessorOfaPreciousSoul2.class);
				if(previous != null && previous.getState() == COMPLETED && st.getPlayer().getLevel() >= 65)
					htmltext = "caradine_q0246_0101.htm";
				else
				{
					htmltext = "caradine_q0246_0102.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond == 1)
				htmltext = "caradine_q0246_0105.htm";
		}
		else if(npcId == 31741)
		{
			if(cond == 1)
				htmltext = "ossian_q0246_0101.htm";
			else if((cond == 2 || cond == 3) && (st.getQuestItemsCount(RING_OF_GODDESS_WATERBINDER) < 1 || st.getQuestItemsCount(NECKLACE_OF_GODDESS_EVERGREEN) < 1))
				htmltext = "ossian_q0246_0203.htm";
			else if(cond == 3 && st.getQuestItemsCount(RING_OF_GODDESS_WATERBINDER) == 1 && st.getQuestItemsCount(NECKLACE_OF_GODDESS_EVERGREEN) == 1)
				htmltext = "ossian_q0246_0202.htm";
			else if(cond == 4)
				htmltext = "ossian_q0246_0301.htm";
			else if((cond == 4 || cond == 5) && st.getQuestItemsCount(STAFF_OF_GODDESS_RAIN_SONG) < 1)
				htmltext = "ossian_q0246_0402.htm";
			else if(cond == 5 && st.getQuestItemsCount(RING_OF_GODDESS_WATERBINDER) == 1 && st.getQuestItemsCount(NECKLACE_OF_GODDESS_EVERGREEN) == 1 && st.getQuestItemsCount(STAFF_OF_GODDESS_RAIN_SONG) == 1)
				htmltext = "ossian_q0246_0303.htm";
			else if(cond == 6)
				htmltext = "ossian_q0246_0403.htm";
		}
		else if(npcId == 30721)
			if(cond == 6 && st.getQuestItemsCount(RELIC_BOX) == 1)
				htmltext = "magister_ladd_q0246_0401.htm";
		return htmltext;
	}

	@Override
	public String onKill(L2NpcInstance npc, QuestState st)
	{
		if(!st.getPlayer().isSubClassActive())
			return null;

		int npcId = npc.getNpcId();
		int cond = st.getInt("cond");
		if(cond == 2)
		{
			if(Rnd.chance(15))
				if(npcId == 21541 && st.getQuestItemsCount(RING_OF_GODDESS_WATERBINDER) == 0)
				{
					st.giveItems(RING_OF_GODDESS_WATERBINDER, 1);
					if(st.getQuestItemsCount(RING_OF_GODDESS_WATERBINDER) == 1 && st.getQuestItemsCount(NECKLACE_OF_GODDESS_EVERGREEN) == 1)
						st.set("cond", "3");
					st.playSound(SOUND_ITEMGET);
				}
				else if(npcId == 21544 && st.getQuestItemsCount(NECKLACE_OF_GODDESS_EVERGREEN) == 0)
				{
					st.giveItems(NECKLACE_OF_GODDESS_EVERGREEN, 1);
					if(st.getQuestItemsCount(RING_OF_GODDESS_WATERBINDER) == 1 && st.getQuestItemsCount(NECKLACE_OF_GODDESS_EVERGREEN) == 1)
						st.set("cond", "3");
					st.playSound(SOUND_ITEMGET);
				}
		}
		else if(cond == 4)
			if(npcId == 25325 && st.getQuestItemsCount(STAFF_OF_GODDESS_RAIN_SONG) == 0)
			{
				st.giveItems(STAFF_OF_GODDESS_RAIN_SONG, 1);
				st.set("cond", "5");
				st.playSound(SOUND_ITEMGET);
			}
		return null;
	}
}