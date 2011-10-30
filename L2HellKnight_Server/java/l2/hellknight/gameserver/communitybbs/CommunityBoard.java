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
package l2.hellknight.gameserver.communitybbs;

import java.util.StringTokenizer;

import l2.hellknight.Config;

import l2.hellknight.gameserver.communitybbs.Manager.BuffBBSManager;
import l2.hellknight.gameserver.communitybbs.Manager.ClanBBSManager;
import l2.hellknight.gameserver.communitybbs.Manager.ClassBBSManager;
import l2.hellknight.gameserver.communitybbs.Manager.PostBBSManager;
import l2.hellknight.gameserver.communitybbs.Manager.RegionBBSManager;
import l2.hellknight.gameserver.communitybbs.Manager.ShopBBSManager;
import l2.hellknight.gameserver.communitybbs.Manager.TeleportBBSManager;
import l2.hellknight.gameserver.communitybbs.Manager.TopBBSManager;
import l2.hellknight.gameserver.communitybbs.Manager.TopicBBSManager;
import l2.hellknight.gameserver.datatables.MultiSell;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.entity.TvTEvent;
import l2.hellknight.gameserver.network.L2GameClient;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.ShowBoard;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;

public class CommunityBoard
{
	private CommunityBoard()
	{
	}
	
