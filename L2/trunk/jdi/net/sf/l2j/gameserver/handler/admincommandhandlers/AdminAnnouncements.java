package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class AdminAnnouncements
  implements IAdminCommandHandler
{
  private static final String[] ADMIN_COMMANDS = { "admin_list_announcements", "admin_reload_announcements", "admin_announce_announcements", "admin_add_announcement", "admin_del_announcement", "admin_announce", "admin_announce_menu" };

  private static final int REQUIRED_LEVEL = Config.GM_ANNOUNCE;

  public boolean useAdminCommand(String command, L2PcInstance activeChar) {
    if ((!Config.ALT_PRIVILEGES_ADMIN) && (
      (!checkLevel(activeChar.getAccessLevel())) || (!activeChar.isGM()))) return false;

    if (command.equals("admin_list_announcements"))
    {
      Announcements.getInstance().listAnnouncements(activeChar);
    }
    else if (command.equals("admin_reload_announcements"))
    {
      Announcements.getInstance().loadAnnouncements();
      Announcements.getInstance().listAnnouncements(activeChar);
    }
    else if (command.startsWith("admin_announce_menu"))
    {
      Announcements sys = new Announcements();
      sys.handleAnnounce(command, 20);
      Announcements.getInstance().listAnnouncements(activeChar);
    }
    else if (command.equals("admin_announce_announcements"))
    {
      for (L2PcInstance player : L2World.getInstance().getAllPlayers())
      {
        Announcements.getInstance().showAnnouncements(player);
      }
      Announcements.getInstance().listAnnouncements(activeChar);
    }
    else if (command.startsWith("admin_add_announcement"))
    {
      if (!command.equals("admin_add_announcement"))
        try
        {
          String val = command.substring(23);
          Announcements.getInstance().addAnnouncement(val);
          Announcements.getInstance().listAnnouncements(activeChar);
        } catch (StringIndexOutOfBoundsException e) {
        }
    }
    else if (command.startsWith("admin_del_announcement"))
    {
      try
      {
        int val = new Integer(command.substring(23)).intValue();
        Announcements.getInstance().delAnnouncement(val);
        Announcements.getInstance().listAnnouncements(activeChar);
      }
      catch (StringIndexOutOfBoundsException e)
      {
      }

    }
    else if (command.startsWith("admin_announce"))
    {
      Announcements sys = new Announcements();
      sys.handleAnnounce(command, 15);
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