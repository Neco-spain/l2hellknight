package quests._311_ExpulsionOfEvilSpirits;

import l2rt.Config;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Spawn;
import l2rt.gameserver.model.L2World;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.util.Rnd;
import l2rt.util.Location;

public class _311_ExpulsionOfEvilSpirits extends Quest implements ScriptFile
{
	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

	private static int Chairen = 32655;
	private static int Guard = 18811;
	private static int Baranka = 18808;

	private static int SoulCoreContainingEvilSpirit = 14881;

	private static int RagnaOrcAmulet = 14882;

	private static int DROP_CHANCE1 = 1;
	private static int DROP_CHANCE2 = 40;

	private static int[] MOBS = new int[] { 22691, 22692, 22693, 22694, 22695, 22696, 22697, 22698, 22699, 22701, 22702 };

	public _311_ExpulsionOfEvilSpirits()
	{
		super(false);

		addStartNpc(Chairen);
		addTalkId(Guard); 
		addKillId(MOBS);
		addKillId(Baranka);
	}
	
	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("32655-06.htm"))
		{
			st.set("cond", "1");
			st.setState(STARTED);
			st.playSound(SOUND_ACCEPT);
		}
		else if(event.equalsIgnoreCase("32655-09.htm"))
		{
			st.exitCurrentQuest(true);
			st.playSound(SOUND_FINISH);
		}
		if(event.equalsIgnoreCase("summon"))
		{
			if(st.getQuestItemsCount(14848) >= 1)
			{
				st.takeItems(14848, 1);
				npc.setBusy(true);
				Functions.spawn(new Location(74568,-101912,-960), Baranka);
				htmltext = "Fight!";
			}
			else
			{
				htmltext = "Sorry You don`t have Protection Soul's Pendant";
			}
		}
		if(event.equalsIgnoreCase("32655-18.htm"))
        {
			if(st.getQuestItemsCount(SoulCoreContainingEvilSpirit) >= 10)
			{
				st.giveItems(14848, 1);	// Protection Soul's Pendant
				st.takeItems(SoulCoreContainingEvilSpirit, 10);
				st.playSound("ItemSound.quest_finish");
				//st.set("cond", "2"); // Наработки для аи охраника
				htmltext = "32655-18ok.htm";
			}
			else
			{
				htmltext = "32655-18no.htm";
			}
        }
		else
		{
			int id = 0;
			try
			{
				id = Integer.parseInt(event);
			}
			catch(Exception e)
			{}

			if(id > 0)
			{
				int count = 0;
				switch(id)
				{
					case 9482:
						count = 488;
						break;
					case 9483:
						count = 305;
						break;
					case 9484:
						count = 183;
						break;
					case 9485:
						count = 122;
						break;
					case 9486:
						count = 122;
						break;
					case 9487:
						count = 366;
						break;
					case 9488:
						count = 229;
						break;
					case 9489:
						count = 183;
						break;
					case 9490:
						count = 122;
						break;
					case 9491:
						count = 122;
						break;
					case 9628:
						count = 24;
						break;
					case 9629:
						count = 43;
						break;
					case 9630:
						count = 36;
						break;
				}
				if(count > 0)
				{
					if(st.getQuestItemsCount(RagnaOrcAmulet) >= count)
					{
						st.giveItems(id, 1);
						st.takeItems(RagnaOrcAmulet, count);
						st.playSound(SOUND_MIDDLE);
						return "32655-16.htm";
					}
					return "32655-15.htm";
				}
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
		int cond = st.getCond();
		if(npcId == Chairen)
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 80)
					htmltext = "32655-05.htm"; // TODO поменять после заполнения диалогов  на htmltext = "32655-01.htm";
				else
				{
					htmltext = "32655-00.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(id == STARTED)
			{
				if(st.getQuestItemsCount(RagnaOrcAmulet) >= 1)
					htmltext = "32655-10.htm";
				else
					htmltext = "32655-07.htm";
			}		
		return htmltext;
	}

	@Override
	public String onKill(L2NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == Baranka)
		{
            for(L2NpcInstance gvard : L2ObjectsStorage.getAllByNpcId(Guard, true))
                gvard.setBusy(false);
		}
		
		if(cond >= 1 && contains(MOBS, npcId))
		{
			if(Rnd.chance(DROP_CHANCE1))
			{
				st.giveItems(SoulCoreContainingEvilSpirit, 1);
				st.playSound(SOUND_FANFARE2);
			}
			if(Rnd.chance(DROP_CHANCE2))
			{
				st.giveItems(RagnaOrcAmulet, (int)Config.RATE_QUESTS_DROP);
				st.playSound(SOUND_ITEMGET);
			}
		}	
		return null;
	}
}