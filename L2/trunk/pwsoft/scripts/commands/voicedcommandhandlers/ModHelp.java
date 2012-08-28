package scripts.commands.voicedcommandhandlers;

import javolution.text.TextBuilder;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import scripts.commands.IVoicedCommandHandler;

public class ModHelp
  implements IVoicedCommandHandler
{
  private static final String[] VOICED_COMMANDS = { "moderhelp" };

  public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
  {
    if (activeChar.isModerator())
    {
      String Moder = activeChar.getForumName();
      int rank = activeChar.getModerRank();

      if (command.equalsIgnoreCase("moderhelp"))
      {
        NpcHtmlMessage nhm = NpcHtmlMessage.id(5);
        TextBuilder build = new TextBuilder("<html><body>");
        build.append("<center><font color=\"LEVEL\">\u041F\u043E\u043C\u043E\u0449\u044C</font></center><br><br>");
        build.append("\u041C\u043E\u0434\u0435\u0440\u0430\u0442\u043E\u0440 <font color=\"LEVEL\">" + Moder + "</font>; rank " + rank + "<br><br>");
        build.append("C\u043F\u0438\u0441\u043E\u043A \u043A\u043E\u043C\u0430\u043D\u0434:<br>");
        build.append("<font color=66CC00>.\u0441\u0438\u043D\u0442\u0430\u043A\u0441\u0438\u0441</font> (\u043A\u0430\u043A \u0440\u0430\u0431\u043E\u0442\u0430\u0435\u0442); \u043E\u043F\u0438\u0441\u0430\u043D\u0438\u0435<br>");
        build.append("<font color=66CC00>.banchat</font> (.banchat Hope 120); \u0411\u0430\u043D \u0447\u0430\u0442\u0430 \u0432 \u043C\u0438\u043D\u0443\u0442\u0430\u0445<br>");
        build.append("<font color=66CC00>.unbanchat</font> (.unbanchat Hope); \u0421\u043D\u044F\u0442\u044C \u0431\u0430\u043D \u0447\u0430\u0442\u0430<br>");
        build.append("<font color=66CC00>.kick</font> (\u0432\u0437\u044F\u0442\u044C \u0447\u0430\u0440\u0430 \u0432 \u0442\u0430\u0440\u0433\u0435\u0442); \u041A\u0438\u043A\u043D\u0443\u0442\u044C \u0442\u043E\u0440\u0433\u043E\u0432\u0446\u0430<br>");
        build.append("<font color=66CC00>.cleartitle</font> (\u0432\u0437\u044F\u0442\u044C \u0447\u0430\u0440\u0430 \u0432 \u0442\u0430\u0440\u0433\u0435\u0442); \u0423\u0434\u0430\u043B\u0438\u0442\u044C \u0442\u0438\u0442\u0443\u043B<br>");
        switch (rank)
        {
        case 2:
          build.append("<font color=CC9900>.showstat</font> (.showstat Nick); \u0421\u0442\u0430\u0442\u044B \u0447\u0430\u0440\u0430<br>");
        }

        build.append("<br><br>");
        build.append("</body></html>");
        nhm.setHtml(build.toString());
        activeChar.sendPacket(nhm);
      }
    }
    return true;
  }

  public String[] getVoicedCommandList()
  {
    return VOICED_COMMANDS;
  }
}