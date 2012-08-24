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
package l2.hellknight.gameserver.network.clientpackets;

import java.util.StringTokenizer;
import java.util.logging.Level;
import java.net.URL;
import java.net.URLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;

import javolution.text.TextBuilder;

import l2.hellknight.ExternalConfig;
import l2.hellknight.L2DatabaseFactory;

import l2.hellknight.Config;
import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.communitybbs.CommunityBoard;
import l2.hellknight.gameserver.datatables.AdminTable;
import l2.hellknight.gameserver.datatables.ItemTable;
import l2.hellknight.gameserver.handler.AdminCommandHandler;
import l2.hellknight.gameserver.handler.BypassHandler;
import l2.hellknight.gameserver.handler.IAdminCommandHandler;
import l2.hellknight.gameserver.handler.IBypassHandler;
import l2.hellknight.gameserver.model.L2CharPosition;
import l2.hellknight.gameserver.model.L2Object;
import l2.hellknight.gameserver.model.L2World;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2MerchantSummonInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.entity.Hero;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.communityserver.CommunityServerThread;
import l2.hellknight.gameserver.network.communityserver.writepackets.RequestShowCommunityBoard;
import l2.hellknight.gameserver.network.serverpackets.ActionFailed;
import l2.hellknight.gameserver.network.serverpackets.ConfirmDlg;
import l2.hellknight.gameserver.network.serverpackets.ExShowScreenMessage;
import l2.hellknight.gameserver.network.serverpackets.NpcHtmlMessage;
import l2.hellknight.gameserver.util.GMAudit;
import l2.hellknight.gameserver.util.Util;

/**
 * This class ...
 * @version $Revision: 1.12.4.5 $ $Date: 2005/04/11 10:06:11 $
 */
public final class RequestBypassToServer extends L2GameClientPacket
{
	private static final String _C__23_REQUESTBYPASSTOSERVER = "[C] 23 RequestBypassToServer";
	
	// S
	private String _command;
	
