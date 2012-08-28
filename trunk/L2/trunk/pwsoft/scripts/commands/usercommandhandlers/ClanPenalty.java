package scripts.commands.usercommandhandlers;

import javolution.text.TextBuilder;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import scripts.commands.IUserCommandHandler;

public class ClanPenalty
  implements IUserCommandHandler
{
  private static final int[] COMMAND_IDS = { 100 };

  public boolean useUserCommand(int id, L2PcInstance activeChar)
  {
    if (id != COMMAND_IDS[0]) return false;

    String penaltyStr = "No current penalties in effect.";

    TextBuilder htmlContent = new TextBuilder("<html><body>");
    htmlContent.append("<center><table width=\"270\" border=\"0\" bgcolor=\"111111\">");
    htmlContent.append("<tr><td width=\"170\">Penalty</td>");
    htmlContent.append("<td width=\"100\" align=\"center\">Expiration Date</td></tr>");
    htmlContent.append("</table><table width=\"270\" border=\"0\">");
    htmlContent.append("<tr><td>" + penaltyStr + "</td></tr>");
    htmlContent.append("</table></center>");
    htmlContent.append("</body></html>");

    NpcHtmlMessage penaltyHtml = NpcHtmlMessage.id(0);
    penaltyHtml.setHtml(htmlContent.toString());
    activeChar.sendPacket(penaltyHtml);

    return true;
  }

  public int[] getUserCommandList()
  {
    return COMMAND_IDS;
  }
}