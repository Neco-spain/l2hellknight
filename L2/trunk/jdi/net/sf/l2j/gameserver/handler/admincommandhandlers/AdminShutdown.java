package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.Shutdown;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class AdminShutdown
  implements IAdminCommandHandler
{
  private static final String[] ADMIN_COMMANDS = { "admin_server_shutdown", "admin_server_restart", "admin_server_abort" };
  private static final int REQUIRED_LEVEL = Config.GM_RESTART;

  public boolean useAdminCommand(String command, L2PcInstance activeChar) {
    if ((!Config.ALT_PRIVILEGES_ADMIN) && (
      (!checkLevel(activeChar.getAccessLevel())) || (!activeChar.isGM()))) return false;

    if (command.startsWith("admin_server_shutdown"))
    {
      try
      {
        int val = Integer.parseInt(command.substring(22));
        serverShutdown(activeChar, val, false);
      }
      catch (StringIndexOutOfBoundsException e)
      {
        sendHtmlForm(activeChar);
      }
    }
    else if (command.startsWith("admin_server_restart"))
    {
      try
      {
        int val = Integer.parseInt(command.substring(21));
        serverShutdown(activeChar, val, true);
      }
      catch (StringIndexOutOfBoundsException e)
      {
        sendHtmlForm(activeChar);
      }
    }
    else if (command.startsWith("admin_server_abort"))
    {
      serverAbort(activeChar);
    }

    return true;
  }

  public String[] getAdminCommandList() {
    return ADMIN_COMMANDS;
  }

  private boolean checkLevel(int level) {
    return level >= REQUIRED_LEVEL;
  }

  private void sendHtmlForm(L2PcInstance activeChar) {
    NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
    int t = GameTimeController.getInstance().getGameTime();
    int h = t / 60;
    int m = t % 60;
    SimpleDateFormat format = new SimpleDateFormat("h:mm a");
    Calendar cal = Calendar.getInstance();
    cal.set(11, h);
    cal.set(12, m);
    adminReply.setFile("data/html/admin/shutdown.htm");
    L2World.getInstance(); adminReply.replace("%count%", String.valueOf(L2World.getAllPlayersCount()));
    adminReply.replace("%used%", String.valueOf(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
    adminReply.replace("%xp%", String.valueOf(Config.RATE_XP));
    adminReply.replace("%sp%", String.valueOf(Config.RATE_SP));
    adminReply.replace("%adena%", String.valueOf(Config.RATE_DROP_ADENA));
    adminReply.replace("%drop%", String.valueOf(Config.RATE_DROP_ITEMS));
    adminReply.replace("%time%", String.valueOf(format.format(cal.getTime())));
    activeChar.sendPacket(adminReply);
  }

  private void serverShutdown(L2PcInstance activeChar, int seconds, boolean restart)
  {
    Shutdown.getInstance().startShutdown(activeChar, seconds, restart);
  }

  private void serverAbort(L2PcInstance activeChar)
  {
    Shutdown.getInstance().abort(activeChar);
  }
}