package scripts.commands.voicedcommandhandlers;

import javolution.text.TextBuilder;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance.UserKey;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.util.Util;
import scripts.commands.IVoicedCommandHandler;

public class Security
  implements IVoicedCommandHandler
{
  private static final String[] VOICED_COMMANDS = { "security", "security_" };

  public boolean useVoicedCommand(String command, L2PcInstance player, String target)
  {
    if (command.equalsIgnoreCase("security")) {
      showWelcome(player);
    } else if (command.startsWith("security_")) {
      String choise = command.substring(9).trim();
      if (choise.startsWith("hwid")) {
        int flag = Integer.parseInt(choise.substring(4).trim());
        player.saveHWID(flag == 1);
        showWelcome(player);
        return true;
      }if (choise.startsWith("pwd")) {
        String pass = "";
        String passr = "";
        String error = "\u041F\u0440\u043E\u0438\u0437\u043E\u0448\u043B\u0430 \u043E\u0448\u0438\u0431\u043A\u0430 \u043F\u0440\u0438 \u0443\u0441\u0442\u0430\u043D\u043E\u0432\u043A\u0435 \u043F\u0430\u0440\u043E\u043B\u044F.";
        boolean ok = false;
        try {
          String[] pwd = choise.substring(4).split(" ");
          pass = pwd[0];
          passr = pwd[1];
          if ((pass.length() < 5) || (pass.length() > 15) || (passr.length() < 5) || (passr.length() > 15))
            error = "\u041F\u0430\u0440\u043E\u043B\u044C \u0434\u043E\u043B\u0436\u0435\u043D \u0431\u044B\u0442\u044C \u0431\u043E\u043B\u0435\u0435 5 \u0438 \u043C\u0435\u043D\u0435\u0435 15 \u0441\u0438\u043C\u0432\u043E\u043B\u043E\u0432.";
          else if ((!Util.isAlphaNumeric(pass)) || (!Util.isAlphaNumeric(passr)))
            error = "\u041F\u0430\u0440\u043E\u043B\u044C \u0441\u043E\u0434\u0435\u0440\u0436\u0438\u0442 \u0437\u0430\u043F\u0440\u0435\u0449\u0435\u043D\u043D\u044B\u0435 \u0441\u0438\u043C\u0432\u043E\u043B\u044B.";
          else if (!pass.equalsIgnoreCase(passr))
            error = "\u041F\u0430\u0440\u043E\u043B\u0438 \u043D\u0435 \u0441\u043E\u0432\u043F\u0430\u0434\u0430\u044E\u0442.";
          else
            ok = true;
        }
        catch (Exception e) {
          ok = false;
        }
        if ((ok) && (player.updatePassword(pass)))
          player.sendHtmlMessage("\u041D\u043E\u0432\u044B\u0439 \u043F\u0430\u0440\u043E\u043B\u044C \u0443\u0441\u0442\u0430\u043D\u043E\u0432\u043B\u0435\u043D.");
        else {
          player.sendHtmlMessage(error);
        }

        return true;
      }if (choise.startsWith("email")) {
        String pass = "";
        String passr = "";
        String error = "\u041F\u0440\u043E\u0438\u0437\u043E\u0448\u043B\u0430 \u043E\u0448\u0438\u0431\u043A\u0430 \u043F\u0440\u0438 \u0443\u0441\u0442\u0430\u043D\u043E\u0432\u043A\u0435 \u0438\u043C\u0435\u0439\u043B\u0430.";
        boolean ok = false;
        try {
          String[] pwd = choise.substring(6).split(" ");
          pass = pwd[0];
          passr = pwd[1];
          if ((pass.length() < 5) || (pass.length() > 35) || (passr.length() < 5) || (passr.length() > 35))
            error = "\u0418\u043C\u0435\u0438\u043B \u0434\u043E\u043B\u0436\u0435\u043D \u0431\u044B\u0442\u044C \u0431\u043E\u043B\u0435\u0435 5 \u0438 \u043C\u0435\u043D\u0435\u0435 35 \u0441\u0438\u043C\u0432\u043E\u043B\u043E\u0432.";
          else if ((!Util.isValidEmail(pass)) || (!Util.isValidEmail(passr)))
            error = "\u0418\u043C\u0435\u0438\u043B \u0441\u043E\u0434\u0435\u0440\u0436\u0438\u0442 \u0437\u0430\u043F\u0440\u0435\u0449\u0435\u043D\u043D\u044B\u0435 \u0441\u0438\u043C\u0432\u043E\u043B\u044B.";
          else if (!pass.equalsIgnoreCase(passr))
            error = "\u0418\u043C\u0435\u0439\u043B\u044B \u043D\u0435 \u0441\u043E\u0432\u043F\u0430\u0434\u0430\u044E\u0442.";
          else
            ok = true;
        }
        catch (Exception e) {
          ok = false;
        }
        if ((ok) && (player.updateEmail(pass))) {
          player.getClient().setHasEmail(true);
          player.sendHtmlMessage("\u041D\u043E\u0432\u044B\u0439 \u0438\u043C\u0435\u0438\u043B \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u0443\u0441\u0442\u0430\u043D\u043E\u0432\u043B\u0435\u043D.<center><br>==========<br> <font color=\"33CC00\">" + pass + "</font><br>==========</center>");
        }
        else {
          player.sendAdmResultMessage(error);
          player.sendUserPacket(Static.CHANGE_EMAIL);
        }
        return true;
      }
      return false;
    }
    return true;
  }

  private void showWelcome(L2PcInstance player) {
    if ((player.isParalyzed()) || (player.getUserKey().on == 1)) {
      player.sendActionFailed();
      return;
    }

    NpcHtmlMessage nhm = NpcHtmlMessage.id(5);
    TextBuilder build = new TextBuilder("<html><body>");
    build.append("<font color=\"LEVEL\">\u041F\u0430\u0440\u0430\u043C\u0435\u0442\u0440\u044B \u0431\u0435\u0437\u043E\u043F\u0430\u0441\u043D\u043E\u0441\u0442\u0438.</font><br>");
    build.append("<table width=290>");
    if (Config.VS_HWID) {
      build.append("<tr><td width=180><font color=66CC00>\u041F\u0440\u0438\u0432\u044F\u0437\u043A\u0430 \u0447\u0430\u0440\u0430 \u043A \u043A\u043E\u043C\u043F\u044C\u044E\u0442\u0435\u0440\u0443:</font></td>");
      if (player.getMyHWID().length() > 5) {
        build.append("<td>[\u0412\u043A\u043B.]</td>");
        build.append("<td><button value=\"\u0412\u044B\u043A\u043B.\" action=\"bypass -h security_hwid 0\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
      } else {
        build.append("<td><button value=\"\u0412\u043A\u043B.\" action=\"bypass -h security_hwid 1\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        build.append("<td>[\u0412\u044B\u043A\u043B.]</td></tr>");
      }
    }
    if (Config.VS_PWD) {
      build.append("<tr><td width=180><font color=66CC00>\u0421\u043C\u0435\u043D\u0430 \u043F\u0430\u0440\u043E\u043B\u044F:</font></td>");
      build.append("<td></td><td></td></tr><tr><td width=180>\u0412\u0432\u0435\u0434\u0438\u0442\u0435 \u043D\u043E\u0432\u044B\u0439 \u043F\u0430\u0440\u043E\u043B\u044C:</td><td> </td><td></td></tr>");
      build.append("<tr><td><edit var=\"pwd\" width=120 length=\"16\"></td>");
      build.append("<td></td><td></td></tr><tr><td width=180>\u041F\u043E\u0432\u0442\u043E\u0440\u0438\u0442\u0435 \u043D\u043E\u0432\u044B\u0439 \u043F\u0430\u0440\u043E\u043B\u044C:</td><td> </td><td></td></tr>");
      build.append("<tr><td><edit var=\"pwdr\" width=120 length=\"16\"></td>");
      build.append("<td><button value=\"\u0412\u043A\u043B.\" action=\"bypass -h security_pwd $pwd $pwdr\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td> </td></tr>");
    }
    build.append("</table></body></html>");
    nhm.setHtml(build.toString());
    player.sendPacket(nhm);
    build.clear();
    build = null;
  }

  public String[] getVoicedCommandList()
  {
    return VOICED_COMMANDS;
  }
}