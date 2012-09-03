package quests._10289_FadeToBlack;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.util.Util;

public class _10289_FadeToBlack extends Quest implements ScriptFile
{
	// NPC
	private static final int GREYMORE = 32757;
	// Items
	private static final int MARK_OF_DARKNESS = 15528;
	private static final int MARK_OF_SPLENDOR = 15527;
	//MOBs
	private static final int ANAYS = 25701;
	
	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}
	
	public _10289_FadeToBlack()
	{
		super(false);
		
		addStartNpc(GREYMORE);
		addTalkId(GREYMORE);
		addKillId(ANAYS);
	}
	
	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;
		
		if (st == null)
			return htmltext;
		
		if (npc.getNpcId() == GREYMORE)
		{
			if (event.equalsIgnoreCase("32757-04.htm"))
			{
				st.setState(STARTED);
				st.set("cond", "1");
				st.playSound(SOUND_ACCEPT);
			}
			else if(Util.isNumber(event) && st.getQuestItemsCount(MARK_OF_SPLENDOR) > 0)
			{
				int itemId = Integer.parseInt(event);
				st.takeItems(MARK_OF_SPLENDOR, 1);
				st.giveItems(itemId, 1);
				st.playSound(SOUND_FINISH);
				st.exitCurrentQuest(false);
				htmltext = "32757-08.htm";
			}
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		QuestState qs = st.getPlayer().getQuestState("_10288_SecretMission");
		
		if (npc.getNpcId() == GREYMORE)
		{
			switch(st.getState())
			{
				case CREATED :
					if (st.getPlayer().getLevel() >= 82 && qs != null && qs.getState() == COMPLETED)
						htmltext = "32757-02.htm";
					else if (st.getPlayer().getLevel() < 82)
						htmltext = "32757-00.htm";
					else
						htmltext = "32757-01.htm";
					break;
				case STARTED :
					if (st.getInt("cond") == 1)
						htmltext = "32757-04b.htm";
					if (st.getInt("cond") == 2 && st.getQuestItemsCount(MARK_OF_DARKNESS) > 0)
					{
						htmltext = "32757-05.htm";
						st.takeItems(MARK_OF_DARKNESS, 1);
						st.getPlayer().addExpAndSp(55983, 136500);
						st.set("cond","1");
						st.playSound(SOUND_MIDDLE);
					}
					else if (st.getInt("cond") == 3)
						htmltext = "32757-06.htm";
					break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2NpcInstance npc, QuestState st)
	{
		/*L2PcInstance partyMember = getRandomPartyMember(player,"1");
		
		if (partyMember == null)
			return super.onKill(npc, player, isPet);
		
		QuestState st = partyMember.getQuestState(qn);
		
		if (st != null)
		{
			st.giveItems(MARK_OF_SPLENDOR, 1);
			st.playSound(SOUND_ITEMGET);
			st.set("cond","3");
		}
		
		if (st.getPlayer().getParty() != null)
		{
			QuestState st2;
			for(L2PcInstance pmember : st.getPlayer().getParty().getPartyMembers())
			{
				st2 = pmember.getQuestState(qn);
				
				if(st2 != null && st2.getInt("cond") == 1 && pmember.getObjectId() != partyMember.getObjectId())
				{
					st2.giveItems(MARK_OF_DARKNESS, 1);
					st2.playSound(SOUND_ITEMGET);
					st2.set("cond","2");
				}
			}
		}
		
		return super.onKill(npc, player, isPet);*/
		return null;
	}
}