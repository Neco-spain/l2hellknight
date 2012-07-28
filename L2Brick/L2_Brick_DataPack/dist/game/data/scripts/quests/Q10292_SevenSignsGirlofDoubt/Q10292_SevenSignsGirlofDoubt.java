package quests.Q10292_SevenSignsGirlofDoubt;

import javolution.util.FastList;
import javolution.util.FastMap;

import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;
import l2.brick.gameserver.model.quest.State;
import l2.brick.gameserver.util.Util;

public class Q10292_SevenSignsGirlofDoubt extends Quest
{
	private static final String qn = "Q10292_SevenSignsGirlofDoubt";
	// NPC
	private static final int Hardin = 30832;
	private static final int Wood = 32593;
	private static final int Franz = 32597;
	private static final int Elcadia = 32784;
	private static final int Gruff_looking_Man = 32862;
	private static final int Jeina = 32617;
	// Item
	private static final int Elcadias_Mark = 17226;
	// Mobs
	private static final int[] Mobs = { 22801, 22802, 22804, 22805 };
	
	private final FastMap<Integer, InstanceHolder> instanceWorlds = new FastMap<Integer, InstanceHolder>();
	
	private static class InstanceHolder
	{
		// List 
		FastList<L2Npc> mobs = new FastList<L2Npc>();
		// State 
		boolean spawned = false;
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		
		int instanceId = npc.getInstanceId();
		InstanceHolder holder = instanceWorlds.get(instanceId);
		if (holder == null && instanceId > 0)
		{
			holder = new InstanceHolder();
			instanceWorlds.put(instanceId, holder);
		}
		
		if (st == null)
			return htmltext;
		if (event.equalsIgnoreCase("evil_despawn"))
		{
			holder.spawned = false;
			for(L2Npc h : holder.mobs)
			{
				if(h != null)
					h.deleteMe();
			}
			holder.mobs.clear();
			instanceWorlds.remove(instanceId);
			return null;
		}
		else if (npc.getNpcId() == Wood)
		{
			if (event.equalsIgnoreCase("32593-05.htm"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if (npc.getNpcId() == Franz)
		{
			if (event.equalsIgnoreCase("32597-08.htm"))
			{
				st.set("cond", "2");
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if (npc.getNpcId() == Hardin)
		{
			if (event.equalsIgnoreCase("30832-02.html"))
			{
				st.set("cond", "8");
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if (npc.getNpcId() == Elcadia)
		{
			if (event.equalsIgnoreCase("32784-03.html"))
			{
				st.set("cond", "3");
				st.playSound("ItemSound.quest_middle");
			}
			else if (event.equalsIgnoreCase("32784-14.html"))
			{
				st.set("cond", "7");
				st.playSound("ItemSound.quest_middle");
			}
			else if (event.equalsIgnoreCase("spawn"))
			{
				if (!holder.spawned)
				{
					st.takeItems(Elcadias_Mark, -1);
					holder.spawned = true;
					L2Npc evil = addSpawn(27422, 89440, -238016, -9632, 335, false, 0, false, player.getInstanceId());
					evil.setIsNoRndWalk(true);
					holder.mobs.add(evil);
					L2Npc evil1 = addSpawn(27424, 89524, -238131, -9632, 56, false, 0, false, player.getInstanceId());
					evil1.setIsNoRndWalk(true);
					holder.mobs.add(evil1);
					startQuestTimer("evil_despawn", 60000, evil, player);
					return null;
				}
				else
				{
					htmltext = "32593-02.htm";
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
		{
			return htmltext;
		}
		else if (npc.getNpcId() == Wood)
		{
			if (st.getState() == State.COMPLETED)
				htmltext = "32593-02.htm";
			else if (player.getLevel() < 81)
				htmltext = "32593-03.htm";
			else if (player.getQuestState("Q198_SevenSignEmbryo") == null || player.getQuestState("Q198_SevenSignEmbryo").getState() != State.COMPLETED)
				htmltext = "32593-03.htm";
			else if (st.getState() == State.CREATED)
				htmltext = "32593-01.htm";
			else if (st.getInt("cond") >= 1)
				htmltext = "32593-07.html";
		}
		else if (npc.getNpcId() == Franz)
		{
			if (st.getInt("cond") == 1)
				htmltext = "32597-01.htm";
			else if (st.getInt("cond") == 2)
				htmltext = "32597-03.html";
		}
		else if (npc.getNpcId() == Elcadia)
		{
			if (st.getInt("cond") == 2)
			{
				htmltext = "32784-01.html";
			}
			else if (st.getInt("cond") == 3)
			{
				htmltext = "32784-04.html";
			}
			else if (st.getInt("cond") == 4)
			{
				st.playSound("ItemSound.quest_middle");
				st.set("cond", "5");
				htmltext = "32784-05.html";
			}
			else if (st.getInt("cond") == 5)
			{
				st.playSound("ItemSound.quest_middle");
				htmltext = "32784-05.html";
			}
			else if (st.getInt("cond") == 6)
			{
				st.playSound("ItemSound.quest_middle");
				htmltext = "32784-11.html";
			}
			else if (st.getInt("cond") == 8)
			{
				if (player.isSubClassActive())
				{
					htmltext = "32784-18.html";
				}
				else
				{
					st.playSound("ItemSound.quest_finish");
					st.addExpAndSp(10000000, 1000000);
					st.exitQuest(false);
					htmltext = "32784-16.html";
				}
			}
		}
		else if (npc.getNpcId() == Hardin)
		{
			if (st.getInt("cond") == 7)
				htmltext = "30832-01.html";
			else if (st.getInt("cond") == 8)
				htmltext = "30832-04.html";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(qn);
		
		if (st != null && st.getInt("cond") == 3 && Util.contains(Mobs, npc.getNpcId()) && st.getQuestItemsCount(Elcadias_Mark) < 10 && st.getQuestItemsCount(Elcadias_Mark) != 9)
		{
			st.giveItems(Elcadias_Mark, 1);
			st.playSound("ItemSound.quest_middle");
		}
		else if (st != null && st.getInt("cond") == 3 && Util.contains(Mobs, npc.getNpcId()) && st.getQuestItemsCount(Elcadias_Mark) >= 9)
		{
			st.giveItems(Elcadias_Mark, 1);
			st.playSound("ItemSound.quest_middle");
			st.set("cond", "4");
		}
		else if (st != null && st.getInt("cond") == 5 && npc.getNpcId() == 27422)
		{
			int instanceid = npc.getInstanceId();
			InstanceHolder holder = instanceWorlds.get(instanceid);
			if (holder == null)
				return null;
			for (L2Npc h : holder.mobs)
			{
				if (h != null)
					h.deleteMe();
			}
			holder.spawned = false;
			holder.mobs.clear();
			instanceWorlds.remove(instanceid);
			st.set("cond", "6");
			st.playSound("ItemSound.quest_middle");
		}
		return super.onKill(npc, player, isPet);
	}
	
	public Q10292_SevenSignsGirlofDoubt(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(Wood);
		addTalkId(Wood);
		addTalkId(Franz);
		addTalkId(Hardin);
		addTalkId(Elcadia);
		addTalkId(Gruff_looking_Man);
		addTalkId(Jeina);
		addKillId(27422);
		for (int _npc : Mobs)
		{
			addKillId(_npc);
		}
		
		questItemIds = new int[] { Elcadias_Mark };
	}
	
	public static void main(String[] args)
	{
		new Q10292_SevenSignsGirlofDoubt(10292, qn, "Seven Signs, Girl of Doubt");
	}
}
