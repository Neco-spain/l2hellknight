package quests.Q270_TheOneWhoEndsSilence;

import l2.hellknight.Config;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;
import l2.hellknight.gameserver.util.Util;

/**
 * The One Who Ends Silence (270).
 * @author sam.jr
 */
public class Q270_TheOneWhoEndsSilence extends Quest
{
	private static final String qn = "270_TheOneWhoEndsSilence";
	
	/*
	 * TODO: Remove the comment below: Lvl 82+ quest "The One Who Ends Silence" 
	 * where you have to collect at least 100 Monk's Rags from some of the monks 
	 * on the ground floor and any of the angels on the upper floor. 
	 * Unfortunately you get this quest item very rarely, about one for every 50 
	 * monks killed. As a reward you will get Icarus weapon recipes and key parts.
	 * Tada!
	 */
	
	// NPCs
	private static final int GREYMORE = 32757;
	private static final int[] SOLINA_NPCS =
	{
		22781,
		22790,
		22792,
		22793
	};
	private static final int[] DIVINITY_NPCS =
	{
		22794,
		22795,
		22796,
		22797,
		22798,
		22799,
		22800
	};
	
	// Items
	private static final int MONK_CLOTHES = 15526;
	
	// Rewards
	private static final int[] REW_REC =
	{
		10373,
		10374,
		10375,
		10376,
		10377,
		10378,
		10379,
		10380,
		10381
	};
	private static final int[] REW_SP =
	{
		9898,
		5593,
		5594,
		5595
	};
	private static final int[] REW_PARTS =
	{
		10397,
		10398,
		10399,
		10400,
		10401,
		10402,
		10403,
		10404,
		10405
	};
	
	private static final int CHANCE = 10; // ??
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(qn);
		
		if (st == null)
		{
			return htmltext;
		}
		
		final long mcc = st.getQuestItemsCount(MONK_CLOTHES);
		
		if (npc.getNpcId() == GREYMORE)
		{
			switch (event)
			{
				case "32757-04.htm":
					st.set("cond", "1");
					st.setState(State.STARTED);
					st.playSound("ItemSound.quest_accept");
					break;
				case "32757-08.html":
					if (mcc == 0)
					{
						htmltext = "32757-06.html";
					}
					else if (mcc < 100)
					{
						htmltext = "32757-07.html";
					}
					break;
				case "32757-12.html":
					st.playSound("ItemSound.quest_finish");
					st.exitQuest(true);
					break;
			}
			
			if (Util.isDigit(event))
			{
				int iEvent = Integer.parseInt(event);
				if (st.getQuestItemsCount(MONK_CLOTHES) < iEvent)
				{
					htmltext = "32757-10.html";
				}
				else
				{
					switch (iEvent)
					{
						case 100:
							if (mcc >= iEvent)
							{
								if (getRandom(100) <= 50)
								{
									st.giveItems(REW_REC[getRandom(REW_REC.length)], 1);
								}
								else
								{
									st.giveItems(REW_SP[getRandom(REW_SP.length)], 1);
								}
								st.takeItems(MONK_CLOTHES, iEvent);
								htmltext = "32757-09.html";
							}
							break;
						case 200:
							if (mcc >= iEvent)
							{
								st.giveItems(REW_REC[getRandom(REW_REC.length)], 1);
								st.giveItems(REW_SP[getRandom(REW_SP.length)], 1);
								st.takeItems(MONK_CLOTHES, iEvent);
								htmltext = "32757-09.html";
							}
							break;
						case 300:
							if (mcc >= iEvent)
							{
								st.giveItems(REW_REC[getRandom(REW_REC.length)], 1);
								st.giveItems(REW_SP[getRandom(REW_SP.length)], 1);
								st.giveItems(REW_PARTS[getRandom(REW_PARTS.length)], 1);
								st.takeItems(MONK_CLOTHES, iEvent);
								htmltext = "32757-09.html";
							}
							break;
						case 400:
							if (mcc >= iEvent)
							{
								if (getRandom(100) < 50)
								{
									st.giveItems(REW_REC[getRandom(REW_REC.length)], 2);
									st.giveItems(REW_SP[getRandom(REW_SP.length)], 1);
								}
								else
								{
									st.giveItems(REW_REC[getRandom(REW_REC.length)], 1);
									st.giveItems(REW_SP[getRandom(REW_SP.length)], 2);
									st.giveItems(REW_PARTS[getRandom(REW_PARTS.length)], 1);
								}
								st.takeItems(MONK_CLOTHES, iEvent);
								htmltext = "32757-09.html";
							}
							break;
						case 500:
							if (mcc >= iEvent)
							{
								st.giveItems(REW_REC[getRandom(REW_REC.length)], 2);
								st.giveItems(REW_SP[getRandom(REW_SP.length)], 2);
								st.giveItems(REW_PARTS[getRandom(REW_PARTS.length)], 1);
								st.takeItems(MONK_CLOTHES, iEvent);
								htmltext = "32757-09.html";
							}
							break;
						default:
							htmltext = "32757-10.html";
							break;
					}
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		final QuestState qs = player.getQuestState("10288_SecretMission");
		
		if (npc.getNpcId() == GREYMORE)
		{
			switch (st.getState())
			{
				case State.CREATED:
					if ((player.getLevel() >= 82) && (qs != null) && (qs.getState() == State.COMPLETED))
					{
						htmltext = "32757-01.htm";
					}
					else
					{
						htmltext = "32757-03.html";
					}
					break;
				case State.STARTED:
					htmltext = "32757-05.html";
					break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		L2PcInstance partyMember = getRandomPartyMember(player, "1");
		if (partyMember == null)
		{
			return super.onKill(npc, player, isPet);
		}
		
		final QuestState st = partyMember.getQuestState(qn);
		int chance = (int) (CHANCE * Config.RATE_QUEST_DROP);
		
		if (st != null)
		{
			int numItems = (chance / 100);
			chance = chance % 100;
			if (getRandom(100) < chance)
			{
				numItems++;
			}
			if (numItems > 0)
			{
				st.playSound("ItemSound.quest_itemget");
				st.giveItems(MONK_CLOTHES, numItems);
			}
		}
		
		if (player.getParty() != null)
		{
			QuestState qs;
			for (L2PcInstance pmember : player.getParty().getMembers())
			{
				qs = pmember.getQuestState(qn);
				
				if ((qs != null) && (qs.getInt("cond") == 1) && (pmember.getObjectId() != partyMember.getObjectId()))
				{
					int numItems = (chance / 100);
					chance = chance % 100;
					if (getRandom(100) < chance)
					{
						numItems++;
					}
					if (numItems > 0)
					{
						st.playSound("ItemSound.quest_itemget");
						st.giveItems(MONK_CLOTHES, numItems);
					}
				}
			}
		}
		return super.onKill(npc, player, isPet);
	}
	
	public Q270_TheOneWhoEndsSilence(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(GREYMORE);
		addTalkId(GREYMORE);
		addKillId(SOLINA_NPCS);
		addKillId(DIVINITY_NPCS);
		questItemIds = new int[]
		{
			MONK_CLOTHES
		};
	}
	
	public static void main(String[] args)
	{
		new Q270_TheOneWhoEndsSilence(270, qn, "The One Who Ends Silence");
	}
}