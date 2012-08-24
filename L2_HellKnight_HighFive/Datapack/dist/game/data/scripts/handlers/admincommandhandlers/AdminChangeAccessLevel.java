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
package handlers.admincommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import l2.hellknight.Config;
import l2.hellknight.L2DatabaseFactory;
import l2.hellknight.gameserver.datatables.AdminTable;
import l2.hellknight.gameserver.handler.IAdminCommandHandler;
import l2.hellknight.gameserver.model.L2World;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.network.SystemMessageId;


/**
 * This class handles following admin commands:
 * - changelvl = change a character's access level
 *  Can be used for character ban (as opposed to regular //ban that affects accounts)
 *  or to grant mod/GM privileges ingame
 * @version $Revision: 1.1.2.2.2.3 $ $Date: 2005/04/11 10:06:00 $
 * con.close() change by Zoey76 24/02/2011
 */
public class AdminChangeAccessLevel implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_changelvl"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		handleChangeLevel(command, activeChar);
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	/**
	 * If no character name is specified, tries to change GM's target access
	 * level. Else if a character name is provided, will try to reach it either
	 * from L2World or from a database connection.
	 * 
	 * @param command
	 * @param activeChar
	 */
	private void handleChangeLevel(String command, L2PcInstance activeChar)
	{
		String[] parts = command.split(" ");
		if (parts.length == 2)
		{
			try
			{
				int lvl = Integer.parseInt(parts[1]);
				if (activeChar.getTarget() instanceof L2PcInstance)
					onLineChange(activeChar, (L2PcInstance) activeChar.getTarget(), lvl);
				else
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //changelvl <target_new_level> | <player_name> <new_level>");
			}
		}
		else if (parts.length == 3)
		{
			String name = parts[1];
			int lvl = Integer.parseInt(parts[2]);
			L2PcInstance player = L2World.getInstance().getPlayer(name);
			if (player != null)
				onLineChange(activeChar, player, lvl);
			else
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					PreparedStatement statement = con.prepareStatement("UPDATE characters SET accesslevel=? WHERE char_name=?");
					statement.setInt(1, lvl);
					statement.setString(2, name);
					statement.execute();
					int count = statement.getUpdateCount();
					statement.close();
					if (count == 0)
						activeChar.sendMessage("Character not found or access level unaltered.");
					else
						activeChar.sendMessage("Character's access level is now set to " + lvl);
				}
				catch (SQLException se)
				{
					activeChar.sendMessage("SQLException while changing character's access level");
					if (Config.DEBUG)
						se.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * @param activeChar
	 * @param player
	 * @param lvl
	 */
	private void onLineChange(L2PcInstance activeChar, L2PcInstance player, int lvl)
	{
		if (lvl >= 0)
		{
			if (AdminTable.getInstance().hasAccessLevel(lvl))
			{
				player.setAccessLevel(lvl);
				player.sendMessage("Your access level has been changed to " + lvl);
				activeChar.sendMessage("Character's access level is now set to " + lvl + ". Effects won't be noticeable until next session.");
			}
			else
			{
				activeChar.sendMessage("You are trying to set unexisting access level: " + lvl + " please try again with a valid one!");
			}
		}
		else
		{
			player.setAccessLevel(lvl);
			player.sendMessage("Your character has been banned. Bye.");
			player.logout();
		}
	}
}
