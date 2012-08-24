/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package quests.Q309_ForAGoodCause;

import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;
import l2.hellknight.gameserver.network.serverpackets.RadarControl;
import l2.hellknight.gameserver.util.Util;

/**
 * For A Good Cause (309).<br>
 * 2011/09/30 Based on official server Naia.<br>
 * Thanks to Belgarion.
 * @author nonom, Zoey76
 */
public class Q309_ForAGoodCause extends Quest
{
	private static final String qn = "309_ForAGoodCause";
	
	// NPC's
	private static final int ATRA = 32647;
	
	// Mobs
	private static final int CONTAMINATED_MUCROKIAN = 22654;
	private static final int CHANGED_MUCROKIAN = 22655;
	private static final int[] MUCROKIANS =
	{
		22650,
		22651,
		22652,
		22653
	};
	
	// Quest Items
	private static final int MUCROKIAN_HIDE = 14873;
	private static final int FALLEN_MUCROKIAN_HIDE = 14874;
	
	private static final int MUCROKIAN_HIDE_CHANCE = 50;
	private static final int FALLEN_HIDE_CHANCE = 50;
	
	// Rewards
	private static final int REC_DYNASTY_EARRINGS_70 = 9985;
	private static final int REC_DYNASTY_NECKLACE_70 = 9986;
	private static final int REC_DYNASTY_RING_70 = 9987;
	private static final int REC_DYNASTY_SIGIL_60 = 10115;
	
	private static final int REC_MOIRAI_CIRCLET_60 = 15777;
	private static final int REC_MOIRAI_TUNIC_60 = 15780;
	private static final int REC_MOIRAI_STOCKINGS_60 = 15783;
	private static final int REC_MOIRAI_GLOVES_60 = 15786;
	private static final int REC_MOIRAI_SHOES_60 = 15789;
	private static final int REC_MOIRAI_SIGIL_60 = 15790;
	private static final int REC_MOIRAI_EARRING_70 = 15814;
	private static final int REC_MOIRAI_NECKLACE_70 = 15813;
	private static final int REC_MOIRAI_RING_70 = 15812;
	
