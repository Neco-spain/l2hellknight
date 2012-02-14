package quests._024_InhabitantsOfTheForestOfTheDead;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.gameserver.model.quest.QuestTimer;
import l2rt.util.Rnd;
import quests._023_LidiasHeart._023_LidiasHeart;

public class _024_InhabitantsOfTheForestOfTheDead extends Quest implements ScriptFile
{
	// Список NPC
	private final int DORIAN = 31389;
	private final int TOMBSTONE = 31531;
	private final int MAID_OF_LIDIA = 31532;
	private final int MYSTERIOUS_WIZARD = 31522;
	//private final int BENEDICT = 31349;

	// Список итемов
	private final int LIDIA_HAIR_PIN = 7148;
	private final int SUSPICIOUS_TOTEM_DOLL = 7151;
	private final int FLOWER_BOUQUET = 7152;
	private final int SILVER_CROSS_OF_EINHASAD = 7153;
	private final int BROKEN_SILVER_CROSS_OF_EINHASAD = 7154;
	private final int SUSPICIOUS_TOTEM_DOLL1 = 7156;

	// Список мобов
	// Bone Snatchers, Bone Shapers, Bone Collectors, Bone Animators, Bone Slayers, Skull Collectors, Skull Animators
	private final int[] MOBS = new int[] { 21557, 21558, 21560, 21561, 21562, 21563, 21564, 21565, 21566, 21567 };
	private final int[] VAMPIRE = new int[] { 25328, 25329, 25330, 25331, 25332 };

	public _024_InhabitantsOfTheForestOfTheDead()
	{
		super(false); //TODO: проверить партийность в клиенте

		addStartNpc(DORIAN);

		addTalkId(TOMBSTONE);
		addTalkId(MAID_OF_LIDIA);
		addTalkId(MYSTERIOUS_WIZARD);

		for(int npcId : MOBS)
			addKillId(npcId);

		addQuestItem(SUSPICIOUS_TOTEM_DOLL);
	}

	@Override
	public String onTalk(L2NpcInstance npc, QuestState qs)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = qs.getInt("cond");

