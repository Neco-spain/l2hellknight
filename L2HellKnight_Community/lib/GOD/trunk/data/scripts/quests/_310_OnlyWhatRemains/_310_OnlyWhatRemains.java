package quests._310_OnlyWhatRemains;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.util.Rnd;
import quests._240_ImTheOnlyOneYouCanTrust._240_ImTheOnlyOneYouCanTrust;

public class _310_OnlyWhatRemains extends Quest implements ScriptFile
{
	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}
	
	private static final int KINTAIJIN = 32640;

	private static final int SpikedStakato = 22617;
	private static final int CannibalisticStakatoFollower = 22624;
	private static final int CannibalisticStakatoLeader1 = 22625;
	private static final int CannibalisticStakatoLeader2 = 22626;
	private static final int Stakato_Cheif = 25667;

	private static final int DIRTYBEAD  = 14880;
	private static final int GROWTHACCELERATOR = 14832;
	private static final int COLOREDJEWEL = 14835;
	private static final int reward[] = { 14833, 14834 };

/**
 * Quest _310_ для Stakato Nest.
 * @author Drizzy
 * @date 26.08.10
 */
	public _310_OnlyWhatRemains()
	{
		super(true);
		addStartNpc(KINTAIJIN);
		addKillId(new int[] {SpikedStakato, CannibalisticStakatoFollower, CannibalisticStakatoLeader1, CannibalisticStakatoLeader2, Stakato_Cheif});
		addQuestItem(new int[] {DIRTYBEAD, GROWTHACCELERATOR, 14833, 14834});
	}	
	
	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int id = st.getState();
		int cond = st.getCond();
		if(id == COMPLETED)
			htmltext = "32640-10.htm";
		else if(id == CREATED)
		{
			QuestState ImTheOnlyOneYouCan = st.getPlayer().getQuestState(_240_ImTheOnlyOneYouCanTrust.class);
			if(ImTheOnlyOneYouCan != null && ImTheOnlyOneYouCan.isCompleted() && st.getPlayer().getLevel() >= 81)
				htmltext = "32640-1.htm";
			else
			{
				htmltext = "32640-0.htm";
				st.exitCurrentQuest(true);
			}
		}
		else
		{
			if(cond == 1)
				htmltext = "32640-8.htm";
			if(cond == 2)
				htmltext = "32640-9.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		L2Player player = st.getPlayer();
		String htmltext = event;		
		int cond = st.getCond();		
		if(event.equals("give_item"))
		{
			if(st.getQuestItemsCount(DIRTYBEAD) >= 500)
			{
				st.set("cond", "1");
				st.takeItems(DIRTYBEAD, 500);
				st.giveItems(GROWTHACCELERATOR, 1);
				if(st.getQuestItemsCount(COLOREDJEWEL) == 0)
					st.giveItems(COLOREDJEWEL, 1);
				st.playSound(SOUND_ACCEPT);
				htmltext = "32640-12.htm";
			}
		}
		if(event.equalsIgnoreCase("32640-3.htm"))
		{
			st.set("cond", "1");
			st.setState(STARTED);
			st.playSound(SOUND_ACCEPT);
		}		
		if(event.equals("tp1"))
		{
			if(st.getQuestItemsCount(COLOREDJEWEL) == 1)
			{
				player.teleToLocation(80456, -52322, -5640);
				return "";
			}
			else
			{
				htmltext = "32640-11.htm";
			}
		}
		if(event.equals("tp2"))
		{
			if(st.getQuestItemsCount(COLOREDJEWEL) == 1)
			{
				player.teleToLocation(88718, -46214, -4640);
				return "";
			}
			else
			{
				htmltext = "32640-11.htm";
			}			
		}
		if(event.equals("tp3"))
		{
			if(st.getQuestItemsCount(COLOREDJEWEL) == 1)
			{
				player.teleToLocation(87464, -54221, -5120);
				return "";
			}
			else
			{
				htmltext = "32640-11.htm";
			}
		}
		if(event.equals("tp4"))
		{
			if(st.getQuestItemsCount(COLOREDJEWEL) == 1)
			{
				player.teleToLocation(80848, -49426, -5128);
				return "";
			}
			else
			{
				htmltext = "32640-11.htm";
			}
		}
		if(event.equals("tp5"))
		{
			if(st.getQuestItemsCount(COLOREDJEWEL) == 1)
			{
				player.teleToLocation(87682, -43291, -4128);
				return "";
			}
			else
			{
				htmltext = "32640-11.htm";
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(L2NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getInt("cond");
		if(cond >= 1)
		{
			st.giveItems(DIRTYBEAD, 1);
			if(st.getQuestItemsCount(DIRTYBEAD) >= 500 && cond == 1)
			{
				st.set("cond", "2");
				st.playSound(SOUND_MIDDLE);
			}
			else
				st.playSound(SOUND_ITEMGET);

			if(npcId == Stakato_Cheif)
			{
				st.giveItems(reward[Rnd.get(reward.length)], 1);	
				st.playSound(SOUND_ITEMGET);	
			}
		}
		return null;
	}	
}