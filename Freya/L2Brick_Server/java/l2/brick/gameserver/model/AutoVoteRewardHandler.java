/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
 
package l2.brick.gameserver.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import l2.brick.Config;
import l2.brick.gameserver.Announcements;
import l2.brick.gameserver.GmListTable;
import l2.brick.gameserver.ThreadPoolManager;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;

public class AutoVoteRewardHandler
{
	private int	lastVoteCount = 0;
	private int	initialCheck = 60 * 1000;														
	private int	delayForCheck = Config.DELAY_FOR_NEXT_REWARD * 1000;	
	
	private AutoVoteRewardHandler()

	{
		if (Config.VOTE_REWARD_ENABLE)
		{		
		System.out.println("========= "+Config.SERVER_NAME_FOR_VOTES+" =========");
		System.out.println("Vote Reward System activated");
		System.out.println("========= "+Config.SERVER_NAME_FOR_VOTES+" =========");
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AutoReward(), initialCheck, delayForCheck);
		}
	}
	

	private class AutoReward implements Runnable
	{
		public void run()		
		{
			if (Config.VOTE_REWARD_ENABLE)
			{
			System.out.println("");
			System.out.println("==================");
			System.out.println("Vote Count Check.");
			if (Config.VOTE_REWARD1_ID == 0 || Config.VOTE_REWARD1_COUNT == 0 || Config.VOTE_REWARD2_ID == 0 || Config.VOTE_REWARD2_COUNT == 0)
			{
				GmListTable.broadcastMessageToGMs("The rewards aren't Identified. Please take a look.");
				return;
			}
			int newVoteCount = getVotes(Config.VOTE_HTML_PATCH);
			System.out.println("getLastVoteCount:"+getLastVoteCount());
			System.out.println("newVoteCount:"+newVoteCount);
			System.out.println("==================");
			System.out.println("");
			if (newVoteCount != 0 && getLastVoteCount() != 0 && newVoteCount >= getLastVoteCount() + Config.VOTES_FOR_REWARD)
			{
				for (L2PcInstance player : L2World.getInstance().getAllPlayersArray())
				{
					if (player != null)
					{
							L2ItemInstance item1 = player.getInventory().getItemByItemId(Config.VOTE_REWARD1_ID);
							if (item1 == null || item1.getCount() < Config.MAX_REWARD_COUNT_FOR_STACK_ITEM1)
							{
								if (player.getAppearance().getNameColor() != Config.OFFLINE_NAME_COLOR)
								{
									player.addItem("reward", Config.VOTE_REWARD1_ID, Config.VOTE_REWARD1_COUNT, player, true);
								}
							}
							else
							{
								player.sendMessage("[" + Config.SERVER_NAME_FOR_VOTES+"]" + "You have reached our limit for vote reward items!!");
							}
							L2ItemInstance item2 = player.getInventory().getItemByItemId(Config.VOTE_REWARD2_ID);
							if (item2 == null || item2.getCount() < Config.MAX_REWARD_COUNT_FOR_STACK_ITEM2)
							{
								if (player.getAppearance().getNameColor() != Config.OFFLINE_NAME_COLOR)
								{
									player.addItem("reward", Config.VOTE_REWARD2_ID, Config.VOTE_REWARD2_COUNT, player, true);
								}
							}
							else
							{
								player.sendMessage("[" + Config.SERVER_NAME_FOR_VOTES+"]" + "You have reached our limit for vote reward items!!");
							}
					}
				}	
				setLastVoteCount(newVoteCount);
			}
			Announcements.getInstance().announceToAll("[" + Config.SERVER_NAME_FOR_VOTES+"]" + "Our Current vote count is: " + newVoteCount);
			Announcements.getInstance().announceToAll("[" + Config.SERVER_NAME_FOR_VOTES+"]" + "The next reward will be given at " + (getLastVoteCount()+ Config.VOTES_FOR_REWARD) + " Votes.");
			if (getLastVoteCount() == 0)
			{
				setLastVoteCount(newVoteCount);
			}
			}
		}
	}

	private int getVotes(String urlString)
	{
		InputStreamReader isr = null;
		BufferedReader in = null;
		try
		{
			URL url = new URL(urlString);
			URLConnection con = url.openConnection();
			con.addRequestProperty("User-Agent", "Mozilla/4.76"); 
			isr = new InputStreamReader(con.getInputStream());
			in = new BufferedReader(isr);
			String inputLine;
			int voteCount = 0;
			while ((inputLine = in.readLine()) != null)
			{
				if (inputLine.contains("moreinfo_total_rank_text"))
				{
					int Sub = 12;
					switch (inputLine.length())
					{
						case 116:
							Sub = 13; 
							break;
						case 117:
							Sub = 14; 
							break;
						case 118:
							Sub = 15;
							break;
						case 119:
							Sub = 16; 
							break;
					}
					voteCount = Integer.parseInt(inputLine.substring(inputLine.length() - Sub, inputLine.length() - 11));
					break;
				}
			}
			return voteCount;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return 0;
		}
		finally
		{
			try
			{
				in.close();
			}
			catch (IOException e)
			{
				
			}
			try
			{
				isr.close();
			}
			catch (IOException e)
			{
				
			}
		}
	}
	
	/**
	 * @return
	 */

	private void setLastVoteCount(int voteCount)
	{
		lastVoteCount = voteCount;
	}
	
	private int getLastVoteCount()
	{
		return lastVoteCount;
	}
	
	public static AutoVoteRewardHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final AutoVoteRewardHandler _instance = new AutoVoteRewardHandler();
	}
}