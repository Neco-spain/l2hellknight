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
package quests.Q10294_SevenSignToTheMonastery;

import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.State;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.network.serverpackets.SocialAction;

/**
 * @author SquiD
 *
 */

public final class Q10294_SevenSignToTheMonastery extends Quest
{
	private static final String	qn = "Q10294_SevenSignToTheMonastery";
	
	// NPCs
	private static int AbyssalSaintessElcadia = 32784;
	private static int AbyssalSaintessElcadia2 = 32785;
	private static int ErissEvilThoughts = 32792;

	private static int RelicGuardian = 32803;

	private static int WestRelicWatcher = 32804;
	private static int NorthRelicWatcher = 32805;
	private static int EastRelicWatcher = 32806;
	private static int SouthRelicWatcher = 32807;

	private static int WestTeleportControlDevice = 32816;
	private static int NorthTeleportControlDevice = 32817;
	private static int EastTeleportControlDevice = 32818;
	private static int SouthTeleportControlDevice = 32819;

	private static int WestReadingDesk1 = 32821;
	private static int WestReadingDesk2 = 32822;
	private static int WestReadingDesk3 = 32823;
	private static int WestReadingDesk4 = 32824;

	private static int NorthReadingDesk1 = 32825;
	private static int NorthReadingDesk2 = 32826;
	private static int NorthReadingDesk3 = 32827;
	private static int NorthReadingDesk4 = 32828;

	private static int EastReadingDesk1 = 32829;
	private static int EastReadingDesk2 = 32830;
	private static int EastReadingDesk3 = 32831;
	private static int EastReadingDesk4 = 32832;

	private static int SouthReadingDesk1 = 32833;
	private static int SouthReadingDesk2 = 32834;
	private static int SouthReadingDesk3 = 32835;
	private static int SouthReadingDesk4 = 32836;
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;	
		else if("32784-03.htm".equals(event))
		{
            st.set("cond","1");
			st.setState(State.STARTED);
            st.playSound("ItemSound.quest_accept");
		}
		else if("32792-03.htm".equals(event))
		{
			st.set("cond","2");
			st.playSound("ItemSound.quest_middle");		
		}
		else if("32804-04.htm".equals(event))
		{
			if(st.getInt("book_" + WestReadingDesk1) > 0)
				htmltext = "32804-05.htm";
		}
		else if("32805-04.htm".equals(event))
		{
			if(st.getInt("book_" + NorthReadingDesk1) > 0)
				htmltext = "32805-05.htm";
		}
		else if("32806-04.htm".equals(event))
		{
			if(st.getInt("book_" + EastReadingDesk1) > 0)
				htmltext = "32806-05.htm";
		}
		else if("32807-04.htm".equals(event))
		{
			if(st.getInt("book_" + SouthReadingDesk1) > 0)
				htmltext = "32807-05.htm";
		}
		else if("32785-04.htm".equals(event))
		{
			// TODO Spawn Guard
		}
		else if("32785-05.htm".equals(event))
		{
			st.playSound("ItemSound.quest_middle");
			//npc.setState(1);
			//player.sendPacket(new ExChangeNPCState(npc));
			st.set("book_" + npc.getNpcId(), "1"); // it was now look for script
			if(isAllBooksFinded(st))
			{
				//player.sendPacket(new EventTrigger(22100500, false));
				//player.sendPacket(new EventTrigger(22100502, true));
				player.showQuestMovie(24); //ExStartScenePlayer.SCENE_SSQ2_HOLY_BURIAL_GROUND_CLOSING
			}
		}
		else if("32792-08.htm".equals(event))
		{
			if(player.getLevel() < 81)
				htmltext = "<html><body>Only characters who are <font color=\"LEVEL\">level 81</font> or higher may complete this quest.</body></html>";
			else
			{
				st.addExpAndSp(25000000, 2500000);
				player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
				st.exitQuest(false);
				st.playSound("ItemSound.quest_finish");
			}
		}
		else if("NotReady".equals(event))
			return null;
			
