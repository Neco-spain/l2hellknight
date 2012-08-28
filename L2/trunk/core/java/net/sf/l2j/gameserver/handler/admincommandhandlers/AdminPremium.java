package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class AdminPremium
  implements IAdminCommandHandler
{
  private static final String[] ADMIN_COMMANDS = { "admin_premium_menu", "admin_premium_add1", "admin_premium_add2", "admin_premium_add3" };
  private static final int REQUIRED_LEVEL = Config.GM_ANNOUNCE;
  private static final Logger _log = Logger.getLogger(AdminPremium.class.getName());

  public boolean useAdminCommand(String command, L2PcInstance activeChar)
  {
    if ((!Config.ALT_PRIVILEGES_ADMIN) && (
      (!checkLevel(activeChar.getAccessLevel())) || (!activeChar.isGM()))) {
      return false;
    }

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
      catch (StringIndexOutOfBoundsException e)
      {
        activeChar.sendMessage("Error");
      }
    }
    else if (command.startsWith("admin_premium_add2"))
    {
      try
      {
        String val = command.substring(19);
        addPremiumServices(2, val);
      }
      catch (StringIndexOutOfBoundsException e)
      {
        activeChar.sendMessage("Error.");
      }
    }
    else if (command.startsWith("admin_premium_add3"))
    {
      try
      {
        String val = command.substring(19);
        addPremiumServices(3, val);
      }
      catch (StringIndexOutOfBoundsException e)
      {
        activeChar.sendMessage("Error");
      }
    }
    return true;
  }

  public String[] getAdminCommandList()
  {
    return ADMIN_COMMANDS;
  }

  private boolean checkLevel(int level)
  {
    return level >= REQUIRED_LEVEL;
  }

  private void addPremiumServices(int Months, String AccName)
  {
    Connection con = null;
    try
    {
      Calendar finishtime = Calendar.getInstance();
      finishtime.setTimeInMillis(System.currentTimeMillis());
      finishtime.set(13, 0);
      finishtime.add(2, Months);

      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("UPDATE account_premium SET premium_service=?,enddate=? WHERE account_name=?");
      statement.setInt(1, 1);
      statement.setLong(2, finishtime.getTimeInMillis());
      statement.setString(3, AccName);
      statement.execute();
      statement.close();
    }
    catch (SQLException e)
    {
      _log.info("PremiumService: Could not increase data");
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