	@Override
	protected void readImpl()
	{
		_command = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		if (!getClient().getFloodProtectors().getServerBypass().tryPerformAction(_command))
			return;
		
		if (_command.isEmpty())
		{
			_log.info(activeChar.getName() + " send empty requestbypass");
			activeChar.logout();
			return;
		}
		
		try
		{
			if (_command.startsWith("admin_"))
			{
				String command = _command.split(" ")[0];
				
				IAdminCommandHandler ach = AdminCommandHandler.getInstance().getHandler(command);
				
				if (ach == null)
				{
					if (activeChar.isGM())
					{
						activeChar.sendMessage("The command " + command.substring(6) + " does not exist!");
					}
					_log.warning(activeChar + " requested not registered admin command '" + command + "'");
					return;
				}
				
				if (!AdminTable.getInstance().hasAccess(command, activeChar.getAccessLevel()))
				{
					activeChar.sendMessage("You don't have the access rights to use this command!");
					_log.warning("Character " + activeChar.getName() + " tried to use admin command " + command + ", without proper access level!");
					return;
				}
				
				if (AdminTable.getInstance().requireConfirm(command))
				{
					activeChar.setAdminConfirmCmd(_command);
					ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.S1);
					dlg.addString("Are you sure you want execute command " + _command.substring(6) + " ?");
					activeChar.sendPacket(dlg);
				}
				else
				{
					if (Config.GMAUDIT)
					{
						GMAudit.auditGMAction(activeChar.getName() + " [" + activeChar.getObjectId() + "]", _command, (activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target"));
					}
					
					ach.useAdminCommand(_command, activeChar);
				}
			}
			else if (_command.equals("come_here") && activeChar.isGM())
			{
				comeHere(activeChar);
			}
			else if (_command.startsWith("npc_"))
			{
				if (!activeChar.validateBypass(_command))
					return;
				
				int endOfId = _command.indexOf('_', 5);
				String id;
				if (endOfId > 0)
				{
					id = _command.substring(4, endOfId);
				}
				else
				{
					id = _command.substring(4);
				}
				if (Util.isDigit(id))
				{
					L2Object object = L2World.getInstance().findObject(Integer.parseInt(id));
					
					if (object != null && object.isNpc() && endOfId > 0 && activeChar.isInsideRadius(object, L2Npc.INTERACTION_DISTANCE, false, false))
					{
						((L2Npc) object).onBypassFeedback(activeChar, _command.substring(endOfId + 1));
					}
				}
				
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else if (_command.startsWith("summon_"))
			{
				if (!activeChar.validateBypass(_command))
					return;
				
				int endOfId = _command.indexOf('_', 8);
				String id;
				
				if (endOfId > 0)
				{
					id = _command.substring(7, endOfId);
				}
				else
				{
					id = _command.substring(7);
				}
				
				if (Util.isDigit(id))
				{
					L2Object object = L2World.getInstance().findObject(Integer.parseInt(id));
					
					if (object instanceof L2MerchantSummonInstance && endOfId > 0 && activeChar.isInsideRadius(object, L2Npc.INTERACTION_DISTANCE, false, false))
					{
						((L2MerchantSummonInstance) object).onBypassFeedback(activeChar, _command.substring(endOfId + 1));
					}
				}
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else if(_command.startsWith("votehopzone"))
			{
				long lastVoteHopzone = 0L;
				long voteDelay = 43200000L;
				int firstvoteshop;
				
				firstvoteshop = getHopZoneVotes();
				
				Connection con = null;
				PreparedStatement statement = null;
				try
				{
					con = L2DatabaseFactory.getInstance().getConnection();
					
					statement = con.prepareStatement("SELECT lastVoteHopzone FROM characters WHERE obj_Id=?");
					statement.setInt(1, activeChar.getObjectId());
					
					ResultSet rset = statement.executeQuery();
					
					
					while (rset.next())
					{
						lastVoteHopzone = rset.getLong("lastVoteHopzone");
					}
				}
				catch (Exception e)
				{}
				finally
				{
					L2DatabaseFactory.close(con);
				}
				
				if(lastVoteHopzone + voteDelay < System.currentTimeMillis())
				{	
					sendPacket(new ExShowScreenMessage("You have 45 seconds to vote for our server!", 6000));					
					activeChar.sendMessage("Go fast on the site and vote on the hopzone banner!");
					waitS(45);
					
					if (firstvoteshop < getHopZoneVotes())
					{
					       TextBuilder tb = new TextBuilder();
					       NpcHtmlMessage html = new NpcHtmlMessage(1);
					               
					       tb.append("<html><head><title>Vote Reward Panel</title></head><body>");
					       tb.append("<center>");
					       tb.append("<table width=\"250\" cellpadding=\"5\" bgcolor=\"000000\">");
					       tb.append("<tr>");
					       tb.append("<td width=\"45\" valign=\"top\" align=\"center\"><img src=\"L2ui_ch3.menubutton4\" width=\"38\" height=\"38\"></td>");
					       tb.append("<td valign=\"top\"><font color=\"FF6600\">Vote Panel</font>");  
					       tb.append("<br1><font color=\"00FF00\">"+activeChar.getName()+"</font>, get your reward here.</td>");
					       tb.append("</tr>");
					       tb.append("</table>");
					       tb.append("</center>");
					       tb.append("<center>");
					       tb.append("<td valign=\"top\"><font color=\"FF6600\">Choose your reward " + activeChar.getName()+".</font>");  
					       tb.append("<button value=\"Item:"+ItemTable.getInstance().getTemplate(ExternalConfig.VOTE_REWARD_ID1).getName()+"   Amount:"+ ExternalConfig.VOTE_REWARD_AMOUNT1+ "\" action=\"bypass -h votereward1\" width=204 height=20>");
					       tb.append("<button value=\"Item:"+ ItemTable.getInstance().getTemplate(ExternalConfig.VOTE_REWARD_ID2).getName()+"   Amount:"+ExternalConfig.VOTE_REWARD_AMOUNT2 + "\" action=\"bypass -h votereward2\" width=204 height=20>");
					       tb.append("<button value=\"Item:"+ ItemTable.getInstance().getTemplate(ExternalConfig.VOTE_REWARD_ID3).getName()+"   Amount:"+ ExternalConfig.VOTE_REWARD_AMOUNT3+ "\" action=\"bypass -h votereward3\" width=204 height=20>");
					       tb.append("</center>");
					       tb.append("<center>");
					       tb.append("<img src=\"l2ui_ch3.herotower_deco\" width=256 height=32 align=center>");
					       tb.append("<font color=\"FF6600\">By Elfocrash</font>");  
					       tb.append("</center>");
					       tb.append("</body></html>");
					               
					       html.setHtml(tb.toString());
					       activeChar.sendPacket(html);
						
						sendPacket(new ExShowScreenMessage("Choose your vote reward!Thanks for voting for us!", 6000));
						activeChar.sendMessage("Thank you for voting for us!");
						activeChar.updateLastVoteHopzone();

					}
					else
					{
						sendPacket(new ExShowScreenMessage("You did not vote.Please try again.", 6000));
						activeChar.sendMessage("You did not vote.Please try again.");
					}
				}
				else
					activeChar.sendMessage("12 hours have to pass till you are able to vote again.");
			}
			else if(_command.startsWith("votetopzone"))
			{
				long lastVoteTopzone = 0L;
				long voteDelay = 43200000L;
				int firstvotestop;
				
				firstvotestop = getTopZoneVotes();
				
				Connection con = null;
				PreparedStatement statement = null;
				try
				{
					con = L2DatabaseFactory.getInstance().getConnection();
					
					statement = con.prepareStatement("SELECT lastVoteTopzone FROM characters WHERE obj_Id=?");
					statement.setInt(1, activeChar.getObjectId());
					
					ResultSet rset = statement.executeQuery();
					
					
					while (rset.next())
					{
						lastVoteTopzone = rset.getLong("lastVoteTopzone");
					}
				}
				catch (Exception e)
				{}
				finally
				{
					L2DatabaseFactory.close(con);
				}
				
				if(lastVoteTopzone + voteDelay < System.currentTimeMillis())
				{	
					sendPacket(new ExShowScreenMessage("You have 45 seconds to vote for our server!", 6000));
					activeChar.sendMessage("Go fast on the site and vote on the topzone banner!");
					waitS(45);
					
					if (firstvotestop < getTopZoneVotes())
					{
					       TextBuilder tb = new TextBuilder();
					       NpcHtmlMessage html = new NpcHtmlMessage(1);
					               
					       tb.append("<html><head><title>Vote Reward Panel</title></head><body>");
					       tb.append("<center>");
					       tb.append("<table width=\"250\" cellpadding=\"5\" bgcolor=\"000000\">");
					       tb.append("<tr>");
					       tb.append("<td width=\"45\" valign=\"top\" align=\"center\"><img src=\"L2ui_ch3.menubutton4\" width=\"38\" height=\"38\"></td>");
					       tb.append("<td valign=\"top\"><font color=\"FF6600\">Vote Panel</font>");  
					       tb.append("<br1><font color=\"00FF00\">"+activeChar.getName()+"</font>, get your reward here.</td>");
					       tb.append("</tr>");
					       tb.append("</table>");
					       tb.append("</center>");
					       tb.append("<center>");
					       tb.append("<td valign=\"top\"><font color=\"FF6600\">Choose your reward " + activeChar.getName()+".</font>");  
					       tb.append("<button value=\"Item:"+ ItemTable.getInstance().getTemplate(ExternalConfig.VOTE_REWARD_ID1).getName()+"   Amount:"+ExternalConfig.VOTE_REWARD_AMOUNT1 + "\" action=\"bypass -h votereward1\" width=204 height=20>");
					       tb.append("<button value=\"Item:"+ ItemTable.getInstance().getTemplate(ExternalConfig.VOTE_REWARD_ID2).getName()+"   Amount:"+ ExternalConfig.VOTE_REWARD_AMOUNT2+ "\" action=\"bypass -h votereward2\" width=204 height=20>");
					       tb.append("<button value=\"Item:"+ItemTable.getInstance().getTemplate(ExternalConfig.VOTE_REWARD_ID3).getName() +"   Amount:"+ ExternalConfig.VOTE_REWARD_AMOUNT3+ "\" action=\"bypass -h votereward3\" width=204 height=20>");
					       tb.append("</center>");
					       tb.append("<center>");
					       tb.append("<img src=\"l2ui_ch3.herotower_deco\" width=256 height=32 align=center>");
					       tb.append("<font color=\"FF6600\">By Elfocrash</font>");  
					       tb.append("</center>");
					       tb.append("</body></html>");
					               
					       html.setHtml(tb.toString());
					       activeChar.sendPacket(html);
						sendPacket(new ExShowScreenMessage("You get rewarded for your vote.Thank you!", 6000));
						activeChar.sendMessage("Thank you for voting for us!");
						activeChar.updateLastVoteTopzone();

					}
					else
					{
						sendPacket(new ExShowScreenMessage("You did not vote.Please try again.", 6000));
						activeChar.sendMessage("You did not vote.Please try again.");
					}
				}
				else
					activeChar.sendMessage("12 hours have to pass till you are able to vote again.");
			}
			else if (_command.startsWith("votereward1"))
			{
				activeChar.addItem("reward", ExternalConfig.VOTE_REWARD_ID1, ExternalConfig.VOTE_REWARD_AMOUNT1, activeChar, true);
				activeChar.sendMessage("Wise choise!");
			}
			else if (_command.startsWith("votereward2"))
			{
				activeChar.addItem("reward", ExternalConfig.VOTE_REWARD_ID2, ExternalConfig.VOTE_REWARD_AMOUNT2, activeChar, true);
				activeChar.sendMessage("Wise choise!");
			}
			else if (_command.startsWith("votereward3"))
			{
				activeChar.addItem("reward", ExternalConfig.VOTE_REWARD_ID3, ExternalConfig.VOTE_REWARD_AMOUNT3, activeChar, true);
				activeChar.sendMessage("Wise choise!");
			}
			// Navigate through Manor windows
			else if (_command.startsWith("manor_menu_select"))
			{
				final IBypassHandler manor = BypassHandler.getInstance().getHandler("manor_menu_select");
				if (manor != null)
				{
					manor.useBypass(_command, activeChar, null);
				}
			}
			else if (_command.startsWith("_bbs"))
			{
				if (Config.ENABLE_COMMUNITY_BOARD)
				{
					if (!CommunityServerThread.getInstance().sendPacket(new RequestShowCommunityBoard(activeChar.getObjectId(), _command)))
						activeChar.sendPacket(SystemMessageId.CB_OFFLINE);
				}
				else
				{
					CommunityBoard.getInstance().handleCommands(getClient(), _command);
				}
			}
			else if (_command.startsWith("bbs"))
			{
				if (Config.ENABLE_COMMUNITY_BOARD)
				{
					if (!CommunityServerThread.getInstance().sendPacket(new RequestShowCommunityBoard(activeChar.getObjectId(), _command)))
					{
						activeChar.sendPacket(SystemMessageId.CB_OFFLINE);
					}
				}
				else
				{
					CommunityBoard.getInstance().handleCommands(getClient(), _command);
				}
			}
			else if (_command.startsWith("_mail"))
			{
				if (!CommunityServerThread.getInstance().sendPacket(new RequestShowCommunityBoard(activeChar.getObjectId(), "_bbsmail")))
				{
					activeChar.sendPacket(SystemMessageId.CB_OFFLINE);
				}
			}
			else if (_command.startsWith("_friend"))
			{
				if (!CommunityServerThread.getInstance().sendPacket(new RequestShowCommunityBoard(activeChar.getObjectId(), "_bbsfriend")))
				{
					activeChar.sendPacket(SystemMessageId.CB_OFFLINE);
				}
			}
			else if (_command.startsWith("Quest "))
			{
				if (!activeChar.validateBypass(_command))
					return;
				
				String p = _command.substring(6).trim();
				int idx = p.indexOf(' ');
				if (idx < 0)
				{
					activeChar.processQuestEvent(p, "");
				}
				else
				{
					activeChar.processQuestEvent(p.substring(0, idx), p.substring(idx).trim());
				}
			}
			else if (_command.startsWith("_match"))
			{
				String params = _command.substring(_command.indexOf("?") + 1);
				StringTokenizer st = new StringTokenizer(params, "&");
				int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
				int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
				int heroid = Hero.getInstance().getHeroByClass(heroclass);
				if (heroid > 0)
				{
					Hero.getInstance().showHeroFights(activeChar, heroclass, heroid, heropage);
				}
			}
			else if (_command.startsWith("_diary"))
			{
				String params = _command.substring(_command.indexOf("?") + 1);
				StringTokenizer st = new StringTokenizer(params, "&");
				int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
				int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
				int heroid = Hero.getInstance().getHeroByClass(heroclass);
				if (heroid > 0)
				{
					Hero.getInstance().showHeroDiary(activeChar, heroclass, heroid, heropage);
				}
			}
			else
			{
				final IBypassHandler handler = BypassHandler.getInstance().getHandler(_command);
				if (handler != null)
				{
					handler.useBypass(_command, activeChar, null);
				}
				else
				{
					_log.log(Level.WARNING, getClient() + " sent not handled RequestBypassToServer: [" + _command + "]");
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, getClient() + " sent bad RequestBypassToServer: \"" + _command + "\"", e);
			if (activeChar.isGM())
			{
				StringBuilder sb = new StringBuilder(200);
				sb.append("<html><body>");
				sb.append("Bypass error: " + e + "<br1>");
				sb.append("Bypass command: " + _command + "<br1>");
				sb.append("StackTrace:<br1>");
				for (StackTraceElement ste : e.getStackTrace())
					sb.append(ste.toString() + "<br1>");
				sb.append("</body></html>");
				// item html
				NpcHtmlMessage msg = new NpcHtmlMessage(0, 12807);
				msg.setHtml(sb.toString());
				msg.disableValidation();
				activeChar.sendPacket(msg);
			}
		}
	}
	
	/**
	 * @param activeChar
	 */
	private static void comeHere(L2PcInstance activeChar)
	{
		L2Object obj = activeChar.getTarget();
		if (obj == null)
			return;
		if (obj instanceof L2Npc)
		{
			L2Npc temp = (L2Npc) obj;
			temp.setTarget(activeChar);
			temp.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(activeChar.getX(), activeChar.getY(), activeChar.getZ(), 0));
		}
	}
	
	@Override
	public String getType()
	{
		return _C__23_REQUESTBYPASSTOSERVER;
	}

	private int getHopZoneVotes()
	{
		int votes = -1;
		URL url = null;
		URLConnection con = null;
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader in = null;
		try
		{
			url = new URL(ExternalConfig.VOTE_LINK_HOPZONE);
			con = url.openConnection();    
			con.addRequestProperty("User-Agent", "Mozilla/4.76");
			is = con.getInputStream();
			isr = new InputStreamReader(is);		    
			in = new BufferedReader(isr);
			String inputLine;
			while ((inputLine = in.readLine()) != null)
			{
				if (inputLine.contains("rank anonymous tooltip"))
				{
					votes = Integer.valueOf(inputLine.split(">")[2].replace("</span", ""));
					break;
				}
			}
		}
		catch (Exception e)
		{
			_log.info("[AutoVoteReward] Server HOPZONE is offline or something is wrong in link");
			//e.printStackTrace();
		}
		finally
		{
			if(in!=null)
				try
				{
					in.close();
				}
				catch(IOException e1)
				{
					e1.printStackTrace();
				}
			if(isr!=null)
				try
				{
					isr.close();
				}
				catch(IOException e1)
				{
					e1.printStackTrace();
				}
			if(is!=null)
				try
				{
					is.close();
				}
				catch(IOException e1)
				{
					e1.printStackTrace();
				}
			
		}
		return votes;
	}
	  
	private int getTopZoneVotes()
	{
		int votes = -1;
		URL url = null;
		URLConnection con = null;
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader in = null;
		try
		{
			url = new URL(ExternalConfig.VOTE_LINK_TOPZONE);
			con = url.openConnection();    
			con.addRequestProperty("User-Agent", "Mozilla/4.76");
			is = con.getInputStream();
			isr = new InputStreamReader(is);		    
			in = new BufferedReader(isr);
			String inputLine;
			while ((inputLine = in.readLine()) != null)
			{
				if (inputLine.contains("<tr><td><div align=\"center\"><b><font style=\"font-size: 14px; color: rgb(1, 139, 193);\""))
				{
					String votesLine = in.readLine() ;
					
					votes = Integer.valueOf(votesLine.split(">")[5].replace("</font", ""));
					break;
				}
			}
		}
		catch (Exception e)
		{
			_log.info("[AutoVoteReward] Server TOPZONE is offline or something is wrong in link");
			//e.printStackTrace();
		}
		finally
		{
			if(in!=null)
				try
				{
					in.close();
				}
				catch(IOException e1)
				{
					e1.printStackTrace();
				}
			if(isr!=null)
				try
				{
					isr.close();
				}
				catch(IOException e1)
				{
					e1.printStackTrace();
				}
			if(is!=null)
				try
				{
					is.close();
				}
				catch(IOException e1)
				{
					e1.printStackTrace();
				}
		}
		return votes;
	}
	  
	private static void waitS(int seconds)
	{
		try
		{
			Thread.sleep(1000*seconds);
		}
		catch (InterruptedException ie)
		{ }
	}
}
