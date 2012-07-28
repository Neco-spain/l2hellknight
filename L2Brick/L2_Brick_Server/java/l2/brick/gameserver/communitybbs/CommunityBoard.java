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
package l2.brick.gameserver.communitybbs;

import java.util.StringTokenizer;

import l2.brick.Config;

import l2.brick.gameserver.communitybbs.Manager.BuffBBSManager;
import l2.brick.gameserver.communitybbs.Manager.ClanBBSManager;
import l2.brick.gameserver.communitybbs.Manager.ClassBBSManager;
import l2.brick.gameserver.communitybbs.Manager.PostBBSManager;
import l2.brick.gameserver.communitybbs.Manager.RegionBBSManager;
import l2.brick.gameserver.communitybbs.Manager.ShopBBSManager;
import l2.brick.gameserver.communitybbs.Manager.TeleportBBSManager;
import l2.brick.gameserver.communitybbs.Manager.TopBBSManager;
import l2.brick.gameserver.communitybbs.Manager.TopicBBSManager;
import l2.brick.gameserver.datatables.MultiSell;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.entity.TvTEvent;
import l2.brick.gameserver.model.entity.TvTRoundEvent;
import l2.brick.gameserver.network.L2GameClient;
import l2.brick.gameserver.network.SystemMessageId;
import l2.brick.gameserver.network.serverpackets.ShowBoard;

public class CommunityBoard
{
	private CommunityBoard()
	{
	}
	
	public static CommunityBoard getInstance()
	{
		return SingletonHolder._instance;
	}

	public boolean checkPlayerConditions(L2PcInstance player)
   	{
		if (Config.cbInOlympiadMode && player.isInOlympiadMode())
		{
		player.sendMessage("CommunityBoard useing is prohibited at the Olympiad");
		return false;
		}
		if (Config.cbInFlying && player.isFlyingMounted())
		{
		player.sendMessage("CommunityBoard useing is prohibited at Fly mode!");
		return false;
		}
		if (Config.cbInObserveMode && player.inObserverMode())
		{
		player.sendMessage("CommunityBoard useing is prohibited in ObserveMode!");
		return false;
		}
		if (Config.cbInDead && player.isAlikeDead() || player.isDead())
		{
		player.sendMessage("CommunityBoard useing is prohibited While Dead");
		return false;
		}
		if (Config.cbInKarma && player.getKarma() > 0)
		{
		player.sendMessage("CommunityBoard useing is prohibited with negative Karma!");
		return false;
		}
		if (Config.cbInSiege && player.isInSiege())
		{
		player.sendMessage("CommunityBoard useing is prohibited at the Siege!");
		return false;
		}
		if (Config.cbInCombat && player.isInCombat())
		{
		player.sendMessage("CommunityBoard useing is prohibited in Combat!");
		return false;
		}
		if (Config.cbInCast && player.isCastingNow())
		{
		player.sendMessage("CommunityBoard useing is prohibited while Casting!");
		return false;
		}	
		if (Config.cbInAttack && player.isAttackingNow())
		{
		player.sendMessage("CommunityBoard useing is prohibited while Attacking!");
		return false;
		}		
		if (Config.cbInTransform && player.isTransformed())
		{
		player.sendMessage("CommunityBoard useing is prohibited while Transformed!");
		return false;
		}			
		if (Config.cbInDuel && player.isInDuel())
		{
		player.sendMessage("CommunityBoard useing is prohibited while Playing Duel!");
		return false;
		}			
		if (Config.cbInFishing && player.isFishing())
		{
		player.sendMessage("CommunityBoard useing is prohibited while Fishing!");
		return false;
		}	
		if (Config.cbInVechile && player.isInVehicle())
		{
		player.sendMessage("CommunityBoard useing is prohibited In VehicleMode!");
		return false;
		}		
		if (Config.cbInStore && player.isInStoreMode())
		{
		player.sendMessage("CommunityBoard useing is prohibited In StoreMode!");
		return false;
		}
		if (Config.cbInTvT && TvTEvent.isPlayerParticipant(player.getObjectId()))
		{
		player.sendMessage("CommunityBoard useing is prohibited While TvTEvent Is Started!");
		return false;
		}
		if (Config.cbInTvTRound && TvTRoundEvent.isPlayerParticipant(player.getObjectId()))
		{
		player.sendMessage("CommunityBoard useing is prohibited While TvTRoundEvent Is Started!");
		return false;
		}
      return true;		
    }	
	
	public void handleCommands(L2GameClient client, String command)
	{
		L2PcInstance activeChar = client.getActiveChar();
		if (activeChar == null)
				return;
	
		if(!checkPlayerConditions(activeChar))
		    return;
		
		switch (Config.COMMUNITY_TYPE)
		{
			default:
			case 0: 
				activeChar.sendPacket(SystemMessageId.CB_OFFLINE);
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
