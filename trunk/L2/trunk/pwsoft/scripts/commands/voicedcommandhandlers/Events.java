package scripts.commands.voicedcommandhandlers;

import javolution.text.TextBuilder;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import scripts.autoevents.basecapture.BaseCapture;
import scripts.autoevents.encounter.Encounter;
import scripts.autoevents.lasthero.LastHero;
import scripts.autoevents.masspvp.massPvp;
import scripts.commands.IVoicedCommandHandler;

public class Events
  implements IVoicedCommandHandler
{
  private static final String[] VOICED_COMMANDS = { "join", "leave", "eventhelp" };

  public boolean useVoicedCommand(String command, L2PcInstance player, String target) {
    if ((player.isOutOfControl()) || (player.isParalyzed()) || (player.underAttack())) {
      player.sendHtmlMessage("\u0412 \u0434\u0430\u043D\u043D\u044B\u0439 \u043C\u043E\u043C\u0435\u043D\u0442 \u0432\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0438\u0441\u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u044C \u0438\u0432\u0435\u043D\u0442 \u043A\u043E\u043C\u0430\u043D\u0434\u044B.");
      return false;
    }
    if (command.startsWith("join")) {
      String event = command.substring(4).trim();
      if (event.equalsIgnoreCase("tvt")) {
        if (!Config.TVT_EVENT_ENABLED) {
          player.sendHtmlMessage("\u0418\u0432\u0435\u043D\u0442 -\u0422\u0432\u0422- \u043E\u0442\u043A\u043B\u044E\u0447\u0435\u043D.");
          return false;
        }
        if ((Config.TVT_NOBL) && (!player.isNoble())) {
          player.sendHtmlMessage("\u0422\u043E\u043B\u044C\u043A\u043E \u043D\u043E\u0431\u043B\u0435\u0441\u0441\u044B \u043C\u043E\u0433\u0443\u0442 \u0443\u0447\u0430\u0432\u0441\u0442\u0432\u043E\u0432\u0430\u0442\u044C.");
          return false;
        }
        if ((Config.MASS_PVP) && (massPvp.getEvent().isReg(player))) {
          player.sendHtmlMessage("\u0423\u0434\u0430\u0447\u0438 \u043D\u0430 \u0435\u0432\u0435\u043D\u0442\u0435 -\u041C\u0430\u0441\u0441 \u041F\u0412\u041F-.");
          return false;
        }
        if ((Config.ELH_ENABLE) && (LastHero.getEvent().isRegged(player))) {
          player.sendHtmlMessage("\u0412\u044B \u0443\u0436\u0435 \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u043D\u044B \u0432 \u0435\u0432\u0435\u043D\u0442\u0435 -\u041F\u043E\u0441\u043B\u0435\u0434\u043D\u0438\u0439 \u0433\u0435\u0440\u043E\u0439-.");
          return false;
        }
        if ((Config.EBC_ENABLE) && (BaseCapture.getEvent().isRegged(player))) {
          player.sendHtmlMessage("\u0423\u0434\u0430\u0447\u0438 \u043D\u0430 \u0435\u0432\u0435\u043D\u0442\u0435 -\u0417\u0430\u0445\u0432\u0430\u0442 \u0431\u0430\u0437\u044B-");
          return false;
        }

        if (!TvTEvent.isParticipating()) {
          player.sendHtmlMessage("\u0420\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u044F \u0432 \u0434\u0430\u043D\u043D\u044B\u0439 \u043C\u043E\u043C\u0435\u043D\u0442 \u043D\u0435 \u0430\u043A\u0442\u0438\u0432\u043D\u0430.");
          return false;
        }

        if (TvTEvent.isPlayerParticipant(player.getName())) {
          player.sendHtmlMessage("\u0412\u044B \u0443\u0436\u0435 \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u043D\u044B.");
          return false;
        }
        TvTEvent.onBypass("tvt_event_participation", player);
      } else if (event.equalsIgnoreCase("lh")) {
        if (!Config.ELH_ENABLE) {
          player.sendHtmlMessage("\u0418\u0432\u0435\u043D\u0442 -\u041F\u043E\u0441\u043B\u0435\u0434\u043D\u0438\u0439 \u0433\u0435\u0440\u043E\u0439- \u043E\u0442\u043A\u043B\u044E\u0447\u0435\u043D.");
          return false;
        }
        LastHero.getEvent().regPlayer(player);
      } else if (event.equalsIgnoreCase("mpvp")) {
        if (!Config.MASS_PVP) {
          player.sendHtmlMessage("\u0418\u0432\u0435\u043D\u0442 -\u041F\u043E\u0441\u043B\u0435\u0434\u043D\u0438\u0439 \u0433\u0435\u0440\u043E\u0439- \u043E\u0442\u043A\u043B\u044E\u0447\u0435\u043D.");
          return false;
        }
        massPvp.getEvent().regPlayer(player);
      } else if (event.equalsIgnoreCase("bc")) {
        if (!Config.EBC_ENABLE) {
          player.sendHtmlMessage("\u0418\u0432\u0435\u043D\u0442 -\u0417\u0430\u0445\u0432\u0430\u0442 \u0431\u0430\u0437\u044B- \u043E\u0442\u043A\u043B\u044E\u0447\u0435\u043D.");
          return false;
        }
        BaseCapture.getEvent().regPlayer(player);
      } else if (event.equalsIgnoreCase("enc")) {
        if (!Config.EENC_ENABLE) {
          player.sendHtmlMessage("\u0418\u0432\u0435\u043D\u0442 -\u0414\u043E\u0437\u043E\u0440- \u043E\u0442\u043A\u043B\u044E\u0447\u0435\u043D.");
          return false;
        }
        Encounter.getEvent().regPlayer(player);
      }
    } else if (command.startsWith("leave")) {
      String event = command.substring(5).trim();
      if (event.equalsIgnoreCase("tvt")) {
        if (!Config.TVT_EVENT_ENABLED) {
          player.sendHtmlMessage("\u0418\u0432\u0435\u043D\u0442 -\u0422\u0432\u0422- \u043E\u0442\u043A\u043B\u044E\u0447\u0435\u043D.");
          return false;
        }
        if (TvTEvent.isParticipating()) {
          if (!TvTEvent.isPlayerParticipant(player.getName())) {
            player.sendHtmlMessage("\u0412\u044B \u043D\u0435 \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u043D\u044B.");
            return false;
          }
          TvTEvent.onBypass("tvt_event_remove_participation", player);
        } else {
          player.sendHtmlMessage("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u043F\u043E\u043A\u0438\u043D\u0443\u0442\u044C \u0438\u0432\u0435\u043D\u0442.");
          return false;
        }
      } else if (event.equalsIgnoreCase("lh")) {
        if (!Config.ELH_ENABLE) {
          player.sendHtmlMessage("\u0418\u0432\u0435\u043D\u0442 -\u041F\u043E\u0441\u043B\u0435\u0434\u043D\u0438\u0439 \u0433\u0435\u0440\u043E\u0439- \u043E\u0442\u043A\u043B\u044E\u0447\u0435\u043D.");
          return false;
        }
        LastHero.getEvent().delPlayer(player);
      } else if (event.equalsIgnoreCase("mpvp")) {
        if (!Config.MASS_PVP) {
          player.sendHtmlMessage("\u0418\u0432\u0435\u043D\u0442 -\u041F\u043E\u0441\u043B\u0435\u0434\u043D\u0438\u0439 \u0433\u0435\u0440\u043E\u0439- \u043E\u0442\u043A\u043B\u044E\u0447\u0435\u043D.");
          return false;
        }
        massPvp.getEvent().onExit(player);
      } else if (event.equalsIgnoreCase("bc")) {
        if (!Config.EBC_ENABLE) {
          player.sendHtmlMessage("\u0418\u0432\u0435\u043D\u0442 -\u0417\u0430\u0445\u0432\u0430\u0442 \u0431\u0430\u0437\u044B- \u043E\u0442\u043A\u043B\u044E\u0447\u0435\u043D.");
          return false;
        }
        BaseCapture.getEvent().delPlayer(player);
      } else if (event.equalsIgnoreCase("enc")) {
        if (!Config.EENC_ENABLE) {
          player.sendHtmlMessage("\u0418\u0432\u0435\u043D\u0442 -\u0414\u043E\u0437\u043E\u0440- \u043E\u0442\u043A\u043B\u044E\u0447\u0435\u043D.");
          return false;
        }
        Encounter.getEvent().delPlayer(player);
      }
    } else {
      NpcHtmlMessage nhm = NpcHtmlMessage.id(5);
      TextBuilder tb = new TextBuilder("<html><body>");
      tb.append("<center><font color=\"LEVEL\">\u0418\u0432\u0435\u043D\u0442 \u043A\u043E\u043C\u0430\u043D\u0434\u044B</font></center><br><br>");
      if (Config.TVT_EVENT_ENABLED) {
        tb.append("<font color=CC3366>Team vs Team (\u0422\u0432\u0422)</font><br1>");
        tb.append("<font color=66CC00>.join tvt</font> \u043F\u0440\u0438\u043D\u044F\u0442\u044C \u0443\u0447\u0430\u0441\u0442\u0438\u0435.<br1>");
        tb.append("<font color=66CC00>.leave tvt</font> \u043E\u0442\u043A\u0430\u0437\u0430\u0442\u044C\u0441\u044F.<br>");
      }
      if (Config.MASS_PVP) {
        tb.append("<font color=CC3366>Mass PvP (\u041C\u0430\u0441\u0441 \u041F\u0432\u043F)</font><br1>");
        tb.append("<font color=66CC00>.join mpvp</font> \u043F\u0440\u0438\u043D\u044F\u0442\u044C \u0443\u0447\u0430\u0441\u0442\u0438\u0435.<br1>");
        tb.append("<font color=66CC00>.leave mpvp</font> \u043E\u0442\u043A\u0430\u0437\u0430\u0442\u044C\u0441\u044F.<br>");
      }
      if (Config.ELH_ENABLE) {
        tb.append("<font color=CC3366>Last Hero (\u041F\u043E\u0441\u043B\u0435\u0434\u043D\u0438\u0439 \u0433\u0435\u0440\u043E\u0439)</font><br1>");
        tb.append("<font color=66CC00>.join lh</font> \u043F\u0440\u0438\u043D\u044F\u0442\u044C \u0443\u0447\u0430\u0441\u0442\u0438\u0435.<br1>");
        tb.append("<font color=66CC00>.leave lh</font> \u043E\u0442\u043A\u0430\u0437\u0430\u0442\u044C\u0441\u044F.<br>");
      }
      if (Config.EBC_ENABLE) {
        tb.append("<font color=CC3366>Base Capture (\u0417\u0430\u0445\u0432\u0430\u0442 \u0431\u0430\u0437\u044B)</font><br1>");
        tb.append("<font color=66CC00>.join bc</font> \u043F\u0440\u0438\u043D\u044F\u0442\u044C \u0443\u0447\u0430\u0441\u0442\u0438\u0435.<br1>");
        tb.append("<font color=66CC00>.leave bc</font> \u043E\u0442\u043A\u0430\u0437\u0430\u0442\u044C\u0441\u044F.<br>");
      }
      if (Config.EENC_ENABLE) {
        tb.append("<font color=CC3366>Encounter (\u0414\u043E\u0437\u043E\u0440)</font><br1>");
        tb.append("<font color=66CC00>.join enc</font> \u043F\u0440\u0438\u043D\u044F\u0442\u044C \u0443\u0447\u0430\u0441\u0442\u0438\u0435.<br1>");
        tb.append("<font color=66CC00>.leave enc</font> \u043E\u0442\u043A\u0430\u0437\u0430\u0442\u044C\u0441\u044F.<br>");
      }
      tb.append("<br><br>");
      tb.append("</body></html>");
      nhm.setHtml(tb.toString());
      player.sendPacket(nhm);
    }
    return true;
  }

  public String[] getVoicedCommandList() {
    return VOICED_COMMANDS;
  }
}