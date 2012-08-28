package l2m.gameserver.handler.admincommands.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import l2p.commons.lang.StatsUtils;
import l2m.gameserver.Config;
import l2m.gameserver.GameTimeController;
import l2m.gameserver.Shutdown;
import l2m.gameserver.handler.admincommands.IAdminCommandHandler;
import l2m.gameserver.model.GameObjectsStorage;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.base.PlayerAccess;
import l2m.gameserver.network.serverpackets.NpcHtmlMessage;
import org.apache.commons.lang3.math.NumberUtils;

public class AdminShutdown
  implements IAdminCommandHandler
{
  public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    if (!activeChar.getPlayerAccess().CanRestart) {
      return false;
    }
    try
    {
      switch (1.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminShutdown$Commands[command.ordinal()])
      {
      case 1:
        Shutdown.getInstance().schedule(NumberUtils.toInt(wordList[1], -1), 0);
        break;
      case 2:
        Shutdown.getInstance().schedule(NumberUtils.toInt(wordList[1], -1), 2);
        break;
      case 3:
        Shutdown.getInstance().cancel();
      }

    }
    catch (Exception e)
    {
      sendHtmlForm(activeChar);
    }

    return true;
  }

  public Enum[] getAdminCommandEnum()
  {
    return Commands.values();
  }

  private void sendHtmlForm(Player activeChar)
  {
    NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

    int t = GameTimeController.getInstance().getGameTime();
    int h = t / 60;
    int m = t % 60;
    SimpleDateFormat format = new SimpleDateFormat("h:mm a");
    Calendar cal = Calendar.getInstance();
    cal.set(11, h);
    cal.set(12, m);

    StringBuilder replyMSG = new StringBuilder("<html><body>");
    replyMSG.append("<table width=260><tr>");
    replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
    replyMSG.append("<td width=180><center>Server Management Menu</center></td>");
    replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
    replyMSG.append("</tr></table>");
    replyMSG.append("<br><br>");
    replyMSG.append("<table>");
    replyMSG.append("<tr><td>Players Online: " + GameObjectsStorage.getAllPlayersCount() + "</td></tr>");
    replyMSG.append("<tr><td>Used Memory: " + StatsUtils.getMemUsedMb() + "</td></tr>");
    replyMSG.append("<tr><td>Server Rates: " + Config.RATE_XP + "x, " + Config.RATE_SP + "x, " + Config.RATE_DROP_ADENA + "x, " + Config.RATE_DROP_ITEMS + "x</td></tr>");
    replyMSG.append("<tr><td>Game Time: " + format.format(cal.getTime()) + "</td></tr>");
    replyMSG.append("</table><br>");
    replyMSG.append("<table width=270>");
    replyMSG.append("<tr><td>Enter in seconds the time till the server shutdowns bellow:</td></tr>");
    replyMSG.append("<br>");
    replyMSG.append("<tr><td><center>Seconds till: <edit var=\"shutdown_time\" width=60></center></td></tr>");
    replyMSG.append("</table><br>");
    replyMSG.append("<center><table><tr><td>");
    replyMSG.append("<button value=\"Shutdown\" action=\"bypass -h admin_server_shutdown $shutdown_time\" width=60 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td>");
    replyMSG.append("<button value=\"Restart\" action=\"bypass -h admin_server_restart $shutdown_time\" width=60 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td>");
    replyMSG.append("<button value=\"Abort\" action=\"bypass -h admin_server_abort\" width=60 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
    replyMSG.append("</td></tr></table></center>");
    replyMSG.append("</body></html>");

    adminReply.setHtml(replyMSG.toString());
    activeChar.sendPacket(adminReply);
  }

  private static enum Commands
  {
    admin_server_shutdown, 
    admin_server_restart, 
    admin_server_abort;
  }
}