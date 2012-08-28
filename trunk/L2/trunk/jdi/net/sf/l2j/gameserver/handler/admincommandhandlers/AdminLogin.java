package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.LoginServerThread;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class AdminLogin
  implements IAdminCommandHandler
{
  private static final String[] ADMIN_COMMANDS = { "admin_server_gm_only", "admin_server_all", "admin_server_max_player", "admin_server_list_clock", "admin_server_login" };

  private static final int REQUIRED_LEVEL = Config.GM_ACCESSLEVEL;

  public boolean useAdminCommand(String command, L2PcInstance activeChar)
  {
    if (!Config.ALT_PRIVILEGES_ADMIN)
    {
      if (activeChar.getAccessLevel() < REQUIRED_LEVEL) {
        return false;
      }
    }
    if (command.equals("admin_server_gm_only"))
    {
      gmOnly();
      activeChar.sendMessage("Server is now GM only");
      showMainPage(activeChar);
    }
    else if (command.equals("admin_server_all"))
    {
      allowToAll();
      activeChar.sendMessage("Server is not GM only anymore");
      showMainPage(activeChar);
    }
    else if (command.startsWith("admin_server_max_player"))
    {
      StringTokenizer st = new StringTokenizer(command);
      if (st.countTokens() > 1)
      {
        st.nextToken();
        String number = st.nextToken();
        try
        {
          LoginServerThread.getInstance().setMaxPlayer(new Integer(number).intValue());
          activeChar.sendMessage("maxPlayer set to " + new Integer(number).intValue());
          showMainPage(activeChar);
        }
        catch (NumberFormatException e)
        {
          activeChar.sendMessage("Max players must be a number.");
        }
      }
      else
      {
        activeChar.sendMessage("Format is server_max_player <max>");
      }
    }
    else if (command.startsWith("admin_server_list_clock"))
    {
      StringTokenizer st = new StringTokenizer(command);
      if (st.countTokens() > 1)
      {
        st.nextToken();
        String mode = st.nextToken();
        if (mode.equals("on"))
        {
          LoginServerThread.getInstance().sendServerStatus(2, 1);
          activeChar.sendMessage("A clock will now be displayed next to the server name");
          Config.SERVER_LIST_CLOCK = true;
          showMainPage(activeChar);
        }
        else if (mode.equals("off"))
        {
          LoginServerThread.getInstance().sendServerStatus(2, 0);
          Config.SERVER_LIST_CLOCK = false;
          activeChar.sendMessage("The clock will not be displayed");
          showMainPage(activeChar);
        }
        else
        {
          activeChar.sendMessage("Format is server_list_clock <on/off>");
        }
      }
      else
      {
        activeChar.sendMessage("Format is server_list_clock <on/off>");
      }
    }
    else if (command.equals("admin_server_login"))
    {
      showMainPage(activeChar);
    }
    return true;
  }

  private void showMainPage(L2PcInstance activeChar)
  {
    NpcHtmlMessage html = new NpcHtmlMessage(1);
    html.setFile("data/html/admin/login.htm");
    html.replace("%server_name%", LoginServerThread.getInstance().getServerName());
    html.replace("%status%", LoginServerThread.getInstance().getStatusString());
    html.replace("%clock%", String.valueOf(Config.SERVER_LIST_CLOCK));
    html.replace("%brackets%", String.valueOf(Config.SERVER_LIST_BRACKET));
    html.replace("%max_players%", String.valueOf(LoginServerThread.getInstance().getMaxPlayer()));
    activeChar.sendPacket(html);
  }

  private void allowToAll()
  {
    LoginServerThread.getInstance().setServerStatus(0);
    Config.SERVER_GMONLY = false;
  }

  private void gmOnly()
  {
    LoginServerThread.getInstance().setServerStatus(5);
    Config.SERVER_GMONLY = true;
  }

  public String[] getAdminCommandList()
  {
    return ADMIN_COMMANDS;
  }
}