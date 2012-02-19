package quests.Q1_LettersOfLove1;

import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;
import l2.brick.gameserver.model.quest.State;

/**
 * @author l2.brickTeam
 */
public class Q1_LettersOfLove1 extends Quest
{
	private final static String QN = "1_LettersOfLove1";
	
	public static void main(String[] args)
	{
		new Q1_LettersOfLove1(1, QN, "Letters Of Love");
	}
	
	// NPCs 
	private final int DARIN = 30048;
	private final int ROXXY = 30006;
	private final int BAULRO = 30033;
	
	// ITEMS 
	private final int DARINGS_LETTER = 687;
	private final int RAPUNZELS_KERCHIEF = 688;
	private final int DARINGS_RECEIPT = 1079;
	private final int BAULS_POTION = 1080;
	
	// REWARD 
	private final int NECKLACE = 906;
	
	/**
	 * @param questId
	 * @param name
	 * @param descr
	 */
	public Q1_LettersOfLove1(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(DARIN);
		
		addTalkId(DARIN);
		addTalkId(ROXXY);
		addTalkId(BAULRO);
		
		questItemIds = new int[] { DARINGS_LETTER, RAPUNZELS_KERCHIEF, DARINGS_RECEIPT, BAULS_POTION };
	}
	
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(QN);
		if (st == null)
		{
			return null;
		}
		if (event.equalsIgnoreCase("30048-06.htm"))
		{
			st.set("cond", "1");
		}
		st.setState(State.STARTED);
		st.playSound("ItemSound.quest_accept");
		if (st.getQuestItemsCount(DARINGS_LETTER) == 0)
		{
			st.giveItems(DARINGS_LETTER, 1);
		}
		return htmltext;
	}
	
	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(QN);
		String htmltext = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>";
		if (st == null)
		{
			return htmltext;
		}
		int npcId = npc.getNpcId();
		int id = st.getState();
		
		int cond = st.getInt("cond");
		long ItemsCount_DL = st.getQuestItemsCount(DARINGS_LETTER);
		long ItemsCount_RK = st.getQuestItemsCount(RAPUNZELS_KERCHIEF);
		long ItemsCount_DR = st.getQuestItemsCount(DARINGS_RECEIPT);
		long ItemsCount_BP = st.getQuestItemsCount(BAULS_POTION);
		if (id == State.COMPLETED)
		{
			htmltext = "<html><body>This quest has already been completed.</body></html>";
		}
		else if ((npcId == DARIN) && (id == State.CREATED))
		{
			if (player.getLevel() >= 2)
			{
				if (cond < 15)
				{
					htmltext = "30048-02.htm";
				}
				else
				{
					htmltext = "30048-01.htm";
					st.exitQuest(true);
				}
			}
			else
			{
				htmltext = "<html><body>Quest for characters level 2 and above.</body></html>";
				st.exitQuest(true);
			}
		}
		else if ((id == State.STARTED) && (cond > 0))
		{
			if (npcId == ROXXY)
			{
				if ((ItemsCount_RK == 0) && (ItemsCount_DL > 0))
				{
					htmltext = "30006-01.htm";
					st.takeItems(DARINGS_LETTER, -1);
					st.giveItems(RAPUNZELS_KERCHIEF, 1);
					st.set("cond", "2");
					st.playSound("ItemSound.quest_middle");
				}
				else if ((ItemsCount_BP > 0) || (ItemsCount_DR > 0))
				{
					htmltext = "30006-03.htm";
				}
				else if (ItemsCount_RK > 0)
				{
					htmltext = "30006-02.htm";
				}
			}
			else if ((npcId == DARIN) && (ItemsCount_RK > 0))
			{
				htmltext = "30048-08.htm";
				st.takeItems(RAPUNZELS_KERCHIEF, -1);
				st.giveItems(DARINGS_RECEIPT, 1);
				st.set("cond", "3");
				st.playSound("ItemSound.quest_middle");
			}
			else if (npcId == BAULRO)
			{
				if (ItemsCount_DR > 0)
				{
					htmltext = "30033-01.htm";
					st.takeItems(DARINGS_RECEIPT, -1);
					st.giveItems(BAULS_POTION, 1);
					st.set("cond", "4");
					st.playSound("ItemSound.quest_middle");
				}
				else if (ItemsCount_BP > 0)
				{
					htmltext = "30033-02.htm";
				}
			}
			else if ((npcId == DARIN) && (ItemsCount_RK == 0))
			{
				if (ItemsCount_DR > 0)
				{
					htmltext = "30048-09.htm";
				}
				else if (ItemsCount_BP > 0)
				{
					htmltext = "30048-10.htm";
					st.takeItems(BAULS_POTION, -1);
					st.giveAdena(2466, true);
					st.giveItems(NECKLACE, 1);
					st.addExpAndSp(5672, 446);
					st.unset("cond");
					st.exitQuest(false);
					st.playSound("ItemSound.quest_finish");
				}
				else
				{
					htmltext = "30048-07.htm";
				}
			}
		}
		return htmltext;
	}
}
