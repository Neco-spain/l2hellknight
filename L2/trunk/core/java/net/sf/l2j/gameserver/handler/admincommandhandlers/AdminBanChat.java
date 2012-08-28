package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class AdminBanChat implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = { "admin_banchat", "admin_unbanchat" };
	private static final int REQUIRED_LEVEL = Config.GM_BAN_CHAT;

	@SuppressWarnings("unused")
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (!Config.ALT_PRIVILEGES_ADMIN)
		{
			if (!(checkLevel(activeChar.getAccessLevel())))
			{
				System.out.println("Not required level.");
				return false;
			}
		}

		StringTokenizer st = new StringTokenizer(command);
		st.nextToken();
		String account_name = "";
		String player = "";
		L2PcInstance plyr = null;
		if (command.startsWith("admin_banchat"))
		{
			try
			{
				player = st.nextToken();
				int delay = 0;
				try
				{
					delay = Integer.parseInt(st.nextToken());
				}
				catch (NumberFormatException nfe)
				{
					activeChar.sendMessage("Использовать: //banchat <имя персонажа> [время в минутах]");
				}
				catch (NoSuchElementException nsee) { }
				L2PcInstance playerObj = L2World.getInstance().getPlayer(player);
				if (playerObj != null)
				{
					playerObj.setChatBanned(true, delay);
					activeChar.sendMessage("Character " + player + " banchat for " + (delay > 0 ? delay + " minutes." : "ever!"));
				}
				else
					chatBanOfflinePlayer(activeChar, player, delay);
			}
			catch (NoSuchElementException nsee)
			{
				activeChar.sendMessage("Использовать: //banchat <имя персонажа> [время в минутах]");
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("admin_unbanchat"))
		{
			try
			{
				player = st.nextToken();
				L2PcInstance playerObj = L2World.getInstance().getPlayer(player);

				if (playerObj != null)
				{
					playerObj.setChatBanned(false, 0);
					activeChar.sendMessage("Character " + player + " unbanchat");
				}
				else
					chatUnBanOfflinePlayer(activeChar, player);
			}
			catch (NoSuchElementException nsee)
			{
				activeChar.sendMessage("Specify a character name.");
			}
			catch (Exception e)
			{
			}
		}
		GMAudit.auditGMAction(activeChar.getName(), command, player, "");
		return true;
	}

	private void chatBanOfflinePlayer(L2PcInstance activeChar, String name, int delay)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement = con.prepareStatement("UPDATE characters SET banchat_time=? WHERE char_name=?");
			statement.setLong(1, delay * 60000L);
			statement.setString(2, name);
			statement.execute();
			int count = statement.getUpdateCount();
			statement.close();

			if (count == 0)
				activeChar.sendMessage("Character not found!");
			else
				activeChar.sendMessage("Character " + name + " banchat for " + (delay > 0 ? delay + " minutes." : "ever!"));
		}
		catch (SQLException se)
		{
			activeChar.sendMessage("SQLException while banchat player");
		}
		finally
		{
			try { con.close(); }
			catch (Exception e)
			{
			}
		}
	}

	private void chatUnBanOfflinePlayer(L2PcInstance activeChar, String name)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET banchat_time=? WHERE char_name=?");
			statement.setLong(1, 0);
			statement.setString(2, name);
			statement.execute();
			int count = statement.getUpdateCount();
			statement.close();
			if (count == 0)
				activeChar.sendMessage("Character not found!");
			else
				activeChar.sendMessage("Character " + name + " unbanchat");
		}
		catch (SQLException se)
		{
			activeChar.sendMessage("SQLException while jailing player");
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
			}
		}
	}

	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private boolean checkLevel(int level)
	{
		return (level >= REQUIRED_LEVEL);
	}
}