	private static final int[] MOIRAI_PIECES =
	{
		15647,
		15650,
		15653,
		15656,
		15659,
		15692,
		15772,
		15773,
		15774
	};
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return getNoQuestMsg(player);
		}
		
		String htmltext = event;
		if (event.equalsIgnoreCase("32647-05.html"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");
			player.sendPacket(new RadarControl(0, 2, 77325, 205773, -3432));
		}
		else if (event.equalsIgnoreCase("claimreward"))
		{
			final QuestState qs = player.getQuestState("239_WontYouJoinUs");
			if (qs != null)
			{
				htmltext = (qs.isCompleted()) ? "32647-11.html" : "32647-10.html";
			}
			else
			{
				htmltext = "32647-09.html";
			}
		}
		else if (event.equalsIgnoreCase("receivepieces"))
		{
			htmltext = onPiecesExchangeRequest(st, MOIRAI_PIECES[getRandom(MOIRAI_PIECES.length - 1)], 100);
		}
		else if (Util.isDigit(event))
		{
			int val = Integer.parseInt(event);
			switch (val)
			{
				case 96:
					htmltext = onRecipeExchangeRequest(st, REC_DYNASTY_EARRINGS_70, FALLEN_MUCROKIAN_HIDE, Integer.parseInt(event));
					break;
				case 192:
					htmltext = onRecipeExchangeRequest(st, REC_DYNASTY_EARRINGS_70, MUCROKIAN_HIDE, Integer.parseInt(event));
					break;
				case 256:
					htmltext = onRecipeExchangeRequest(st, REC_DYNASTY_NECKLACE_70, MUCROKIAN_HIDE, Integer.parseInt(event));
					break;
				case 64:
					htmltext = onRecipeExchangeRequest(st, REC_DYNASTY_RING_70, FALLEN_MUCROKIAN_HIDE, Integer.parseInt(event));
					break;
				case 103:
					htmltext = onRecipeExchangeRequest(st, REC_DYNASTY_SIGIL_60, FALLEN_MUCROKIAN_HIDE, Integer.parseInt(event));
					break;
				case 206:
					htmltext = onRecipeExchangeRequest(st, REC_DYNASTY_SIGIL_60, MUCROKIAN_HIDE, Integer.parseInt(event));
					break;
			}
		}
		else if (event.startsWith("circlet"))
		{
			if (event.endsWith("90"))
			{
				htmltext = onRecipeExchangeRequest(st, REC_MOIRAI_CIRCLET_60, FALLEN_MUCROKIAN_HIDE, 90);
			}
			else if (event.endsWith("180"))
			{
				htmltext = onRecipeExchangeRequest(st, REC_MOIRAI_CIRCLET_60, MUCROKIAN_HIDE, 180);
			}
		}
		else if (event.startsWith("stockings"))
		{
			if (event.endsWith("90"))
			{
				htmltext = onRecipeExchangeRequest(st, REC_MOIRAI_STOCKINGS_60, FALLEN_MUCROKIAN_HIDE, 90);
			}
			else if (event.endsWith("180"))
			{
				htmltext = onRecipeExchangeRequest(st, REC_MOIRAI_STOCKINGS_60, MUCROKIAN_HIDE, 180);
			}
		}
		else if (event.startsWith("tunic"))
		{
			if (event.endsWith("90"))
			{
				htmltext = onRecipeExchangeRequest(st, REC_MOIRAI_TUNIC_60, FALLEN_MUCROKIAN_HIDE, 90);
			}
			else if (event.endsWith("180"))
			{
				htmltext = onRecipeExchangeRequest(st, REC_MOIRAI_TUNIC_60, FALLEN_MUCROKIAN_HIDE, 180);
			}
		}
		else if (event.startsWith("gloves"))
		{
			if (event.endsWith("90"))
			{
				htmltext = onRecipeExchangeRequest(st, REC_MOIRAI_GLOVES_60, FALLEN_MUCROKIAN_HIDE, 90);
			}
			else if (event.endsWith("180"))
			{
				htmltext = onRecipeExchangeRequest(st, REC_MOIRAI_GLOVES_60, MUCROKIAN_HIDE, 180);
			}
		}
		else if (event.startsWith("shoes"))
		{
			if (event.endsWith("90"))
			{
				htmltext = onRecipeExchangeRequest(st, REC_MOIRAI_SHOES_60, FALLEN_MUCROKIAN_HIDE, 90);
			}
			else if (event.endsWith("180"))
			{
				htmltext = onRecipeExchangeRequest(st, REC_MOIRAI_SHOES_60, MUCROKIAN_HIDE, 180);
			}
		}
		else if (event.startsWith("sigil"))
		{
			if (event.endsWith("90"))
			{
				htmltext = onRecipeExchangeRequest(st, REC_MOIRAI_SIGIL_60, FALLEN_MUCROKIAN_HIDE, 90);
			}
			else if (event.endsWith("180"))
			{
				htmltext = onRecipeExchangeRequest(st, REC_MOIRAI_SIGIL_60, MUCROKIAN_HIDE, 180);
			}
		}
		else if (event.startsWith("earring"))
		{
			if (event.endsWith("90"))
			{
				htmltext = onRecipeExchangeRequest(st, REC_MOIRAI_EARRING_70, FALLEN_MUCROKIAN_HIDE, 90);
			}
			else if (event.endsWith("180"))
			{
				htmltext = onRecipeExchangeRequest(st, REC_MOIRAI_EARRING_70, MUCROKIAN_HIDE, 180);
			}
		}
		else if (event.startsWith("necklace"))
		{
			if (event.endsWith("90"))
			{
				htmltext = onRecipeExchangeRequest(st, REC_MOIRAI_NECKLACE_70, FALLEN_MUCROKIAN_HIDE, 90);
			}
			else if (event.endsWith("180"))
			{
				htmltext = onRecipeExchangeRequest(st, REC_MOIRAI_NECKLACE_70, MUCROKIAN_HIDE, 180);
			}
			else if (event.endsWith("128"))
			{
				htmltext = onRecipeExchangeRequest(st, REC_DYNASTY_NECKLACE_70, MUCROKIAN_HIDE, 128);
			}
		}
		else if (event.startsWith("ring"))
		{
			if (event.endsWith("90"))
			{
				htmltext = onRecipeExchangeRequest(st, REC_MOIRAI_RING_70, FALLEN_MUCROKIAN_HIDE, 90);
			}
			else if (event.endsWith("180"))
			{
				htmltext = onRecipeExchangeRequest(st, REC_MOIRAI_RING_70, MUCROKIAN_HIDE, 180);
			}
			else if (event.endsWith("128"))
			{
				htmltext = onRecipeExchangeRequest(st, REC_DYNASTY_RING_70, MUCROKIAN_HIDE, 128);
			}
		}
		else if (event.equalsIgnoreCase("32647-14.html") || event.equalsIgnoreCase("32647-07.html"))
		{
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(true);
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
		
		if (npc.getNpcId() == ATRA)
		{
			final QuestState qs = player.getQuestState("308_ReedFieldMaintenance");
			if ((qs != null) && qs.isStarted())
			{
				htmltext = "32647-17.html";
			}
			else if (st.isStarted())
			{
				htmltext = (st.hasQuestItems(MUCROKIAN_HIDE) || st.hasQuestItems(FALLEN_MUCROKIAN_HIDE)) ? "32647-08.html" : "32647-06.html";
			}
			else
			{
				htmltext = (player.getLevel() >= 82) ? "32647-01.htm" : "32647-00.html";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		final QuestState st = player.getQuestState(qn);
		if ((st != null) && (st.getInt("cond") == 1))
		{
			if (Util.contains(MUCROKIANS, npc.getNpcId()))
			{
				if (getRandom(100) < MUCROKIAN_HIDE_CHANCE)
				{
					st.giveItems(MUCROKIAN_HIDE, 1);
					st.playSound("ItemSound.quest_itemget");
				}
				else if ((npc.getNpcId() == CHANGED_MUCROKIAN) && (getRandom(100) < FALLEN_HIDE_CHANCE))
				{
					st.giveItems(FALLEN_MUCROKIAN_HIDE, 1);
					st.playSound("ItemSound.quest_itemget");
				}
				else if ((npc.getNpcId() == CONTAMINATED_MUCROKIAN) && (getRandom(100) < 10))
				{
					st.giveItems(MUCROKIAN_HIDE, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		return super.onKill(npc, player, isPet);
	}
	
	private String onPiecesExchangeRequest(QuestState st, int pieces, int event)
	{
		if (st.getQuestItemsCount(MUCROKIAN_HIDE) >= event)
		{
			st.giveItems(pieces, getRandom(1, 4));
			st.takeItems(MUCROKIAN_HIDE, event);
			st.playSound("ItemSound.quest_finish");
			return "32647-16.html";
		}
		return "32647-15.html";
	}
	
	private String onRecipeExchangeRequest(QuestState st, int recipe, int takeid, int quanty)
	{
		if (st.getQuestItemsCount(takeid) >= quanty)
		{
			st.giveItems(recipe, 1);
			st.takeItems(takeid, quanty);
			st.playSound("ItemSound.quest_finish");
			return "32647-16.html";
		}
		return "32647-15.html";
	}
	
	public Q309_ForAGoodCause(int id, String name, String descr)
	{
		super(id, name, descr);
		
		addStartNpc(ATRA);
		addTalkId(ATRA);
		addKillId(MUCROKIANS);
	}
	
	public static void main(String[] args)
	{
		new Q309_ForAGoodCause(309, qn, "For A Good Cause");
	}
}