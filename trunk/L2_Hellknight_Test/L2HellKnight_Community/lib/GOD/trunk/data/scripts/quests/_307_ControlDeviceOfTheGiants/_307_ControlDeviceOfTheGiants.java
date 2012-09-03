package quests._307_ControlDeviceOfTheGiants;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;

public class _307_ControlDeviceOfTheGiants extends Quest implements ScriptFile
{
	// NPCs
	private static int DROPH = 32711;
	//RBs
	private static int GORGOLOS = 25681;
	private static int LAST_TITAN_UTENUS = 25684;
	private static int GIANT_MARPANAK = 25680;
	private static int HEKATON_PRIME = 25687;
	
	// ITEMS
	private static int CET_1_SHEET = 14829;
	private static int CET_2_SHEET = 14830;	
	private static int CET_3_SHEET = 14831;
	private static int SUPPLY_BOX = 14850;
	private int Resp = 0;
	

	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}	
	public _307_ControlDeviceOfTheGiants()
	{
		super(false);

		addStartNpc(DROPH);
		addQuestItem(CET_1_SHEET);
		addQuestItem(CET_2_SHEET);
		addQuestItem(CET_3_SHEET);
		addKillId(GORGOLOS);		
		addKillId(LAST_TITAN_UTENUS);
		addKillId(GIANT_MARPANAK);
		addKillId(HEKATON_PRIME);
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		L2Player player = st.getPlayer();
		String htmltext = event;
		if(event.equalsIgnoreCase("32711-02.htm"))
		{
			st.set("cond", "1");
			st.setState(STARTED);
			st.playSound(SOUND_ACCEPT);
		}
		if(event.equalsIgnoreCase("dospawn"))
		{
			st.addSpawn(HEKATON_PRIME, 191975, 56959, -7616, 1800000);
			Resp = 1;
			return "32711-04.htm";
		}		
		return htmltext;
	}

	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getInt("cond");
		int id = st.getState();
		L2Player player = st.getPlayer();
		if(npcId == DROPH)
		{
			if(id == CREATED && cond == 0)
			{
				if(player.getLevel() >= 79)
					return "32711-01.htm";
				else
				{	
					st.exitCurrentQuest(true);
					return "32711-00.htm";
				}	
			}		
			else if(id == STARTED)
				if(npcId == DROPH)
				{
					if(cond == 2)
					{
						st.takeItems(CET_1_SHEET,1);
						st.takeItems(CET_2_SHEET,1);
						st.takeItems(CET_3_SHEET,1);
						st.giveItems(SUPPLY_BOX,1);
						st.exitCurrentQuest(true);
						st.playSound(SOUND_FINISH);						
						return "32711-10.htm"; //quest completed html
					}	
					else if(st.getQuestItemsCount(CET_1_SHEET) == 0 || st.getQuestItemsCount(CET_2_SHEET) == 0 || st.getQuestItemsCount(CET_3_SHEET) == 0)
						return "32711-09.htm"; //didn't killed all RBs html						
					else if(cond == 1 && st.getQuestItemsCount(CET_1_SHEET) >= 1 && st.getQuestItemsCount(CET_2_SHEET) >= 1 && st.getQuestItemsCount(CET_3_SHEET) >= 1 && Resp == 0)
						return "32711-11.htm"; //quest set raid boss spawn html 
					else if(Resp == 1)
						return "32711-06.htm";
				}		

			
		}	
		return "noquest";
	}
	public String onKill(L2NpcInstance npc, QuestState st)
	{
		L2Player player = st.getPlayer();
		int cond = st.getInt("cond");
		int npcId = npc.getNpcId();
		if(cond == 1)
		{
			if(npcId == GORGOLOS)
			{
				st.giveItems(CET_1_SHEET,1);
				st.playSound("SOUND_ITEMGET");
			}	
			else if(npcId == LAST_TITAN_UTENUS)
			{
				st.giveItems(CET_2_SHEET,1);
				st.playSound("SOUND_ITEMGET");
			}		
			else if(npcId == GIANT_MARPANAK)
			{
				st.giveItems(CET_3_SHEET,1);
				st.playSound("SOUND_ITEMGET");
			}	
			else if(npcId == HEKATON_PRIME)
			{
				st.set("cond","2");
				ThreadPoolManager.getInstance().scheduleGeneral(new setresp(), 1000*60*60*3);
			}		
		}	
		return null;		
	}	
	private class setresp implements Runnable
	{
		public void run()
		{
			Resp = 0;
		}
	}	
}