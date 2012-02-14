package quests._10285_MeetingSirra;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.gameserver.model.L2Player;
import quests._10284_AcquisitionOfDivineSword._10284_AcquisitionOfDivineSword;

public class _10285_MeetingSirra extends Quest implements ScriptFile
{
	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}
	
	//private static final String qn = "_10285_MeetingSirra";
	// NPC's
	private static final int rafforty = 32020;
	private static final int steward = 32029;
	private static final int jinia = 32760;
	private static final int kegor = 32761;
	private static final int sirra = 32762;
	private static final int jinia2 = 32781;

	public _10285_MeetingSirra()
	{
		super(false);
		addStartNpc(rafforty);
		addFirstTalkId(sirra);
		addTalkId(rafforty);
		addTalkId(jinia);
		addTalkId(jinia2);
		addTalkId(kegor);
		addTalkId(sirra);
		addTalkId(steward);
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;
		
		int npcId = npc.getNpcId();
		L2Player player = st.getPlayer();
		
		if (npcId == rafforty)
		{
			if (event.equalsIgnoreCase("32020-05.htm"))
			{
				st.setState(STARTED);
				st.set("cond", "1");
				st.set("progress", "1");
				st.set("Ex", "0");
				st.playSound(SOUND_ACCEPT);
			}
		}
		
		else if (npcId == jinia)
		{
			if (event.equalsIgnoreCase("32760-02.htm"))
			{
				st.set("Ex", "1");
				st.set("cond", "3");
				st.playSound(SOUND_MIDDLE);
			}

			else if (event.equalsIgnoreCase("32760-06.htm"))
			{
				st.set("Ex", "3");
				//addSpawn(sirra, -23905,-8790,-5384,56238, false, 0, false, npc.getInstanceId());
				addSpawn(sirra, -23905,-8790,-5384,56238,0,0);
				st.set("cond", "5");
				st.playSound(SOUND_MIDDLE);
			}

			else if (event.equalsIgnoreCase("32760-12.htm"))
			{
				st.set("Ex", "5");
				st.set("cond", "7");
				st.playSound(SOUND_MIDDLE);
			}

			else if (event.equalsIgnoreCase("32760-14.htm"))
			{
				st.set("Ex", "0");
				st.set("progress", "2");
				st.playSound(SOUND_MIDDLE);

				// destroy instance after 1 min
				//Instance inst = InstanceManager.getInstance().getInstance(npc.getInstanceId());
				//InstancedZoneManager ilm = InstancedZoneManager.getInstance();
				//inst.setDuration(60000);
				//inst.setEmptyDestroyTime(0);
			}
		}

		else if (npcId == kegor)
		{
			if (event.equalsIgnoreCase("32761-02.htm"))
			{
				st.set("Ex", "2");
				st.set("cond", "4");
				st.playSound(SOUND_MIDDLE);
			}
		}

		else if (npcId == sirra)
		{
			if (event.equalsIgnoreCase("32762-08.htm"))
			{
				st.set("Ex", "4");
				st.set("cond", "6");
				st.playSound(SOUND_MIDDLE);
			}
		}

		else if (npcId == steward)
		{
			if (event.equalsIgnoreCase("go"))
			{
				if (player.getLevel() >= 82)
				{
					player.teleToLocation(103045,-124361,-2768);
					htmltext = "";
				}
				else
					htmltext = "32029-01a";
			}
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int id = st.getState();
		L2Player player = st.getPlayer();
		
		if (npcId == rafforty)
		{
			switch (id)
			{
				case CREATED:
					QuestState qs = player.getQuestState(_10284_AcquisitionOfDivineSword.class);
					if ((qs != null) && (qs.getState() == COMPLETED) && (player.getLevel() >= 82))
						htmltext = "32020-01.htm";
					else
						htmltext = "32020-03.htm";
					break;
				case STARTED:
					if (st.getInt("progress") == 1)
						htmltext = "32020-06.htm";
					else if (st.getInt("progress") == 2)
						htmltext = "32020-09.htm";
					else if (st.getInt("progress") == 3)
					{
						st.giveItems(57, 283425);
						st.addExpAndSp(939075, 83855);
						st.playSound(SOUND_FINISH);
						st.exitCurrentQuest(false);
						htmltext = "32020-10.htm";
					}
					break;
				case COMPLETED:
					htmltext = "32020-02.htm";
					break;
			}
		}
		
		else if (npcId == jinia && st.getInt("progress") == 1)
		{
			switch (st.getInt("Ex"))
			{
				case 0:
					return "32760-01.htm";
				case 1:
					return "32760-03.htm";
				case 2:
					return "32760-04.htm";
				case 3:
					return "32760-07.htm";
				case 4:
					return "32760-08.htm";
				case 5:
					return "32760-13.htm";
			}
		}

		else if (npcId == kegor && st.getInt("progress") == 1)
		{
			switch (st.getInt("Ex"))
			{
				case 1:
					return "32761-01.htm";
				case 2:
					return "32761-03.htm";
				case 3:
					return "32761-04.htm";
			}
		}
		
		else if (npcId == sirra && st.getInt("progress") == 1)
		{
			switch (st.getInt("Ex"))
			{
				case 3:
					return "32762-01.htm";
				case 4:
					return "32762-09.htm";
			}
		}

		else if (npcId == steward && st.getInt("progress") == 2)
		{
			htmltext = "32029-01.htm";
			st.set("cond", "8");
			st.playSound(SOUND_MIDDLE);
		}

		else if (npcId == jinia2 && st.getInt("progress") == 2)
		{
			htmltext = "32781-01.htm";
			st.playSound(SOUND_MIDDLE);
		}
	
		return htmltext;
	}

	@Override
	public String onKill(L2NpcInstance npc, QuestState st)
	{
		return null;
	}
}