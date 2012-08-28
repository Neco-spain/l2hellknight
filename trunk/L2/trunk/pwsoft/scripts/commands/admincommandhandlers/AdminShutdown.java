package scripts.commands.admincommandhandlers;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.Shutdown;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.Broadcast;
import scripts.commands.IAdminCommandHandler;

public class AdminShutdown
  implements IAdminCommandHandler
{
  private static final String[] ADMIN_COMMANDS = { "admin_server_shutdown", "admin_server_restart", "admin_server_abort", "admin_server_gc" };
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
    else if (command.startsWith("admin_server_gc"))
    {
      System.out.println("Starting clearing memory... current: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024L / 1024L + "MB...");
      System.gc();
      System.out.println("...done; now: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024L / 1024L + "MB");
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
    NpcHtmlMessage adminReply = NpcHtmlMessage.id(5);
    int t = GameTimeController.getInstance().getGameTime();
    int h = t / 60;
    int m = t % 60;
    SimpleDateFormat format = new SimpleDateFormat("h:mm a");
    Calendar cal = Calendar.getInstance();
    cal.set(11, h);
    cal.set(12, m);
    adminReply.setFile("data/html/admin/shutdown.htm");
    adminReply.replace("%count%", String.valueOf(L2World.getInstance().getAllPlayersCount()));
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
    Announcements.getInstance().announceToAll("\u0412\u043D\u0438\u043C\u0430\u043D\u0438\u0435! \u0421\u0435\u0440\u0432\u0435\u0440 \u0431\u0443\u0434\u0435\u0442 \u0432\u044B\u043A\u043B\u044E\u0447\u0435\u043D!");
    Announcements.getInstance().announceToAll("\u0412\u044B\u0439\u0434\u0438\u0442\u0435 \u0438\u0437 \u0438\u0433\u0440\u044B, \u0447\u0442\u043E-\u0431\u044B \u043D\u0435 \u043F\u043E\u0442\u0435\u0440\u044F\u0442\u044C \u0434\u043E\u0441\u0442\u0438\u0433\u043D\u0443\u0442\u044B\u0445 \u0440\u0435\u0437\u0443\u043B\u044C\u0442\u0430\u0442\u043E\u0432.");
    Announcements.getInstance().announceToAll("\u041F\u0440\u0438\u043D\u043E\u0441\u0438\u043C \u0438\u0437\u0432\u0438\u043D\u0435\u043D\u0438\u044F \u0437\u0430 \u0432\u043E\u0437\u043C\u043E\u0436\u043D\u044B\u0435 \u043D\u0435\u0443\u0434\u043E\u0431\u0441\u0442\u0432\u0430.");
    Broadcast.toAllOnlinePlayers(SystemMessage.id(SystemMessageId.THE_SERVER_WILL_BE_COMING_DOWN_IN_S1_SECONDS).addNumber(seconds));
    Shutdown.getInstance().startShutdown(activeChar, seconds, restart);
  }

  private void serverAbort(L2PcInstance activeChar)
  {
    Shutdown.getInstance().abort(activeChar);
  }
}