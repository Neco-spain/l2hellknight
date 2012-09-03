package quests._176_StepsForHonor;

import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.entity.siege.territory.TerritorySiege;
import l2rt.util.Rnd;

public class _176_StepsForHonor extends Quest implements ScriptFile
{
	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

	private static final int RAPIDUS = 36479;

	private static final int CLOAK = 14603;
	



	public _176_StepsForHonor()
	{
		super(true);
		addStartNpc(RAPIDUS);
		addTalkId(RAPIDUS);
		addQuestItem(CLOAK);
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("36479-02.htm"))
		{
			st.setCond(1);
			st.setState(STARTED);
			st.playSound(SOUND_ACCEPT);
		}	
		return htmltext;
	}

	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		L2Player player = st.getPlayer();
		String htmltext = "noquest";
		int id = st.getState();
		int cond = st.getCond();
		int npcId = npc.getNpcId();
		
		if(npcId == RAPIDUS)
		{
			if(id == STARTED)
			{
				if(TerritorySiege.isInProgress())
					htmltext = "36479-tw.htm";	
				else if (cond == 1)
				{
					htmltext = "36479-03.htm";
					st.addNotifyOfPlayerKill();
				}	
				else if(cond == 2)	
				{
					st.setCond(3);
					htmltext = "36479-04.htm";
					st.addNotifyOfPlayerKill();
					st.takeItems(4674,-1);
				}
				else if(cond == 3)	
				{
					htmltext = "36479-05.htm";
					st.addNotifyOfPlayerKill();
				}	
				else if(cond == 4)
				{
					st.setCond(5);
					htmltext = "36479-06.htm";	
					st.addNotifyOfPlayerKill();
					st.takeItems(4674,-1);
				}	
				else if(cond == 5)	
				{
					htmltext = "36479-07.htm";	
					st.addNotifyOfPlayerKill();
				}	
				else if(cond == 6)
				{
					st.setCond(7);
					htmltext = "36479-08.htm";
					st.addNotifyOfPlayerKill();
					st.takeItems(4674,-1);
				}	
				else if(cond == 7)	
				{
					htmltext = "36479-09.htm";
					st.addNotifyOfPlayerKill();
				}	
				else if (cond == 8)
				{
					st.giveItems(CLOAK,1);
					htmltext = "36479-09.htm";
					st.exitCurrentQuest(false);
					st.setState(COMPLETED);
					st.takeItems(4674,-1);
				}	
			}	
			else if(id == CREATED)
			{
				if(st.getPlayer().getLevel() >= 80)
				{
					htmltext = "36479-01.htm";
					st.addNotifyOfPlayerKill();
				}	
				else
					htmltext = "36479-00.htm";		
			}
			else if (id == COMPLETED)
				htmltext = "36479-11.htm";
		}
		return htmltext;
	}
	@Override
	public String onPlayerKill(L2Player killed, QuestState st)
	{	
		int cond = st.getCond();
		L2Player killer = st.getPlayer();
		if (cond == 1)
		{
			if(killed == null || killer == null || !checkPlayers(killed, killer))
				return null;
			if(st.getQuestItemsCount(4674) >= 8 && cond == 1)
			{
				//st.removeNotifyOfPlayerKill();
				st.setCond(2);
				st.giveItems(4674,1);
				return null;
			}
			else
			{
				st.giveItems(4674,1);
				return null;
			}
		}	
		else if (cond == 3)
		{
			if(killed == null || killer == null || !checkPlayers(killed, killer))
				return null;
			if(st.getQuestItemsCount(4674) >= 17 && cond == 3)
			{
				//st.removeNotifyOfPlayerKill();
				st.setCond(4);
				st.giveItems(4674,1);
				return null;
			}
			else
			{
				st.giveItems(4674,1);
				return null;
			}
		}	
		else if (cond == 5)
		{
			if(killed == null || killer == null || !checkPlayers(killed, killer))
				return null;
			if(st.getQuestItemsCount(4674) >= 26 && cond == 5)
			{
				//st.removeNotifyOfPlayerKill();
				st.setCond(6);
				st.giveItems(4674,1);
				return null;
			}
			else
			{
				st.giveItems(4674,1);
				return null;
			}
		}	
		else if (cond == 7)
		{
			if(killed == null || killer == null || !checkPlayers(killed, killer))
				return null;
			if(st.getQuestItemsCount(4674) >= 35 && cond == 7)
			{
				//st.removeNotifyOfPlayerKill();
				st.setCond(8);
				st.giveItems(4674,1);
				return null;
			}
			else
			{
				st.giveItems(4674,1);
				return null;
			}
		}		
		return null;
		
	}	
	public static boolean checkPlayers(L2Player killed, L2Player killer)
	{
		if(killer.getTerritorySiege() < 0 || killed.getTerritorySiege() < 0 || killer.getTerritorySiege() == killed.getTerritorySiege())
			return false;
		if(killer.getParty() != null && killer.getParty() == killed.getParty())
			return false;
		if(killer.getClan() != null && killer.getClan() == killed.getClan())
			return false;
		if(killer.getAllyId() > 0 && killer.getAllyId() == killed.getAllyId())
			return false;
		if(killer.getLevel() < 61 || killed.getLevel() < 61)
			return false;
		return true;	
	}	
}