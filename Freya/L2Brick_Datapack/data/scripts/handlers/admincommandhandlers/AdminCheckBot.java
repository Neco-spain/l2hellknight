package handlers.admincommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;

import javolution.text.TextBuilder;

import l2.brick.Config;
import l2.brick.L2DatabaseFactory;
import l2.brick.gameserver.handler.IAdminCommandHandler;
import l2.brick.gameserver.instancemanager.BotManager;
import l2.brick.gameserver.model.L2World;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.network.SystemMessageId;
import l2.brick.gameserver.network.serverpackets.NpcHtmlMessage;
import l2.brick.gameserver.network.serverpackets.SystemMessage;
import l2.brick.gameserver.util.BotPunish;

public class AdminCheckBot implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = {
			"admin_checkBots",
			"admin_readBot",
			"admin_markBotReaded",
			"admin_punish_bot"
	};
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (!Config.ENABLE_BOTREPORT)
		{
			activeChar.sendMessage("Bot reporting is not enabled!");
			return false;
		}
		
		String[] sub = command.split(" ");
		if (command.startsWith("admin_checkBots"))
		{
			sendBotPage(activeChar);
		}
		else if (command.startsWith("admin_readBot"))
		{
			sendBotInfoPage(activeChar, Integer.valueOf(sub[1]));
		}
		else if (command.startsWith("admin_markBotReaded"))
		{
			try
			{
				BotManager.getInstance().markAsRead(Integer.valueOf(sub[1]));
				sendBotPage(activeChar);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else if (command.startsWith("admin_punish_bot"))
		{
			activeChar.sendMessage("Usage: //punish_bot <charName>");
			
			if (sub != null)
			{
				L2PcInstance target = L2World.getInstance().getPlayer(sub[1]);
				if (target != null)
				{
					synchronized (target)
					{
						int punishLevel = 0;
						try
						{
							punishLevel = BotManager.getInstance().getPlayerReportsCount(target);
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
							target.setPunishDueBotting(BotPunish.Punish.CHATBAN, 10);
							target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.REPORTED_10_MINS_WITHOUT_CHAT));
							break;
						case 2:
							target.setPunishDueBotting(BotPunish.Punish.PARTYBAN, 60);
							target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.REPORTED_60_MINS_WITHOUT_JOIN_PARTY));
							break;
						case 3:
							target.setPunishDueBotting(BotPunish.Punish.PARTYBAN, 120);
							target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.REPORTED_120_MINS_WITHOUT_JOIN_PARTY));
							break;
						case 4:
							target.setPunishDueBotting(BotPunish.Punish.PARTYBAN, 180);
							target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.REPORTED_180_MINS_WITHOUT_JOIN_PARTY));
							break;
						case 5:
							target.setPunishDueBotting(BotPunish.Punish.MOVEBAN, 120);
							target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.REPORTED_120_MINS_WITHOUT_MOVE));
							break;
						case 6:
							target.setPunishDueBotting(BotPunish.Punish.ACTIONBAN, 120);
							target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.REPORTED_120_MINS_WITHOUT_ACTIONS));
							break;
						case 7:
							target.setPunishDueBotting(BotPunish.Punish.ACTIONBAN, 180);
							target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.REPORTED_180_MINS_WITHOUT_ACTIONS));
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
		return true;
	}
	
	private static void sendBotPage(L2PcInstance activeChar)
	{
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>Unread Bot List</title><body><center>");
		tb.append("Here's a list of the current <font color=LEVEL>unread</font><br1>bots!<br>");
		
		for (int i : BotManager.getInstance().getUnread().keySet())
		{
			tb.append("<a action=\"bypass -h admin_readBot " + i + "\">Ticket #" + i + "</a><br1>");
		}
		tb.append("</center></body></html>");
		
		NpcHtmlMessage nhm = new NpcHtmlMessage(5);
		nhm.setHtml(tb.toString());
		activeChar.sendPacket(nhm);
	}
	
	private static void sendBotInfoPage(L2PcInstance activeChar, int botId)
	{
		String[] report = BotManager.getInstance().getUnread().get(botId);
		TextBuilder tb = new TextBuilder();
		
		tb.append("<html><title>Bot #" + botId + "</title><body><center><br>");
		tb.append("- Bot report ticket Id: <font color=FF0000>" + botId + "</font><br>");
		tb.append("- Player reported: <font color=FF0000>" + report[0] + "</font><br>");
		tb.append("- Reported by: <font color=FF0000>" + report[1] + "</font><br>");
		tb.append("- Date: <font color=FF0000>" + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(Long.parseLong(report[2])) + "</font><br>");
		tb.append("<a action=\"bypass -h admin_markBotReaded " + botId + "\">Mark Report as Read</a>");
		tb.append("<a action=\"bypass -h admin_punish_bot " + report[0] + "\">Punish " + report[0] + "</a>");
		tb.append("<a action=\"bypass -h admin_checkBots\">Go Back to bot list</a>");
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
	private static void introduceNewPunishedBotAndClear(L2PcInstance target)
	{
		Connection con = null;
		try
		{
			
			con = L2DatabaseFactory.getInstance().getConnection();
			// Introduce new Punished Bot in database
			PreparedStatement statement = con.prepareStatement("INSERT INTO bot_reported_punish VALUES ( ?, ?, ? )");
			statement.setInt(1, target.getObjectId());
			statement.setString(2, target.getPlayerPunish().getBotPunishType().name());
			statement.setLong(3, target.getPlayerPunish().getPunishTimeLeft());
			statement.execute();
			statement.close();
			
			// Delete all his reports from database
			PreparedStatement delStatement = con.prepareStatement("DELETE FROM bot_report WHERE reported_objectId = ?");
			delStatement.setInt(1, target.getObjectId());
			delStatement.execute();
			delStatement.close();
		}
		catch (SQLException sqle)
		{
			sqle.printStackTrace();
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (SQLException e)
			{
			}
		}
	}
}