		return htmltext;
	}	

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		int npcId = npc.getNpcId();
		int cond = st.getInt("cond");
		if(npcId == AbyssalSaintessElcadia)
		{
			/*QuestState qs = player.getQuestState("Q10293_SevenSignsForbiddenBook");*/ //Turned Off Untli Quest isn't done
			if(cond == 0)
			{
				if(player.getLevel() >= 81/* && qs != null && qs.isCompleted()*/)
					htmltext = "32784-01.htm";
				else
				{
					htmltext = "32784-00.htm";
					st.exitQuest(true);
				}
			}
			else if(cond == 1)
				htmltext = "32784-04.htm";
		}
		else if(npcId == AbyssalSaintessElcadia2)
		{
			if(cond == 1)
				htmltext = "32785-01.htm";
			else if(cond == 2)
				htmltext = "32785-02.htm";
			else if(cond == 3)
				htmltext = "32785-03.htm";
		}
		else if(npcId == ErissEvilThoughts)
		{
			if(cond == 1)
				htmltext = "32792-01.htm";
			else if(cond == 2)
				htmltext = "32792-06.htm";
			else if(cond == 3)
				htmltext = "32792-07.htm";
		}
		else if(npcId == RelicGuardian)
		{
			if(cond == 2)
			{
				if(isAllBooksFinded(st))
				{
					htmltext = "32803-04.htm";
					st.set("cond","3");
					st.playSound("ItemSound.quest_middle");
				}
				else
					htmltext = "32803-01.htm";
			}
			else if(cond == 3)
				htmltext = "32803-05.htm";
		}
		else if(npcId == WestRelicWatcher)
			htmltext = "32804-01.htm";
		else if(npcId == NorthRelicWatcher)
			htmltext = "32805-01.htm";
		else if(npcId == EastRelicWatcher)
			htmltext = "32806-01.htm";
		else if(npcId == SouthRelicWatcher)
			htmltext = "32807-01.htm";
		else
			for(int[] d : desks)
				if(ArrayContains(d, npcId))
				{
					if(npcId == d[0])
						htmltext = npcId + "-01.htm";
					else
						htmltext = "empty_desk.htm";
					break;
				}
		return htmltext;			
	}	
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
     	  int npcId = npc.getNpcId();
		if(npcId == WestTeleportControlDevice)
			return "32816-01.htm";
		else if(npcId == NorthTeleportControlDevice)
			return "32817-01.htm";
		else if(npcId == EastTeleportControlDevice)
			return "32818-01.htm";
		else if(npcId == SouthTeleportControlDevice)
			return "32819-01.htm";

		for(int[] d : desks)
			if(ArrayContains(d, npcId))
			{
				if(npcId == d[0])
					return npcId + "-01.htm";
				return "empty_desk.htm";
			}

		return null;
	}	

	private boolean isAllBooksFinded(QuestState st)
	{
		return st.getInt("book_" + WestReadingDesk1) + st.getInt("book_" + NorthReadingDesk1) + st.getInt("book_" + EastReadingDesk1) + st.getInt("book_" + SouthReadingDesk1) >= 4;
	}
	
	private static int[][] desks = new int[][] 
	{
		{ WestReadingDesk1, WestReadingDesk2, WestReadingDesk3, WestReadingDesk4 },
		{ NorthReadingDesk1, NorthReadingDesk2, NorthReadingDesk3, NorthReadingDesk4 },
		{ EastReadingDesk1, EastReadingDesk2, EastReadingDesk3, EastReadingDesk4 },
		{ SouthReadingDesk1, SouthReadingDesk2, SouthReadingDesk3, SouthReadingDesk4 } 
	};	
	
	public Q10294_SevenSignToTheMonastery(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(AbyssalSaintessElcadia);
		addTalkId(AbyssalSaintessElcadia, ErissEvilThoughts, AbyssalSaintessElcadia2, RelicGuardian);
		addTalkId(WestRelicWatcher, NorthRelicWatcher, EastRelicWatcher, SouthRelicWatcher);
		addTalkId(WestTeleportControlDevice, NorthTeleportControlDevice, EastTeleportControlDevice, SouthTeleportControlDevice);
		addFirstTalkId(WestTeleportControlDevice, NorthTeleportControlDevice, EastTeleportControlDevice, SouthTeleportControlDevice);

		for(int[] ids : desks)
			addTalkId(ids);

		for(int[] ids : desks)
			addFirstTalkId(ids);
	}
	
	  public static boolean ArrayContains(int[] paramArrayOfInt, int paramInt)
	  {
	    for (int k : paramArrayOfInt)
	      if (k == paramInt)
	        return true;
	    return false;
	  }
	
	public static void main(String[] args)
	{
		new Q10294_SevenSignToTheMonastery(10294, qn, "Seven Sign To The Monastery");
	}
}