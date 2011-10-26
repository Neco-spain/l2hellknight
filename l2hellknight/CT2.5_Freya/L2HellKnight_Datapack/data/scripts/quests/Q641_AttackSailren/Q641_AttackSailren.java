package quests.Q641_AttackSailren;

import l2.hellknight.Config;
import l2.hellknight.gameserver.cache.HtmCache;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;
import l2.hellknight.util.Rnd;

public class Q641_AttackSailren extends Quest
{
	// Quest
	public static String qn = "641_AttackSailren"; 
	public static boolean DEBUG = false;
	public static int DROP_CHANCE = 40;
	
	// NPC
	public static int _statue = 32109;
	public static int[] _mobs = 
	{
		22196,
		22197,
		22198,
		22218,
		22223,
		22199
	};
	
	// Quest Items
	public static int GAZKH_FRAGMENT = 8782;
	public static int GAZKH = 8784;
	
	public Q641_AttackSailren(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addTalkId(_statue);
		addStartNpc(_statue);
		for (int npcId : _mobs)
			addKillId(npcId);
	}

	public static String getNoQuestMsg(L2PcInstance player)
	{
		String DEFAULT_NO_QUEST_MSG = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>";
		final String result = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/html/noquest.htm");
		if (result != null && result.length() > 0)
			return result;
		
		return DEFAULT_NO_QUEST_MSG;
	}
	
	@Override
	public String onAdvEvent (String event, L2Npc npc, L2PcInstance player)
	{
		if (DEBUG)
			player.sendMessage("onAdvEvent: " + event + " npcId: " + npc.getNpcId());
		
		String htmltext = getNoQuestMsg(player);
		
		QuestState st = player.getQuestState(qn);
		if (st != null)
		{
			if (event.equalsIgnoreCase("32109-1.htm"))
				htmltext = "32109-2.htm";
			else if (event.equalsIgnoreCase("32109-2.htm"))
			{
				st.setState(State.STARTED);
				st.set("cond","1");
				st.playSound("ItemSound.quest_accept");
				htmltext = "32109-3.htm";
			}
			else if (event.equalsIgnoreCase("32109-5.htm"))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
				sm.addString("Shilen's Protection");
				player.sendPacket(sm);
				st.giveItems(GAZKH,1);
				st.set("cond","3");
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(true);
				htmltext = "32109-5.htm";
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		if (DEBUG)
			player.sendMessage("onTalk: " + npc.getNpcId());
		
		String htmltext = getNoQuestMsg(player);
		if (npc.getNpcId() == _statue)
		{
			QuestState st = player.getQuestState(qn);
			if (st == null)
			{
				st = newQuestState(player);
				st.set("cond", "0");
			}
				
			try
			{
				byte id = st.getState();
				String cond = st.get("cond");
				if (cond == null || cond == "0")
				{
					QuestState prevSt = player.getQuestState("126_TheNameOfEvil2");
					if (prevSt != null)
					{
						byte prevId = prevSt.getState();
						if (prevId != State.COMPLETED)
							htmltext = "<html><body>You have to complete quest The Name of Evil 2 in order to begin this one!</body></html>";
						else if (id == State.COMPLETED && st.getQuestItemsCount(GAZKH) == 1)  
							htmltext = "<html><body>This quest has already been completed.</body></html>";
						else
						
							htmltext = "32109-1.htm";
					}
					else
						htmltext = "<html><body>You have to complete quest The Name of Evil 2 in order to begin this one!</body></html>";
				}
				else if (cond == "1")
				{
					if (st.getQuestItemsCount(GAZKH_FRAGMENT) >= 30)
					{
						st.takeItems(GAZKH_FRAGMENT,30);
						st.set("cond","2");
						st.playSound("ItemSound.quest_middle");
						htmltext = "32109-4.htm";
					}
					else
						htmltext = "<html><body> Please come back once you have 30 Gazkh Fragments. </body></html>";
				}
				else if (cond == "2")
				{
					startQuestTimer("32109-5.htm", 0, npc, player);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if (DEBUG)
			player.sendMessage("onKill: " + npc.getNpcId());
		
		for(int npcId : _mobs)
		{
			if (npc.getNpcId() == npcId)
			{
				QuestState st = player.getQuestState(qn);
				if (st == null)
					return null;
				else
				{
					try
					{
						int chance = Rnd.get(100);
						int cond = Integer.parseInt(st.get("cond"));
						if (cond == 1 && DROP_CHANCE >= chance)
						{
							st.giveItems(GAZKH_FRAGMENT,1);
							st.playSound("ItemSound.quest_itemget");
						}
					}
					catch(NumberFormatException nfe)
					{}
				}
			}
		}
		return null;
	}

	public static void main(String[] args)
	{
		new Q641_AttackSailren(641, qn, "Attack Sailren");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Quest: Attack Sailren");
	}
}