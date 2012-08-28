package scripts.commands.voicedcommandhandlers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javolution.text.TextBuilder;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.AutoRestart;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance.UserKey;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.util.Util;
import scripts.commands.IVoicedCommandHandler;

public class Menu
  implements IVoicedCommandHandler
{
  private static final String[] VOICED_COMMANDS = { "menu", "menu_" };
  private static final Pattern keyPattern = Pattern.compile("[\\w\\u005F\\u002E]+", 64);

  public boolean useVoicedCommand(String command, L2PcInstance player, String target)
  {
    if (player.isAlikeDead())
    {
      showWelcome(player);
      return true;
    }
    if (command.equalsIgnoreCase("menu")) {
      showWelcome(player);
    } else if (command.startsWith("menu_")) {
      String choise = command.substring(5).trim();
      if (choise.startsWith("exp")) {
        int flag = Integer.parseInt(choise.substring(3).trim());

        player.setNoExp(flag == 1);
        showWelcome(player);
        return true;
      }if (choise.startsWith("alone")) {
        int flag = Integer.parseInt(choise.substring(5).trim());

        player.setAlone(flag == 1);
        showWelcome(player);
        return true;
      }if (choise.startsWith("autoloot")) {
        int flag = Integer.parseInt(choise.substring(8).trim());

        player.setAutoLoot(flag == 1);
        showWelcome(player);
        return true;
      }if (choise.startsWith("pathfind")) {
        int flag = Integer.parseInt(choise.substring(8).trim());

        player.setGeoPathfind(flag == 1);
        showWelcome(player);
        return true;
      }if (choise.startsWith("skillchance")) {
        int flag = Integer.parseInt(choise.substring(11).trim());

        player.setShowSkillChances(flag == 1);
        showWelcome(player);
        return true;
      }if (choise.startsWith("showshots")) {
        int flag = Integer.parseInt(choise.substring(9).trim());

        player.setSoulShotsAnim(flag == 1);
        showWelcome(player);
        return true;
      }if (choise.startsWith("blockchat")) {
        int flag = 0;
        try {
          flag = Integer.parseInt(choise.substring(9).trim());
        }
        catch (Exception ignored) {
        }
        player.setChatIgnore(flag);

        showWelcome(player);
        return true;
      }if (choise.startsWith("vote")) {
        try {
          String nick = choise.substring(4);
          if ((nick.length() >= 3) && (nick.length() <= 16) && (!nick.equals(player.getName()))) {
            player.setVoteRf(nick);
          }

          showWelcome(player);
        }
        catch (Exception ignored)
        {
        }
        showWelcome(player);
        return true;
      }if (choise.equalsIgnoreCase("offvote")) {
        player.delVoteRef();
        showWelcome(player);
        return true;
      }if (choise.startsWith("key")) {
        if (!player.getUserKey().key.equalsIgnoreCase("")) {
          return true;
        }
        try
        {
          String key = choise.substring(3).trim();
          if (isValidKey(key)) {
            player.setUserKey(key);
          }
          else {
            player.sendPacket(Static.SET_KEY_ERROR4_16);
            return true;
          }
        }
        catch (Exception ignored)
        {
        }

        return true;
      }if (choise.startsWith("chkkey")) {
        try {
          String key = choise.substring(6).trim();
          if (!player.getUserKey().key.equalsIgnoreCase(key)) {
            if (player.getUserKey().checkLeft()) {
              player.kick();
              return false;
            }
            player.sendHtmlMessage("\u041D\u0435\u0432\u0435\u0440\u043D\u044B\u0439 \u043F\u0430\u0440\u043E\u043B\u044C.");
            return true;
          }
        }
        catch (Exception ignored) {
        }
        player.unsetUserKey();
        if ((Config.VS_EMAIL) && (!player.hasEmail())) {
          player.sendUserPacket(Static.CHANGE_EMAIL);
        }
        else if (Config.SERVER_NEWS)
          player.sendUserPacket(Static.SERVER_WELCOME);
        else {
          showWelcome(player);
        }
        return true;
      }if (choise.equalsIgnoreCase("offkey")) {
        player.delUserKey();
        showWelcome(player);
        return true;
      }

      return false;
    }
    return true;
  }

  private boolean isValidKey(String key) {
    if (!Util.isAlphaNumeric(key)) {
      return false;
    }
    if ((key.length() < 3) || (key.length() > 16)) {
      return false;
    }
    return keyPattern.matcher(key).matches();
  }

  private void showWelcome(L2PcInstance player) {
    NpcHtmlMessage nhm = NpcHtmlMessage.id(5);
    TextBuilder build = new TextBuilder("<html><body>");
    if (Config.VS_ONLINE) {
      build.append("\u0418\u0433\u0440\u043E\u043A\u043E\u0432 \u043E\u043D\u043B\u0430\u0439\u043D: <font color=33CC00>" + L2World.getInstance().getAllPlayersCount() + "</font><br1><img src=\"L2UI.SquareWhite\" width=150 height=1><br>");
    }
    if ((Config.VS_AUTORESTAT) && (Config.RESTART_HOUR > 0)) {
      build.append("\u0412\u0440\u0435\u043C\u044F \u0434\u043E \u0440\u0435\u0441\u0442\u0430\u0440\u0442\u0430: <font color=33CC00>" + AutoRestart.getInstance().remain() / 60000L + "</font> \u043C\u0438\u043D\u0443\u0442.<br1><img src=\"L2UI.SquareWhite\" width=150 height=1><br>");
    }
    build.append("<font color=\"LEVEL\">\u041F\u0430\u0440\u0430\u043C\u0435\u0442\u0440\u044B \u0438\u0433\u0440\u043E\u043A\u0430 </font>");
    build.append("<table width=290><br>");
    int all = 0;
    if (Config.VS_NOEXP) {
      build.append("<tr><td width=180><font color=66CC00>\u041E\u0442\u043A\u0430\u0437 \u043E\u0442 \u043E\u043F\u044B\u0442\u0430:</font></td>");
      if (player.isNoExp()) {
        build.append("<td>[\u0412\u043A\u043B.]</td>");
        build.append("<td><button value=\"\u0412\u044B\u043A\u043B.\" action=\"bypass -h menu_exp 0\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
      } else {
        build.append("<td><button value=\"\u0412\u043A\u043B.\" action=\"bypass -h menu_exp 1\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        build.append("<td>[\u0412\u044B\u043A\u043B.]</td></tr>");
      }
      all++;
    }

    if (Config.VS_NOREQ) {
      build.append("<tr><td width=180><font color=66CC00>\u041E\u0442\u043A\u0430\u0437 \u043E\u0442 \u0442\u0440\u0435\u0439\u0434\u0430/\u043F\u0430\u0442\u0438 \u0438 \u0442.\u0434.:</font></td>");
      if (player.isAlone()) {
        build.append("<td>[\u0412\u043A\u043B.]</td>");
        build.append("<td><button value=\"\u0412\u044B\u043A\u043B.\" action=\"bypass -h menu_alone 0\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
      } else {
        build.append("<td><button value=\"\u0412\u043A\u043B.\" action=\"bypass -h menu_alone 1\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        build.append("<td>[\u0412\u044B\u043A\u043B.]</td></tr>");
      }
      all++;
    }

    if (Config.VS_AUTOLOOT) {
      build.append("<tr><td width=180><font color=66CC00>\u0410\u0432\u0442\u043E\u043B\u0443\u0442:</font></td>");
      if (player.getAutoLoot()) {
        build.append("<td>[\u0412\u043A\u043B.]</td>");
        build.append("<td><button value=\"\u0412\u044B\u043A\u043B.\" action=\"bypass -h menu_autoloot 0\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
      } else {
        build.append("<td><button value=\"\u0412\u043A\u043B.\" action=\"bypass -h menu_autoloot 1\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        build.append("<td>[\u0412\u044B\u043A\u043B.]</td></tr>");
      }
      all++;
    }

    if ((Config.VS_PATHFIND) && (Config.GEODATA == 2)) {
      build.append("<tr><td width=180><font color=66CC00>\u041E\u0433\u0438\u0431\u0430\u043D\u0438\u0435 \u043F\u0440\u0435\u043F\u044F\u0442\u0441\u0442\u0432\u0438\u0439:</font></td>");
      if (player.geoPathfind()) {
        build.append("<td>[\u0412\u043A\u043B.]</td>");
        build.append("<td><button value=\"\u0412\u044B\u043A\u043B.\" action=\"bypass -h menu_pathfind 0\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
      } else {
        build.append("<td><button value=\"\u0412\u043A\u043B.\" action=\"bypass -h menu_pathfind 1\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        build.append("<td>[\u0412\u044B\u043A\u043B.]</td></tr>");
      }
      all++;
    }

    if (Config.VS_SKILL_CHANCES) {
      build.append("<tr><td width=180><font color=66CC00>\u0428\u0430\u043D\u0441\u044B \u043F\u0440\u043E\u0445\u043E\u0436\u0434\u0435\u043D\u0438\u044F \u0441\u043A\u0438\u043B\u043B\u043E\u0432:</font></td>");
      if (player.getShowSkillChances()) {
        build.append("<td>[\u0412\u043A\u043B.]</td>");
        build.append("<td><button value=\"\u0412\u044B\u043A\u043B.\" action=\"bypass -h menu_skillchance 0\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
      } else {
        build.append("<td><button value=\"\u0412\u043A\u043B.\" action=\"bypass -h menu_skillchance 1\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        build.append("<td>[\u0412\u044B\u043A\u043B.]</td></tr>");
      }
      all++;
    }

    if (Config.VS_ANIM_SHOTS) {
      build.append("<tr><td width=180><font color=66CC00>\u0410\u043D\u0438\u043C\u0430\u0446\u0438\u044F \u0441\u0443\u043B\u0448\u043E\u0442\u043E\u0432:</font></td>");
      if (player.showSoulShotsAnim()) {
        build.append("<td>[\u0412\u043A\u043B.]</td>");
        build.append("<td><button value=\"\u0412\u044B\u043A\u043B.\" action=\"bypass -h menu_showshots 0\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
      } else {
        build.append("<td><button value=\"\u0412\u043A\u043B.\" action=\"bypass -h menu_showshots 1\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        build.append("<td>[\u0412\u044B\u043A\u043B.]</td></tr>");
      }
      all++;
    }

    if (Config.VS_VREF) {
      build.append("<tr><td width=180><br><font color=66CC00>\u0413\u043E\u043B\u043E\u0441\u043E\u0432\u0430\u043D\u0438\u0435 \u043D\u0430 \u043D\u0438\u043A<font color=FFCC33>*</font>:</font></td>");
      if (!player.voteRef().equalsIgnoreCase("")) {
        build.append("<td></td><td></td></tr>");
        build.append("<tr><td width=180><font color=3399CC>" + player.voteRef() + "</font></td>");
        build.append("<td> </td>");
        build.append("<td><button value=\"\u0412\u044B\u043A\u043B.\" action=\"bypass -h menu_offvote\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
      } else {
        build.append("<td></td><td></td></tr><tr><td width=180>\u0412\u0432\u0435\u0434\u0438\u0442\u0435 \u043D\u0438\u043A:</td><td> </td><td></td></tr>");
        build.append("<tr><td><edit var=\"name\" width=120 length=\"16\"></td>");
        build.append("<td><button value=\"\u0412\u043A\u043B.\" action=\"bypass -h menu_vote $name\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td> </td></tr>");
      }
      all++;
    }

    if (Config.VS_CKEY) {
      L2PcInstance.UserKey uk = player.getUserKey();
      build.append("<tr><td width=180><br><font color=66CC00>\u041A\u043B\u044E\u0447 \u043E\u0442 \u0447\u0430\u0440\u0430*<font color=FFCC33></font>:</font></td>");
      if (!uk.key.equalsIgnoreCase("")) {
        if (uk.on == 1) {
          build.append("<td></td><td></td></tr><tr><td width=180>\u041A\u0430\u043A\u043E\u0439 \u043A\u043B\u044E\u0447?</td><td> </td><td></td></tr>");
          build.append("<tr><td><edit var=\"key\" width=120 length=\"16\"></td>");
          build.append("<td><button value=\"\u041E\u043A\" action=\"bypass -h menu_chkkey $key\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td> </td></tr>");
        } else {
          build.append("<td></td><td></td></tr><tr><td width=180>\u041A\u043B\u044E\u0447 \u043E\u0442 \u0447\u0430\u0440\u0430</td><td> </td><td></td></tr>");
          build.append("<tr><td><font color=3399CC>*******</font></td>");
          build.append("<td><button value=\"\u0412\u044B\u043A\u043B.\" action=\"bypass -h menu_offkey\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td> </td></tr>");
        }
      } else {
        build.append("<td></td><td></td></tr><tr><td width=180>\u041A\u043B\u044E\u0447:</td><td> </td><td></td></tr>");
        build.append("<tr><td><edit var=\"key\" width=120 length=\"16\"></td>");
        build.append("<td><button value=\"\u0412\u043A\u043B.\" action=\"bypass -h menu_key $key\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td> </td></tr>");
      }
      all++;
    }
    if (all == 0) {
      build.append("<tr><td>\u0412\u0441\u0435 \u0441\u0435\u0440\u0432\u0438\u0441\u044B \u043E\u0442\u043A\u043B\u044E\u0447\u0435\u043D\u044B.</td></tr>");
    }
    build.append("</table><br>");

    if (Config.VS_CHATIGNORE) {
      int ignore = player.getChatIgnore();
      build.append("<font color=\"LEVEL\">\u041F\u0430\u0440\u0430\u043C\u0435\u0442\u0440\u044B \u0447\u0430\u0442\u0430 </font><table width=290><tr><td width=180><font color=66CC00>\u0418\u0433\u043D\u043E\u0440\u0438\u0440\u043E\u0432\u0430\u0442\u044C \u0438\u0433\u0440\u043E\u043A\u043E\u0432 \u043D\u0438\u0436\u0435:</font></td>");
      if (ignore > 0) {
        build.append("<td align=right width=20>" + ignore + "</td><td width=20>\u0443\u0440.</td>");
        build.append("<td><button value=\"\u0412\u044B\u043A\u043B.\" action=\"bypass -h menu_blockchat 0\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
      } else {
        build.append("<td><edit var=\"lvl\" width=20 length=\"2\"></td><td width=20>\u0443\u0440.</td>");
        build.append("<td><button value=\"\u0412\u043A\u043B.\" action=\"bypass -h menu_blockchat $lvl\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
      }
      build.append("</tr></table><br>");
    }

    if (Config.VS_VREF) {
      build.append("<font color=FFCC33>* \u043F\u0440\u0438 \u0433\u043E\u043B\u043E\u0441\u043E\u0432\u0430\u043D\u0438\u0438 \u0432 \u041B2\u0422\u041E\u041F \u043D\u0430\u0433\u0440\u0430\u0434\u0430 \u0431\u0443\u0434\u0435\u0442 \u0438\u0434\u0442\u0438 \u043D\u0430 \u0443\u043A\u0430\u0437\u0430\u043D\u043D\u044B\u044B\u0439 \u043D\u0438\u043A.</font><br><br>");
    }
    if (Config.VS_CKEY) {
      build.append("<font color=FFCC33>* \u0441 \u043A\u043B\u044E\u0447\u0435\u043C \u043D\u0435\u043B\u044C\u0437\u044F \u0431\u0443\u0434\u0435\u0442 \u0442\u043E\u0440\u0433\u043E\u0432\u0430\u0442\u044C, \u0445\u043E\u0434\u0438\u0442\u044C, \u0438\u0441\u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u044C \u0438\u043D\u0432\u0435\u043D\u0442\u0430\u0440\u044C.</font>");
    }
    build.append("</body></html>");
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