	public static CommunityBoard getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public void handleCommands(L2GameClient client, String command)
	{
		L2PcInstance activeChar = client.getActiveChar();
		if (activeChar == null)
				return;
	
     if(Config.RESTRICT_ZONE)	
	{
		if (activeChar.isInOlympiadMode())
		{
		activeChar.sendMessage("Can not be used at the Olympiad");
		return;
		}
		if (activeChar.isFlyingMounted())
		{
		activeChar.sendMessage("Can not be used at Fly mode!");
		return;
		}
		if (activeChar.inObserverMode())
		{
		activeChar.sendMessage("Can not be used in ObserveMode!");
		return;
		}
		if (activeChar.isAlikeDead() || activeChar.isDead())
		{
		activeChar.sendMessage("Can not be used While Dead");
		return;
		}
		if (activeChar.getKarma() > 0)
		{
		activeChar.sendMessage("Can not be used with negative Karma!");
		return;
		}
		if (activeChar.isInSiege())
		{
		activeChar.sendMessage("Can not be used at the Siege!");
		return;
		}
		if (activeChar.isInCombat())
		{
		activeChar.sendMessage("Can not be used in Combat!");
		return;
		}
		if (activeChar.isCastingNow())
		{
		activeChar.sendMessage("Can not be used while Casting!");
		return;
		}	
		if (activeChar.isAttackingNow())
		{
		activeChar.sendMessage("Can not be used while Attacking!");
		return;
		}		
		if (activeChar.isTransformed())
		{
		activeChar.sendMessage("Can not be used while Transformed!");
		return;
		}			
		if (activeChar.isInDuel())
		{
		activeChar.sendMessage("Can not be used while Playing Duel!");
		return;
		}			
		if (activeChar.isFishing())
		{
		activeChar.sendMessage("Can not be used while Fishing!");
		return;
		}
		if (activeChar.isInWater())
		{
		activeChar.sendMessage("Can not be used In Water!");
		return;
		}			
		if (activeChar.isInVehicle())
		{
		activeChar.sendMessage("Can not be used In VehicleMode!");
		return;
		}		
		if (activeChar.isInStoreMode())
		{
		activeChar.sendMessage("Can not be used In StoreMode!");
		return;
		}
		if (TvTEvent.isParticipating())
		{
		activeChar.sendMessage("Can not be used While TvT Is Started!");
		return;
		}	
	}
		
		switch (Config.COMMUNITY_TYPE)
		{
			default:
			case 0: 
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CB_OFFLINE));
				break;
			case 1: 
				RegionBBSManager.getInstance().parsecmd(command, activeChar);
				break;
			case 2: 
				if (command.startsWith("_bbsclan"))
				{
					ClanBBSManager.getInstance().parsecmd(command, activeChar);
				}
				else if (command.startsWith("_bbsmemo"))
				{
					TopicBBSManager.getInstance().parsecmd(command, activeChar);
				}
				else if (command.startsWith("_bbstopics"))
				{
					TopicBBSManager.getInstance().parsecmd(command, activeChar);
				}
				else if (command.startsWith("_bbsposts"))
				{
					PostBBSManager.getInstance().parsecmd(command, activeChar);
				}
				else if (command.startsWith("_bbstop"))
				{
					TopBBSManager.getInstance().parsecmd(command, activeChar);
				}
				else if (command.startsWith("_bbshome"))
				{
					TopBBSManager.getInstance().parsecmd(command, activeChar);
				}								
		        else if (command.startsWith("_bbsloc"))
				{
					RegionBBSManager.getInstance().parsecmd(command, activeChar);
				}
				else
				{
					ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: " + command
							+ " is not implemented yet</center><br><br></body></html>", "101");
					activeChar.sendPacket(sb);
					activeChar.sendPacket(new ShowBoard(null, "102"));
					activeChar.sendPacket(new ShowBoard(null, "103"));
				}
				break;
			case 3: 
				if (command.startsWith("_bbsclan"))
				{
					ClanBBSManager.getInstance().parsecmd(command, activeChar);
				}
				else if (command.startsWith("_bbsmemo"))
				{
					TopicBBSManager.getInstance().parsecmd(command, activeChar);
				}
				else if (command.startsWith("_bbstopics"))
				{
					TopicBBSManager.getInstance().parsecmd(command, activeChar);
				}
				else if (command.startsWith("_bbsposts"))
				{
					PostBBSManager.getInstance().parsecmd(command, activeChar);
				}
				else if (command.startsWith("_bbstop"))
				{
					TopBBSManager.getInstance().parsecmd(command, activeChar);
				}
				else if (command.startsWith("_bbshome"))
				{
					TopBBSManager.getInstance().parsecmd(command, activeChar);
				}
		        else if (command.startsWith("_bbs_buff"))
		        {
		          BuffBBSManager.getInstance().parsecmd(command, activeChar);
		        }				
		        else if (command.startsWith("_bbsclass"))
		        {
		          ClassBBSManager.getInstance().parsecmd(command, activeChar);
		        }
		        else if (command.startsWith("_bbsshop"))
		        {
		          ShopBBSManager.getInstance().parsecmd(command, activeChar);
		        }
		        else if (command.startsWith("_bbsmultisell;"))
		        {
		          StringTokenizer st = new StringTokenizer(command, ";");
		          st.nextToken();
		          ShopBBSManager.getInstance().parsecmd("_bbsshop;" + st.nextToken(), activeChar);
		          MultiSell.getInstance().separateAndSend(Integer.parseInt(st.nextToken()), activeChar, null, false);
		        }
				else if (command.startsWith("_bbsteleport"))
				{
				   if (Config.COMMUNITY_ALLOW_TELEPORT)
				       TeleportBBSManager.getInstance().parsecmd(command, activeChar);
				   else
				return;
				}
		        else if (command.startsWith("_bbsloc"))
				{
					RegionBBSManager.getInstance().parsecmd(command, activeChar);
				}				
				else
				{
					ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: " + command
							+ " is not implemented yet</center><br><br></body></html>", "101");
					activeChar.sendPacket(sb);
					activeChar.sendPacket(new ShowBoard(null, "102"));
					activeChar.sendPacket(new ShowBoard(null, "103"));
				}
				break;
		}
	}	
	
	/**
	 * @param client
	 * @param url
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @param arg5
	 */
	public void handleWriteCommands(L2GameClient client, String url, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
		L2PcInstance activeChar = client.getActiveChar();
		if (activeChar == null)
			return;
		
		switch (Config.COMMUNITY_TYPE)
		{
			case 3:
				if (url.equals("Topic"))
				{
					TopicBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
				}
				else if (url.equals("Post"))
				{
					PostBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
				}
				else if (url.equals("Region"))
				{
					RegionBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
				}
				else if (url.equals("Notice"))
				{
					ClanBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
				}
				else
				{
					ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: " + url
							+ " is not implemented yet</center><br><br></body></html>", "101");
					activeChar.sendPacket(sb);
					activeChar.sendPacket(new ShowBoard(null, "102"));
					activeChar.sendPacket(new ShowBoard(null, "103"));
				}			
			case 2:
				if (url.equals("Topic"))
				{
					TopicBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
				}
				else if (url.equals("Post"))
				{
					PostBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
				}
				else if (url.equals("Region"))
				{
					RegionBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
				}
				else if (url.equals("Notice"))
				{
					ClanBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
				}
				else
				{
					ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: " + url
							+ " is not implemented yet</center><br><br></body></html>", "101");
					activeChar.sendPacket(sb);
					activeChar.sendPacket(new ShowBoard(null, "102"));
					activeChar.sendPacket(new ShowBoard(null, "103"));
				}
				break;
			case 1:
				RegionBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
				break;
			default:
			case 0:
				ShowBoard sb = new ShowBoard("<html><body><br><br><center>The Community board is currently disabled</center><br><br></body></html>", "101");
				activeChar.sendPacket(sb);
				activeChar.sendPacket(new ShowBoard(null, "102"));
				activeChar.sendPacket(new ShowBoard(null, "103"));
				break;
		}
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final CommunityBoard _instance = new CommunityBoard();
	}
}
