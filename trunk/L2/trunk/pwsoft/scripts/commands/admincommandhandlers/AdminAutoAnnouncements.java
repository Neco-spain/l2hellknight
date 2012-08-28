package scripts.commands.admincommandhandlers;

import java.util.StringTokenizer;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import scripts.commands.IAdminCommandHandler;
import scripts.handler.AutoAnnouncementHandler;

public class AdminAutoAnnouncements
  implements IAdminCommandHandler
{
  private static String[] ADMIN_COMMANDS = { "admin_list_autoannouncements", "admin_add_autoannouncement", "admin_del_autoannouncement", "admin_autoannounce" };

  private static final int REQUIRED_LEVEL = Config.GM_ANNOUNCE;

  public boolean useAdminCommand(String command, L2PcInstance admin) {
    if ((!Config.ALT_PRIVILEGES_ADMIN) && (
      (!checkLevel(admin.getAccessLevel())) || (!admin.isGM()))) return false;

    if (command.equals("admin_list_autoannouncements"))
    {
      AutoAnnouncementHandler.getInstance().listAutoAnnouncements(admin);
    }
    else if (command.startsWith("admin_add_autoannouncement"))
    {
      if (!command.equals("admin_add_autoannouncement"))
        try
        {
          StringTokenizer st = new StringTokenizer(command.substring(27));
          int delay = Integer.parseInt(st.nextToken().trim());
          String autoAnnounce = st.nextToken();

          if (delay > 30)
          {
            while (st.hasMoreTokens()) {
              autoAnnounce = autoAnnounce + " " + st.nextToken();
            }

            AutoAnnouncementHandler.getInstance().registerAnnouncment(autoAnnounce, delay);
            AutoAnnouncementHandler.getInstance().listAutoAnnouncements(admin);
          }
        }
        catch (StringIndexOutOfBoundsException e) {
        }
    }
    else if (command.startsWith("admin_del_autoannouncement"))
    {
      try
      {
        int val = new Integer(command.substring(27)).intValue();
        AutoAnnouncementHandler.getInstance().removeAnnouncement(val);
        AutoAnnouncementHandler.getInstance().listAutoAnnouncements(admin);
      }
      catch (StringIndexOutOfBoundsException e)
      {
      }

    }
    else if (command.startsWith("admin_autoannounce"))
    {
      AutoAnnouncementHandler.getInstance().listAutoAnnouncements(admin);
    }

    return true;
  }

  public String[] getAdminCommandList() {
    return ADMIN_COMMANDS;
  }

  private boolean checkLevel(int level) {
    return level >= REQUIRED_LEVEL;
  }
}