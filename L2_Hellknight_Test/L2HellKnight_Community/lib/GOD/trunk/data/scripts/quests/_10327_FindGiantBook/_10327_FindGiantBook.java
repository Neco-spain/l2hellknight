package quests._10327_FindGiantBook;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.gameserver.instancemanager.AwakingManager;

/**
 * Ищущий книгу Гигантов (10327)
 * @author ALF
 */
public class _10327_FindGiantBook extends Quest implements ScriptFile
{
	// NPC
	private static final int Selfin = 33477;
	//private static final int Valakas = 29028;
	// Item
	private static final int FloatingStone = 7267;
	private static final int PoorNecklace = 15524;
	private static final int ValorNecklace = 15525;
	private static final int ValakaSlayerCirclet = 8567;
	
	private static final int[] _Monsters = 
	{
		27462,	//Ракджан	Рыцарь Сигеля
		27463,	//Батус Кракии	Воин Тира
		27464,	//Бамонти	Разбойник Одала
		27465,	//Кракос Кракии	Лучник Эура
		27466,	//Кан Ваисс	Волшебник Фео
		27467,	//Секнос	Заклинатель Иса
		27468,	//Лотус Кракии	Призыватель Веньо
		27469,	//Воскрешающий Могилы	Целитель Альгиза
		27454	//Эль Ваисс	Целитель Альгиза
	};
	
	private static final int[] _Stones = 
	{
		33397,	//Рыцарь Сигеля - Мастер Защиты	Сила перерождения Рыцаря
		33398,	//Воин Тира - Мастер Оружия/Силы	Сила перерождения Воина
		33399,	//Разбойник Одала - Мастер Кинжалов	Сила перерождения Разбойника
		33400,	//Лучник Эура - Мастер Лука/Арбалета	Сила перерождения Лучника
		33401,	//Волшебник Фео - Мастер Магии	Сила перерождения Волшебника
		33402,	//Заклинатель Иса - Мастер Чар	Сила перерождения Заклинателя
		33403,	//Призыватель Веньо - Мастер Призыва	Сила перерождения Призывателя
		33404	//Целитель Альгиза - Мастер Лечения	Сила перерождения Целителя
	};
	
	public _10327_FindGiantBook()
	{
		super(false);
		
		addStartNpc(Selfin);
		addTalkId(Selfin);
		
		for (int _id :_Stones)
			addTalkId(_id);	

		for (int _id :_Monsters)
			addKillId(_id);
		
	}
	// TODO
	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;
		
		
		if (event.equalsIgnoreCase("33477-01.htm"))
		{
			st.setState(STARTED);
			st.set("cond", "1");
			st.playSound(SOUND_ACCEPT);
			AwakingManager.getInstance().SendReqToAwaking(st.getPlayer());
		}
		return htmltext;
	}
	// TODO
	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		
		switch (st.getState())
		{
			case CREATED:
			{
				if (st.getPlayer().getLevel() < 85)
					htmltext = "33477-02.htm";
				else if (st.getPlayer().getClassId().level() < 3)
					htmltext = "33477-02.htm";
				else
					htmltext = "33477-01.htm";
				break;
			}
			case STARTED:
			{
				if (st.getInt("cond") == 1 && st.getQuestItemsCount(PoorNecklace) >= 1)
					htmltext = "31540-08.htm";
				else if (st.getInt("cond") == 1 && st.getQuestItemsCount(PoorNecklace) == 0)
				{
					st.giveItems(PoorNecklace, 1);
					htmltext = "31540-09.htm";
				}
				else if (st.getInt("cond") == 2)
				{
					st.takeItems(ValorNecklace, 1);
					st.giveItems(57, 126549);
					st.addExpAndSp(717291, 77397);
					st.giveItems(ValakaSlayerCirclet, 1);
					st.unset("cond");
					st.exitCurrentQuest(false);
					st.playSound("ItemSound.quest_finish");
					htmltext = "31540-10.htm";
				}
				break;
			}
			case COMPLETED:
			{
				htmltext = "31540-03.htm";
				break;
			}
		}
		
		return htmltext;
	}
	
	// TODO
	/*
	@Override
	public String onKill(L2NpcInstance npc, QuestState st)
	{
		if (st.getPlayer().getParty() != null)
		{
			for (L2Player partyMember : st.getPlayer().getParty().getPartyMembers());
			rewardPlayer(_party);
		}
		else
			rewardPlayer(player);
		return null;
	}
	
	private void rewardPlayer(L2Player player)
	{
		QuestState st = player.getQuestState("_10327_FindGiantBook");
		
		if (st != null && st.getInt("cond") == 1)
		{
			st.takeItems(PoorNecklace, 1);
			st.giveItems(ValorNecklace, 1);
			st.playSound("ItemSound.quest_middle");
			st.set("cond", "2");
		}
	}
	*/
	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}
	
	
}