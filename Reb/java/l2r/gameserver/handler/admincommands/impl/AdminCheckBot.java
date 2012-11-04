package l2r.gameserver.handler.admincommands.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.DateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javolution.text.TextBuilder;
import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.Config;
import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.instancemanager.AutoHuntingManager;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.utils.AutoHuntingPunish;
import l2r.loginserver.database.L2DatabaseFactory;


public class AdminCheckBot implements IAdminCommandHandler
{
	private static final Logger _log = LoggerFactory.getLogger(AdminCheckBot.class);
	
	private static enum Commands
	{
		admin_checkbots,
		admin_readbot,
		admin_markbotreaded,
		admin_punish_bot
	}
	
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		if (!Config.ENABLE_AUTO_HUNTING_REPORT)
		{
			activeChar.sendMessage("Bot reporting is not enabled!");
			return false;
		}
		
		if(!activeChar.getPlayerAccess().CanBan)
			return false;
		
		Commands command = (Commands) comm;
		
		String[] ids = fullString.split(" ");
	
		switch(command)
		{
			case admin_checkbots:
				sendBotPage(activeChar);
				break;
			case admin_readbot:
				sendBotInfoPage(activeChar, Integer.parseInt(ids[1]));
				break;
			case admin_markbotreaded:
			{
				try
				{
					AutoHuntingManager.getInstance().markAsRead(Integer.parseInt(wordList[1]));
					sendBotPage(activeChar);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				break;
			}
			case admin_punish_bot:
			{
				activeChar.sendMessage("Usage: //punish_bot <charName>");
				
				if (wordList != null)
				{
					Player target = GameObjectsStorage.getPlayer(wordList[1]);
					if (target != null)
					{
						synchronized (target)
						{
							int punishLevel = 0;
							try
							{
								punishLevel = AutoHuntingManager.getInstance().getPlayerReportsCount(target);
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
								
							// By System Message guess:
							// Reported 1 time = 10 mins chat ban
							// Reported 2 times = 60 mins w/o join pt
							// Reported 3 times = 120 mins w/o join pt
							// Reported 4 times = 180 mins w/o join pt
							// Reported 5 times = 120 mins w/o move
							// Reported 6 times = 180 mins w/o move
							// Reported 7 times = 120 mins w/o any action
							
							// Must be handled by GM or automatically ?
							// Since never will be retail info, ill put manually
							switch (punishLevel)
							{
							case 1:
								target.setPunishDueBotting(AutoHuntingPunish.Punish.CHATBAN, 10);
								target.sendPacket(new SystemMessage2(SystemMsg.YOU_HAVE_BEEN_REPORTED_AS_AN_ILLEGAL_PROGRAM_USER_SO_YOUR_CHATTING_WILL_BE_BLOCKED_FOR_10_MINUTES));
								break;
							case 2:
								target.setPunishDueBotting(AutoHuntingPunish.Punish.PARTYBAN, 60);
								target.sendPacket(new SystemMessage2(SystemMsg.YOU_HAVE_BEEN_REPORTED_AS_AN_ILLEGAL_PROGRAM_USER_SO_YOUR_PARTY_PARTICIPATION_WILL_BE_BLOCKED_FOR_60_MINUTES));
								break;
							case 3:
								target.setPunishDueBotting(AutoHuntingPunish.Punish.PARTYBAN, 120);
								target.sendPacket(new SystemMessage2(SystemMsg.YOU_HAVE_BEEN_REPORTED_AS_AN_ILLEGAL_PROGRAM_USER_SO_YOUR_PARTY_PARTICIPATION_WILL_BE_BLOCKED_FOR_120_MINUTES));
								break;
							case 4:
								target.setPunishDueBotting(AutoHuntingPunish.Punish.PARTYBAN, 180);
								target.sendPacket(new SystemMessage2(SystemMsg.YOU_HAVE_BEEN_REPORTED_AS_AN_ILLEGAL_PROGRAM_USER_SO_YOUR_PARTY_PARTICIPATION_WILL_BE_BLOCKED_FOR_180_MINUTES));
								break;
							case 5:
								target.setPunishDueBotting(AutoHuntingPunish.Punish.MOVEBAN, 120);
								target.sendPacket(new SystemMessage2(SystemMsg.YOU_HAVE_BEEN_REPORTED_AS_AN_ILLEGAL_PROGRAM_USER_SO_MOVEMENT_IS_PROHIBITED_FOR_120_MINUTES));
								break;
							case 6:
								target.setPunishDueBotting(AutoHuntingPunish.Punish.ACTIONBAN, 120);
								target.sendPacket(new SystemMessage2(SystemMsg.YOU_HAVE_BEEN_REPORTED_AS_AN_ILLEGAL_PROGRAM_USER_SO_YOUR_ACTIONS_WILL_BE_RESTRICTED_FOR_120_MINUTES));
								break;
							case 7:
								target.setPunishDueBotting(AutoHuntingPunish.Punish.ACTIONBAN, 180);
								target.sendPacket(new SystemMessage2(SystemMsg.YOU_HAVE_BEEN_REPORTED_AS_AN_ILLEGAL_PROGRAM_USER_SO_YOUR_ACTIONS_WILL_BE_RESTRICTED_FOR_180_MINUTES));
								break;
							default:
								activeChar.sendMessage("Your target wasnt reported as a bot!");
							}
							// Inserts first time player punish in database, avoiding
							// problems to update punish state in future on log out
							if (punishLevel != 0)
							{
								introduceNewPunishedBotAndClear(target);
								activeChar.sendMessage(target.getName() + " has been punished");
							}
						}
					}
					else
						activeChar.sendMessage("Your target doesnt exist!");
				}
			}
		}
		return true;
	}
	
	private static void sendBotPage(Player activeChar)
	{
		TextBuilder tb = new TextBuilder();
		tb.append("<html><table width=260>");
		tb.append("<tr>");
		tb.append("<td width=40>");
		tb.append("<a action=\"bypass -h admin_admin\">Main</a>");
		tb.append("</td>");
		tb.append("<td width=180>");
		tb.append("<center>Bot Report's info</center>");
		tb.append("</td>");
		tb.append("<td width=40>");
		tb.append("<a action=\"bypass -h admin_admin\">Back</a>");
		tb.append("</td>");
		tb.append("</tr>");
		tb.append("</table>");
		tb.append("<title>Unread Bot List</title><body><center>");
		tb.append("Here's a list of the current <font color=LEVEL>unread</font><br1>bots!<br>");
		
		for (int i : AutoHuntingManager.getInstance().getUnread().keySet())
		{
			tb.append("<a action=\"bypass -h admin_readbot " + i + "\">Ticket #" + i + "</a><br1>");

		}
		tb.append("</center></body></html>");
		
		NpcHtmlMessage nhm = new NpcHtmlMessage(5);
		nhm.setHtml(tb.toString());
		activeChar.sendPacket(nhm);
	}
	
	private static void sendBotInfoPage(Player activeChar, int botId)
	{
		String[] report = AutoHuntingManager.getInstance().getUnread().get(botId);
		TextBuilder tb = new TextBuilder();
		
		tb.append("<html><title>Bot #" + botId + "</title><body><center><br>");
		tb.append("- Bot report ticket Id: <font color=FF0000>" + botId + "</font><br>");
		tb.append("- Player reported: <font color=FF0000>" + report[0] + "</font><br>");
		tb.append("- Reported by: <font color=FF0000>" + report[1] + "</font><br>");
		tb.append("- Date: <font color=FF0000>" + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(Long.parseLong(report[2])) + "</font><br>");
		tb.append("<a action=\"bypass -h admin_markbotreaded " + botId + "\">Mark Report as Read</a>");
		tb.append("<a action=\"bypass -h admin_punish_bot " + report[0] + "\">Punish " + report[0] + "</a>");
		tb.append("<a action=\"bypass -h admin_checkbots\">Go Back to bot list</a>");
		tb.append("</center></body></html>");
		
		NpcHtmlMessage nhm = new NpcHtmlMessage(5);
		nhm.setHtml(tb.toString());
		activeChar.sendPacket(nhm);
	}
	
	/**
	 * Will introduce the first time a new punished bot in database,
	 * to avoid problems on his punish time left update, as will remove
	 * his reports from database
	 * @param L2PcInstance
	 */
	private static void introduceNewPunishedBotAndClear(Player target)
	{
		Connection con = null;
		PreparedStatement statement = null;
		PreparedStatement delStatement = null;
		try
		{
			
			con = L2DatabaseFactory.getInstance().getConnection();
			// Introduce new Punished Bot in database
			statement = con.prepareStatement("INSERT INTO bot_reported_punish VALUES ( ?, ?, ? )");
			statement.setInt(1, target.getObjectId());
			statement.setString(2, target.getPlayerPunish().getBotPunishType().name());
			statement.setLong(3, target.getPlayerPunish().getPunishTimeLeft());
			statement.execute();
			
			// Delete all his reports from database
			delStatement = con.prepareStatement("DELETE FROM bot_report WHERE reported_objectId = ?");
			delStatement.setInt(1, target.getObjectId());
			delStatement.execute();
			DbUtils.closeQuietly(delStatement);
		}
		catch (Exception e)
		{
			_log.info("AdminCheckBot.introduceNewPunishedBotAndClear(target): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}