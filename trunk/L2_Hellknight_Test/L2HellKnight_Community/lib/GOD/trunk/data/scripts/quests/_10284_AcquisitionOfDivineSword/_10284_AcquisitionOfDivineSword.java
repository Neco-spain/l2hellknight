package quests._10284_AcquisitionOfDivineSword;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.gameserver.model.L2Player;

public class _10284_AcquisitionOfDivineSword extends Quest implements ScriptFile
{
	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}
	
	// NPC's
	private static final int rafforty = 32020;
	private static final int jinia = 32760;
	private static final int kroon = 32653;
	private static final int taroon = 32654;

	public _10284_AcquisitionOfDivineSword()
	{
		super(false);
		
		addStartNpc(rafforty);
		addTalkId(rafforty);
		addTalkId(jinia);
		addTalkId(kroon);
		addTalkId(taroon);
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;
		int npcId = npc.getNpcId();
		
		if (npcId == rafforty)
		{
			if (event.equalsIgnoreCase("32020-04.htm"))
			{
				st.setState(STARTED);
				st.set("progress", "1");
				st.set("cond", "1");
				st.set("jinia_themes", "102030"); //theme ID - state - something like 1-0, 2-0, 3-0
				st.playSound(SOUND_ACCEPT);
			}
		}
		
		else if (npcId == jinia)
		{
			if (event.equalsIgnoreCase("32760-05.htm"))
			{
				switch(st.getInt("jinia_themes"))
				{
				case 112030: //1st theme have been readed
					htmltext = "32760-05a.htm";
					break;
				
				case 102130: //2nd theme have been readed
					htmltext = "32760-05b.htm";
					break;

				case 102031: //3rd theme have been readed
					htmltext = "32760-05c.htm";
					break;

				case 102131: //2nd and 3rd theme have been readed
					htmltext = "32760-05d.htm";
					break;

				case 112031: //1st and 3rd theme have been readed
					htmltext = "32760-05e.htm";
					break;

				case 112130: //1st and 2nd theme have been readed
					htmltext = "32760-05f.htm";
					break;

				case 112131: //all three themes have been readed
					htmltext = "32760-05g.htm";
				}
			}
			
			else if (event.equalsIgnoreCase("32760-02c.htm"))
			{
				int jinia_themes = st.getInt("jinia_themes");
				jinia_themes += 10000; //mark 1st theme as readed
				st.set("jinia_themes", Integer.toString(jinia_themes));
			}

			else if (event.equalsIgnoreCase("32760-03c.htm"))
			{
				int jinia_themes = st.getInt("jinia_themes");
				jinia_themes += 100; //mark 2nd theme as readed
				st.set("jinia_themes", Integer.toString(jinia_themes));
			}

			else if (event.equalsIgnoreCase("32760-04c.htm"))
			{
				int jinia_themes = st.getInt("jinia_themes");
				jinia_themes += 1; //mark 3rd theme as readed
				st.set("jinia_themes", Integer.toString(jinia_themes));
			}

			else if (event.equalsIgnoreCase("32760-07.htm"))
			{
				st.set("jinia_themes","102030");
				st.set("progress", "2");
				st.set("cond", "3");
				st.playSound(SOUND_MIDDLE);

			// destroy instance after 1 min
			//InstancedZoneManager ilm = InstancedZoneManager.getInstance();
			//ilm.setDuration(60000);
			//ilm.setEmptyDestroyTime(0);
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
					QuestState _prev = player.getQuestState("_10283_RequestOfIceMerchant");
					if ((_prev != null) && (_prev.getState() == COMPLETED) && (player.getLevel() >= 82))
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
		else if (npcId == jinia)
		{
			if (id != STARTED)
				return htmltext;

			if (st.getInt("progress") == 1)
			{
				int jinia_themes = st.getInt("jinia_themes");
				//look above for explanation 
				switch(jinia_themes)
				{
					case 102030:
						htmltext = "32760-01.htm"; 
						break;
					case 112030:
						htmltext = "32760-01a.htm"; 
						break;
					case 102130:
						htmltext = "32760-01b.htm"; 
						break;
					case 102031:
						htmltext = "32760-01c.htm"; 
						break;
					case 102131:
						htmltext = "32760-01d.htm"; 
						break;
					case 112031:
						htmltext = "32760-01e.htm"; 
						break;
					case 112130:
						htmltext = "32760-01f.htm"; 
						break;
					case 112131:
						htmltext = "32760-01g.htm"; 
						break;
				}
			}
		}
		
		else if (npcId == kroon || npc.getNpcId() == taroon)
		{
			if (id != STARTED)
				return htmltext;
			
			if (st.getInt("progress") == 2)
				htmltext = npc.getNpcId() == kroon ? "32653-01.htm" : "32654-01.htm";

			else if (st.getInt("progress") == 3)
			{
				st.set("jinia_themes","102030");
				st.giveItems(57, 296425);
				st.addExpAndSp(921805, 82230);
				st.playSound(SOUND_FINISH);
				htmltext = npc.getNpcId() == kroon ? "32653-05.htm" : "32654-05.htm";
				st.exitCurrentQuest(false);
			}
		}
		
		return htmltext;
	}
}