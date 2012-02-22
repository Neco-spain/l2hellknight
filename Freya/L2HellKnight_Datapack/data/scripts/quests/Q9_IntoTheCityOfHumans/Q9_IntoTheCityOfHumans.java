/**
 * 
 */
package quests.Q9_IntoTheCityOfHumans;

import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;

/**
 * @author l2.hellknightTeam
 *
 */
public class Q9_IntoTheCityOfHumans extends Quest
{
	private final static String QN = "9_IntoTheCityOfHumans";
	
	public static void main(String[] args)
	{
		new Q9_IntoTheCityOfHumans(663, QN, "Seductive Whispers");
	}
	
	// NPCs 
	private final int PETUKAI = 30583;
	private final int TANAPI = 30571;
	private final int TAMIL = 30576;
	
	// REWARDS
	private final int SCROLL_OF_ESCAPE_GIRAN = 7559;
	private final int MARK_OF_TRAVELER = 7570;
	
	/**
	 * @param questId
	 * @param name
	 * @param descr
	 */
	public Q9_IntoTheCityOfHumans(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(PETUKAI);
		
		addTalkId(PETUKAI);
		
		addTalkId(TANAPI);
		addTalkId(TAMIL);
	}
	
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(QN);
		String htmltext = event;
		if (event.equalsIgnoreCase("30583-03.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30571-02.htm"))
		{
			st.set("cond", "2");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30576-02.htm"))
		{
			st.giveItems(MARK_OF_TRAVELER, 1);
			st.giveItems(SCROLL_OF_ESCAPE_GIRAN, 1);
			st.unset("cond");
			st.exitQuest(false);
			st.playSound("ItemSound.quest_finish");
		}
		return htmltext;
	}
	
	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(QN);
		if (st == null)
		{
			return htmltext;
		}
		int npcId = npc.getNpcId();
		int cond = st.getInt("cond");
		byte id = st.getState();
		if (id == State.COMPLETED)
		{
			htmltext = "<html><body>This quest has already been completed.</body></html>";
		}
		else if (id == State.CREATED)
		{
			if (player.getRace().ordinal() == 3)
			{
				if (player.getLevel() >= 3)
				{
					htmltext = "30583-02.htm";
				}
				else
				{
					htmltext = "<html><body>Quest for characters level 3 and above.</body></html>";
					st.exitQuest(true);
				}
			}
			else
			{
				htmltext = "30583-01.htm";
				st.exitQuest(true);
			}
		}
		else if (id == State.STARTED)
		{
			if ((npcId == TANAPI) && (cond > 0))
			{
				htmltext = "30571-01.htm";
			}
			else if ((npcId == PETUKAI) && (cond == 1))
			{
				htmltext = "30583-04.htm";
			}
			else if ((npcId == TAMIL) && (cond == 2))
			{
				htmltext = "30576-01.htm";
			}
		}
		return htmltext;
	}
}
