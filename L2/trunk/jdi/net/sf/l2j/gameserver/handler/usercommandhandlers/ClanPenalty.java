package net.sf.l2j.gameserver.handler.usercommandhandlers;

import java.text.SimpleDateFormat;
import java.util.Date;
import javolution.text.TextBuilder;
import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class ClanPenalty
  implements IUserCommandHandler
{
  private static final int[] COMMAND_IDS = { 100 };
  private long time;

  public boolean useUserCommand(int id, L2PcInstance activeChar)
  {
    if (id != COMMAND_IDS[0]) return false;
    String date = "";

    if ((activeChar.getClan() != null) && (activeChar.getClan().getCharPenaltyExpiryTime() != 0L))
    {
      time = activeChar.getClan().getCharPenaltyExpiryTime();
      date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(time));
    }
    else if ((activeChar.getClan() == null) && (activeChar.getClanJoinExpiryTime() != 0L))
    {
      time = activeChar.getClanJoinExpiryTime();
      date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(time));
    }
    else if ((activeChar.getClan() == null) && (activeChar.getClanCreateExpiryTime() != 0L))
    {
      time = activeChar.getClanCreateExpiryTime();
      date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(time));
    }
    else
    {
      date = "No current penalties in effect.";
    }
    TextBuilder htmlContent = new TextBuilder("<html><body>");
    htmlContent.append("<center><table width=\"270\" border=\"0\" bgcolor=\"111111\">");
    htmlContent.append("<tr><td width=\"170\">Penalty</td>");
    htmlContent.append("<td width=\"100\" align=\"center\">Expiration Date</td></tr>");
    htmlContent.append("</table><table width=\"270\" border=\"0\">");
    htmlContent.append("<tr><td>" + date + "</td></tr>");
    htmlContent.append("</table></center>");
    htmlContent.append("</body></html>");

    NpcHtmlMessage penaltyHtml = new NpcHtmlMessage(0);
    penaltyHtml.setHtml(htmlContent.toString());
    activeChar.sendPacket(penaltyHtml);

    return true;
  }

  public int[] getUserCommandList()
  {
    return COMMAND_IDS;
  }
}