		if(npcId == DORIAN)
		{
			if(cond == 0)
			{
				QuestState LidiasHeart = qs.getPlayer().getQuestState(_023_LidiasHeart.class);
				if(LidiasHeart != null)
					if(LidiasHeart.isCompleted())
						htmltext = "31389-01.htm";
					else
						htmltext = "31389-02.htm"; // Если 23 квест не пройден
				else
					htmltext = "31389-02.htm"; // Если 23 квест не пройден
			}
			else if(cond == 1 && qs.getQuestItemsCount(FLOWER_BOUQUET) == 1)
				htmltext = "31389-04.htm"; // Если букет еще в руках
			else if(cond == 2 && qs.getQuestItemsCount(FLOWER_BOUQUET) == 0)
				htmltext = "31389-05.htm";
			else if(cond == 3 && qs.getQuestItemsCount(SILVER_CROSS_OF_EINHASAD) == 1)
				htmltext = "31389-14.htm";
			else if(cond == 4 && qs.getQuestItemsCount(BROKEN_SILVER_CROSS_OF_EINHASAD) == 1)
			{
				htmltext = "31389-15.htm";
				qs.takeItems(BROKEN_SILVER_CROSS_OF_EINHASAD, -1);
			}
			else if(cond == 7 && qs.getQuestItemsCount(LIDIA_HAIR_PIN) == 0)
			{
				htmltext = "31389-21.htm";
				qs.giveItems(LIDIA_HAIR_PIN, 1);
				qs.set("cond", "8");
			}
		}
		else if(npcId == TOMBSTONE)
		{
			if(cond == 1 && qs.getQuestItemsCount(FLOWER_BOUQUET) == 1)
				htmltext = "31531-01.htm";
			else if(cond == 2 && qs.getQuestItemsCount(FLOWER_BOUQUET) == 0)
				htmltext = "31531-03.htm"; // Если букет уже оставлен
		}
		else if(npcId == MAID_OF_LIDIA)
		{
			if(cond == 5)
				htmltext = "31532-01.htm";
			else if(cond == 6)
				if(qs.getQuestItemsCount(LIDIA_HAIR_PIN) == 0)
				{
					htmltext = "31532-07.htm";
					qs.set("cond", "7");
				}
				else
					htmltext = "31532-05.htm";
			else if(cond == 8 && qs.getQuestItemsCount(LIDIA_HAIR_PIN) != 0)
				htmltext = "31532-10.htm";
			qs.takeItems(LIDIA_HAIR_PIN, -1);
		}
		else if(npcId == MYSTERIOUS_WIZARD)
		{
			QuestTimer timer_speak = qs.getQuestTimer("To talk with Mystik");
			if(cond == 10 && qs.getQuestItemsCount(SUSPICIOUS_TOTEM_DOLL) != 0)
				htmltext = "31522-01.htm";
			else if(cond == 11 && timer_speak == null && qs.getQuestItemsCount(SUSPICIOUS_TOTEM_DOLL1) == 0)
				htmltext = "31522-09.htm";
			else if(cond == 11 && qs.getQuestItemsCount(SUSPICIOUS_TOTEM_DOLL1) != 0)
				htmltext = "31522-22.htm";
		}
		return htmltext;
	}

	@Override
	public String onEvent(String event, QuestState qs, L2NpcInstance npc)
	{
		if(event.startsWith("playerInMobRange")) //TODO: Сделать какой-то стандарт
		{
			if(qs.getInt("cond") == 3)
			{
				String[] arr = event.split("_");
				int id = Integer.parseInt(arr[1]);
				if(arrayContains(VAMPIRE, id))
				{
					qs.takeItems(LIDIA_HAIR_PIN, -1); //TODO: проверить на соответсвие оффу
					qs.takeItems(SILVER_CROSS_OF_EINHASAD, -1);
					qs.giveItems(BROKEN_SILVER_CROSS_OF_EINHASAD, 1);
					qs.playSound(SOUND_HORROR2);
					qs.set("cond", "4");
				}
			}
			event = null;
		}
		else if(event.equalsIgnoreCase("31389-03.htm"))
		{
			qs.giveItems(FLOWER_BOUQUET, 1);
			qs.set("cond", "1");
			qs.setState(STARTED);
			qs.playSound(SOUND_ACCEPT);
		}
		else if(event.equalsIgnoreCase("31531-02.htm"))
		{
			qs.takeItems(FLOWER_BOUQUET, -1);
			qs.set("cond", "2");
		}
		else if(event.equalsIgnoreCase("31389-13.htm"))
		{
			qs.giveItems(SILVER_CROSS_OF_EINHASAD, 1);
			qs.set("cond", "3");
		}
		else if(event.equalsIgnoreCase("31389-19.htm"))
			qs.set("cond", "5");
		else if(event.equalsIgnoreCase("31532-04.htm"))
		{
			qs.set("cond", "6");
			qs.startQuestTimer("Lidias Letter", 7000);
		}
		else if(event.equalsIgnoreCase("Lidias Letter"))
			return "lidias_letter.htm";
		else if(event.equalsIgnoreCase("31532-06.htm"))
			qs.takeItems(LIDIA_HAIR_PIN, -1);
		else if(event.equalsIgnoreCase("31532-19.htm"))
			qs.set("cond", "9");
		else if(event.equalsIgnoreCase("31522-03.htm"))
			qs.takeItems(SUSPICIOUS_TOTEM_DOLL, -1);
		else if(event.equalsIgnoreCase("31522-08.htm"))
		{
			qs.set("cond", "11");
			qs.startQuestTimer("To talk with Mystik", 600000);
		}
		else if(event.equalsIgnoreCase("31522-21.htm"))
		{
			qs.giveItems(SUSPICIOUS_TOTEM_DOLL1, 1);
			qs.startQuestTimer("html", 5);
			return "Congratulations! You are completed this quest!" + " \n The Quest \"Hiding Behind the Truth\"" + " become available.\n Show Suspicious Totem Doll to " + " Priest Benedict.";
		}
		else if(event.equalsIgnoreCase("html"))
		{
			qs.playSound(SOUND_FINISH);
			qs.addExpAndSp(242105, 22529, true);
			qs.exitCurrentQuest(false);
			return "31522-22.htm";
		}
		return event;
	}

	@Override
	public String onKill(L2NpcInstance npc, QuestState qs)
	{
		if(qs == null)
			return null;

		if(qs.getState() != STARTED)
			return null;

		int npcId = npc.getNpcId();
		int cond = qs.getInt("cond");

		if(arrayContains(MOBS, npcId))
			if(cond == 9 && Rnd.chance(70))
			{
				qs.giveItems(SUSPICIOUS_TOTEM_DOLL, 1);
				qs.playSound(SOUND_MIDDLE);
				qs.set("cond", "10");
			}

		return null;
	}

	private boolean arrayContains(int[] array, int id)
	{
		for(int i : array)
			if(i == id)
				return true;
		return false;
	}

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	@Override
	public void onAbort(QuestState st)
	{
		if(st.getQuestItemsCount(LIDIA_HAIR_PIN) == 0)
			st.giveItems(LIDIA_HAIR_PIN, 1);
	}
}
