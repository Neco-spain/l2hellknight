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
import java.sql.SQLException;
import java.sql.PreparedStatement;

import java.util.Calendar;
import java.util.logging.Logger;

import l2.hellknight.L2DatabaseFactory;
import l2.hellknight.gameserver.handler.IAdminCommandHandler;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;

public class AdminPremium implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = { "admin_premium_menu", "admin_premium_add1", "admin_premium_add2", "admin_premium_add3" };
	
	private static final String UPDATE_PREMIUMSERVICE = "UPDATE account_premium SET premium_service=?,enddate=? WHERE account_name=?";
	private static final Logger _log = Logger.getLogger(AdminPremium.class.getName());

	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.equals("admin_premium_menu"))
		{
			AdminHelpPage.showHelpPage(activeChar, "premium_menu.htm");
		}
		else if (command.startsWith("admin_premium_add1"))
		{
			try
            {
                String val = command.substring(19);
                addPremiumServices(1, val);
            }
            catch(StringIndexOutOfBoundsException e)
            {
                activeChar.sendMessage("Err");
            }
        }
		else if(command.startsWith("admin_premium_add2"))
        {
            try
            {
                String val = command.substring(19);
                addPremiumServices(2, val);
            }
            catch(StringIndexOutOfBoundsException e)
            {
                activeChar.sendMessage("Err");
            }
        }
		else if(command.startsWith("admin_premium_add3"))
        {
            try
            {
                String val = command.substring(19);
                addPremiumServices(3, val);
            }
            catch(StringIndexOutOfBoundsException e)
            {
                activeChar.sendMessage("Err");
            }
        }
        return true;
    }
	
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void addPremiumServices(int Hours,String AccName)
	{
		Connection con = null;
		try
		{
			Calendar finishtime = Calendar.getInstance();
			finishtime.setTimeInMillis(System.currentTimeMillis());
			finishtime.set(Calendar.SECOND, 0);
			finishtime.add(Calendar.HOUR, Hours);
			
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_PREMIUMSERVICE);
			statement.setInt(1, 1);
			statement.setLong(2, finishtime.getTimeInMillis());
			statement.setString(3, AccName);
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.info("PremiumService:  Could not increase data");
		}
		finally
		{
			try
			{
				if (con != null)
					con.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}		
	}
}
