package quests._10287_StoryOfThoseLeft;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;

public class _10287_StoryOfThoseLeft extends Quest implements ScriptFile
{
	private static final int rafforty = 32020;
	private static final int jinia = 32760;
	private static final int kegor = 32761;
	
	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}
	
	public _10287_StoryOfThoseLeft()
	{
		super(false);
		
		addStartNpc(rafforty);
		addTalkId(rafforty);
		addTalkId(jinia);
		addTalkId(kegor);
	}
	

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;
		
		if (st == null)
			return htmltext;

		if (npc.getNpcId() == rafforty)
		{
			if (event.equalsIgnoreCase("32020-04.htm"))
			{
				st.setState(STARTED);
				st.set("cond", "1");
				st.set("progress", "1");
				st.set("Ex1", "0");
				st.set("Ex2", "0");
				st.playSound(SOUND_ACCEPT);
			}
			
			else if (event.startsWith("reward_") && st.getInt("progress") == 2)
			{
				try
				{
					int itemId = Integer.parseInt(event.substring(7));

					if ((itemId >= 10549 && itemId <= 10553) || itemId == 14219)
						st.giveItems(itemId, 1);
					
					st.playSound(SOUND_FINISH);
					st.exitCurrentQuest(false);
					htmltext = "32020-11.htm";
				}
				catch (Exception e)
				{
				
				}
			}
		}

		else if (npc.getNpcId() == jinia)
		{
			if (event.equalsIgnoreCase("32760-03.htm") && st.getInt("progress") == 1 && st.getInt("Ex1") == 0)
			{
				st.set("Ex1", "1");
				st.set("cond", "3");
				st.playSound(SOUND_MIDDLE);
			}
		}
		
		else if (npc.getNpcId() == kegor)
		{
			if (event.equalsIgnoreCase("32761-04.htm") && st.getInt("progress") == 1 && st.getInt("Ex1") == 1 && st.getInt("Ex2") == 0)
			{
				st.set("Ex2", "1");
				st.set("cond", "4");
				st.playSound(SOUND_MIDDLE);
			}
		}

		return htmltext;
	}


	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		
		if (st == null)
			return htmltext;

		if (npc.getNpcId() == rafforty)
		{
			switch (st.getState())
			{
				case CREATED:
					QuestState qs = st.getPlayer().getQuestState("_10286_ReunionWithSirra");
					if (qs != null && qs.getState() == COMPLETED && st.getPlayer().getLevel() >= 82)
						htmltext = "32020-01.htm";
					else
						htmltext = "32020-03.htm";
					break;
				case STARTED:
					if (st.getInt("progress") == 1)
						htmltext = "32020-05.htm";
					else if (st.getInt("progress") == 2)
						htmltext = "32020-09.htm";
					break;
				case COMPLETED:
					htmltext = "32020-02.htm";
					break;
			}
		}

		else if (npc.getNpcId() == jinia && st.getInt("progress") == 1)
		{
			if (st.getInt("Ex1") == 0)
				return "32760-01.htm";

			else if (st.getInt("Ex1") == 1 && st.getInt("Ex2") == 0)
				return "32760-04.htm"; 

			else if (st.getInt("Ex1") == 1 && st.getInt("Ex2") == 1)
			{
				st.set("cond", "5");
				st.playSound(SOUND_MIDDLE);
				st.set("progress", "2");
				st.set("Ex1", "0");
				st.set("Ex2", "0");

				// destroy instance after 1 min
				//Instance inst = InstanceManager.getInstance().getInstance(npc.getInstanceId());
				//inst.setDuration(60000);
				//inst.setEmptyDestroyTime(0);
				
				return "32760-05.htm";
			} 

		}
		
		else if (npc.getNpcId() == kegor && st.getInt("progress") == 1)
		{
			if (st.getInt("Ex1") == 1 && st.getInt("Ex2") == 0)
				htmltext = "32761-01.htm";
			
			else if (st.getInt("Ex1") == 0 && st.getInt("Ex2") == 0)
				htmltext = "32761-02.htm";

			else if (st.getInt("Ex2") == 1)
				htmltext = "32761-05.htm";
		}

		
		return htmltext;
	}
}