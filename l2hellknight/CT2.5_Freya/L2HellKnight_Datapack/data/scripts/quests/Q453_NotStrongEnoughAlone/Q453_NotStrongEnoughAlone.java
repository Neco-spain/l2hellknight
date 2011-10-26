package quests.Q453_NotStrongEnoughAlone;

import java.util.Calendar;

import l2.hellknight.Config;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;
import l2.hellknight.gameserver.util.Util;
import l2.hellknight.util.Rnd;

public class Q453_NotStrongEnoughAlone extends Quest
{
	private static final String qn = "453_NotStrongEnoughAlone";
	// NPC
	private static final int Klemis = 32734;
	private static final int[] Monsters1 = { 22746, 22747, 22748, 22749, 22750, 22751, 22752, 22753 };
	private static final int[] Monsters2 = { 22754, 22755, 22756, 22757, 22758, 22759 };
	private static final int[] Monsters3 = { 22760, 22761, 22762, 22763, 22764, 22765 };
	
	// Restart Time
	private static final int ResetHour = 6;
	private static final int ResetMin = 30;
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("32734-06.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("32734-07.html"))
		{
			st.set("cond", "2");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("32734-08.html"))
		{
			st.set("cond", "3");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("32734-09.html"))
		{
			st.set("cond", "4");
			st.playSound("ItemSound.quest_middle");
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		QuestState prev = player.getQuestState("10282_ToTheSeedOfAnnihilation");
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 84 && prev != null && prev.getState() == State.COMPLETED)
					htmltext = "32734-01.htm";
				else
					htmltext = "32734-03.html";
				break;
			case State.STARTED:
				if (st.getInt("cond") == 1)
					htmltext = "32734-10.html";
				else if (st.getInt("cond") == 2)
					htmltext = "32734-11.html";
				else if (st.getInt("cond") == 3)
					htmltext = "32734-12.html";
				else if (st.getInt("cond") == 4)
					htmltext = "32734-13.html";
				else if (st.getInt("cond") == 5)
				{
					boolean i1 = Rnd.nextBoolean();
					int i0 = Rnd.get(100);
					if (i1)
					{
						if (i0 < 9)
							st.giveItems(15815, 1);
						else if (i0 < 18)
							st.giveItems(15816, 1);
						else if (i0 < 27)
							st.giveItems(15817, 1);
						else if (i0 < 36)
							st.giveItems(15818, 1);
						else if (i0 < 47)
							st.giveItems(15819, 1);
						else if (i0 < 56)
							st.giveItems(15820, 1);
						else if (i0 < 65)
							st.giveItems(15821, 1);
						else if (i0 < 74)
							st.giveItems(15822, 1);
						else if (i0 < 83)
							st.giveItems(15823, 1);
						else if (i0 < 92)
							st.giveItems(15824, 1);
						else
							st.giveItems(15825, 1);
					}
					else
					{
						if (i0 < 9)
							st.giveItems(15634, 1);
						else if (i0 < 18)
							st.giveItems(15635, 1);
						else if (i0 < 27)
							st.giveItems(15636, 1);
						else if (i0 < 36)
							st.giveItems(15637, 1);
						else if (i0 < 47)
							st.giveItems(15638, 1);
						else if (i0 < 56)
							st.giveItems(15639, 1);
						else if (i0 < 65)
							st.giveItems(15640, 1);
						else if (i0 < 74)
							st.giveItems(15641, 1);
						else if (i0 < 83)
							st.giveItems(15642, 1);
						else if (i0 < 92)
							st.giveItems(15643, 1);
						else
							st.giveItems(15644, 1);
					}
					st.exitQuest(false);
					st.playSound("ItemSound.quest_finish");
					htmltext = "32734-14.html";
					
					Calendar reset = Calendar.getInstance();
					reset.set(Calendar.MINUTE, ResetMin);
					if (reset.get(Calendar.HOUR_OF_DAY) >= ResetHour)
						reset.add(Calendar.DATE, 1);
					reset.set(Calendar.HOUR_OF_DAY, ResetHour);
					st.set("reset", String.valueOf(reset.getTimeInMillis()));
				}
				break;
			case State.COMPLETED:
				if (Long.parseLong(st.get("reset")) > System.currentTimeMillis())
					htmltext = "32734-02.htm";
				else
				{
					st.setState(State.CREATED);
					if (player.getLevel() >= 84 && prev != null && prev.getState() == State.COMPLETED)
						htmltext = "32734-01.htm";
					else
						htmltext = "32734-03.html";
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if (player.getParty() != null)
		{
			for (L2PcInstance member : player.getParty().getPartyMembers())
			{
				increaseNpcKill(member, npc);
			}
		}
		else
		{
			increaseNpcKill(player, npc);
		}
		return null;
	}
	
	private void increaseNpcKill(L2PcInstance player, L2Npc npc)
	{
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
			return;
		
		if (Util.contains(Monsters1, npc.getNpcId()) && st.getInt("cond") == 2)
		{
			int val = 0;
			
			if (npc.getNpcId() == Monsters1[0] || npc.getNpcId() == Monsters1[4])
				val = Monsters1[0];
			else if (npc.getNpcId() == Monsters1[1] || npc.getNpcId() == Monsters1[5])
				val = Monsters1[1];
			else if (npc.getNpcId() == Monsters1[2] || npc.getNpcId() == Monsters1[6])
				val = Monsters1[2];
			else if (npc.getNpcId() == Monsters1[3] || npc.getNpcId() == Monsters1[7])
				val = Monsters1[3];
			
			int i = st.getInt(String.valueOf(val));
			if (i < 15)
				st.set(String.valueOf(val), String.valueOf(i + 1));
			
			if (st.getInt(String.valueOf(Monsters1[0])) >= 15 && st.getInt(String.valueOf(Monsters1[1])) >= 15 && st.getInt(String.valueOf(Monsters1[2])) >= 15 && st.getInt(String.valueOf(Monsters1[3])) >= 15)
			{
				st.set("cond", "5");
				st.playSound("ItemSound.quest_middle");
			}
			else
				st.playSound("ItemSound.quest_itemget");
		}
		else if (Util.contains(Monsters2, npc.getNpcId()) && st.getInt("cond") == 3)
		{
			int val = 0;
			
			if (npc.getNpcId() == Monsters2[0] || npc.getNpcId() == Monsters2[3])
				val = Monsters2[0];
			else if (npc.getNpcId() == Monsters2[1] || npc.getNpcId() == Monsters2[4])
				val = Monsters2[1];
			else if (npc.getNpcId() == Monsters2[2] || npc.getNpcId() == Monsters2[5])
				val = Monsters2[2];
			
			int i = st.getInt(String.valueOf(val));
			if (i < 20)
				st.set(String.valueOf(val), String.valueOf(i + 1));
			
			if (st.getInt(String.valueOf(Monsters2[0])) >= 20 && st.getInt(String.valueOf(Monsters2[1])) >= 20 && st.getInt(String.valueOf(Monsters2[2])) >= 20)
			{
				st.set("cond", "5");
				st.playSound("ItemSound.quest_middle");
			}
			else
				st.playSound("ItemSound.quest_itemget");
		}
		else if (Util.contains(Monsters3, npc.getNpcId()) && st.getInt("cond") == 4)
		{
			int val = 0;
			
			if (npc.getNpcId() == Monsters3[0] || npc.getNpcId() == Monsters3[3])
				val = Monsters3[0];
			else if (npc.getNpcId() == Monsters3[1] || npc.getNpcId() == Monsters3[4])
				val = Monsters3[1];
			else if (npc.getNpcId() == Monsters3[2] || npc.getNpcId() == Monsters3[5])
				val = Monsters3[2];
			
			int i = st.getInt(String.valueOf(val));
			if (i < 20)
				st.set(String.valueOf(val), String.valueOf(i + 1));
			
			if (st.getInt(String.valueOf(Monsters3[0])) >= 20 && st.getInt(String.valueOf(Monsters3[1])) >= 20 && st.getInt(String.valueOf(Monsters3[2])) >= 20)
			{
				st.set("cond", "5");
				st.playSound("ItemSound.quest_middle");
			}
			else
				st.playSound("ItemSound.quest_itemget");
		}
	}
	
	public Q453_NotStrongEnoughAlone(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(Klemis);
		addTalkId(Klemis);
		
		for (int i : Monsters1)
		{
			addKillId(i);
		}
		for (int i : Monsters2)
		{
			addKillId(i);
		}
		for (int i : Monsters3)
		{
			addKillId(i);
		}
	}
	
	public static void main(String[] args)
	{
		new Q453_NotStrongEnoughAlone(453, qn, "Not Strong Enought Alone");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Quest: Not Strong Enought Alone");
	}
}