package net.sf.l2j.gameserver.model.actor.instance;

import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Multisell;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.olympiad.CompType;
import net.sf.l2j.gameserver.model.entity.olympiad.Olympiad;
import net.sf.l2j.gameserver.model.entity.olympiad.OlympiadDatabase;
import net.sf.l2j.gameserver.model.entity.olympiad.OlympiadGame;
import net.sf.l2j.gameserver.model.entity.olympiad.OlympiadManager;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.util.log.AbstractLogger;

public class L2OlympiadManagerInstance extends L2NpcInstance
{
  private static Logger _log = AbstractLogger.getLogger(L2OlympiadManagerInstance.class.getName());
  private static final short noblesseGatePass = 6651;

  public L2OlympiadManagerInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    if (command.startsWith("OlympiadDesc")) {
      int val = Integer.parseInt(command.substring(13, 14));
      String suffix = command.substring(14);
      showChatWindow(player, val, suffix);
    } else if (command.startsWith("OlympiadNoble")) {
      int classId = player.getClassId().getId();
      if ((!player.isNoble()) || (classId < 88) || ((classId > 118) && (classId < 131)) || (classId > 134)) {
        player.sendActionFailed();
        return;
      }

      if ((player.isInOlympiadMode()) || (player.getOlympiadGameId() > -1)) {
        player.sendActionFailed();
        return;
      }

      int val = Integer.parseInt(command.substring(14));
      NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());

      switch (val) {
      case 1:
        Olympiad.unRegisterNoble(player);
        break;
      case 2:
        int classed = 0;
        int nonClassed = 0;
        int[] array = Olympiad.getWaitingList();

        if (array != null) {
          classed = array[0];
          nonClassed = array[1];
        }

        reply.setFile("data/html/olympiad/noble_registered.htm");
        reply.replace("%listClassed%", String.valueOf(classed));
        reply.replace("%listNonClassed%", String.valueOf(nonClassed));

        player.sendPacket(reply);
        break;
      case 3:
        int points = Olympiad.getNoblePoints(player.getObjectId());
        reply.setFile("data/html/olympiad/noble_points1.htm");
        reply.replace("%points%", String.valueOf(points));
        player.sendPacket(reply);
        break;
      case 4:
        Olympiad.registerNoble(player, CompType.NON_CLASSED);
        break;
      case 5:
        Olympiad.registerNoble(player, CompType.CLASSED);
        break;
      case 6:
        int passes = Olympiad.getNoblessePasses(player);
        if (passes > 0) {
          L2ItemInstance item = player.getInventory().addItem("Olympiad", 6651, passes, player, null);
          player.sendPacket(SystemMessage.id(SystemMessageId.EARNED_S2_S1_S).addNumber(passes).addItemName(item.getItemId()));
        } else {
          reply.setFile("data/html/olympiad/noble_nopoints.htm");
          player.sendPacket(reply);
        }
        break;
      case 7:
        L2Multisell.getInstance().SeparateAndSend(102, player, false, getCastle().getTaxRate());
        break;
      case 8:
        int point = Olympiad.getNoblePointsPast(player.getObjectId());
        reply.setFile("data/html/olympiad/noble_points2.htm");
        reply.replace("%points%", String.valueOf(point));
        player.sendPacket(reply);
        break;
      case 9:
        L2Multisell.getInstance().SeparateAndSend(103, player, false, getCastle().getTaxRate());
        break;
      default:
        _log.warning(new StringBuilder().append("Olympiad System: Couldnt send packet for request ").append(val).toString());
      }
    }
    else if (command.startsWith("Olympiad")) {
      if ((Olympiad.isRegisteredInComp(player)) || (player.isInOlympiadMode()) || (player.getOlympiadGameId() > -1)) {
        player.sendActionFailed();
        return;
      }

      int val = Integer.parseInt(command.substring(9, 10));

      NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());

      switch (val) {
      case 1:
        StringBuilder replace = new StringBuilder("");
        OlympiadManager manager = Olympiad._manager;
        if (manager != null) {
          for (int i = 0; i < Olympiad.STADIUMS.length; i++) {
            OlympiadGame game = manager.getOlympiadInstance(i);
            if ((game != null) && (game.getState() > 0)) {
              replace.append(new StringBuilder().append("<br1>Arena ").append(i + 1).append(":&nbsp;<a action=\"bypass -h npc_").append(getObjectId()).append("_Olympiad 3_").append(i).append("\">").append(manager.getOlympiadInstance(i).getTitle()).append("</a>").toString());
              replace.append("<img src=\"L2UI.SquareWhite\" width=270 height=1> <img src=\"L2UI.SquareBlank\" width=1 height=3>");
            }
          }
        }

        reply.setFile("data/html/olympiad/olympiad_observe.htm");
        reply.replace("%arenas%", replace.toString());
        reply.replace("%objectId%", String.valueOf(getObjectId()));
        player.sendPacket(reply);
        break;
      case 2:
        int classId = Integer.parseInt(command.substring(11));
        if (classId < 88) break;
        reply.setFile("data/html/olympiad/olympiad_ranking.htm");

        FastList names = OlympiadDatabase.getClassLeaderBoard(classId);

        int index = 1;
        for (String name : names) {
          if (name == null) {
            continue;
          }
          reply.replace(new StringBuilder().append("%place").append(index).append("%").toString(), String.valueOf(index));
          reply.replace(new StringBuilder().append("%rank").append(index).append("%").toString(), Util.htmlSpecialChars(name));
          index++;
          if (index > 10) {
            break;
          }
        }
        for (; index <= 10; index++) {
          reply.replace(new StringBuilder().append("%place").append(index).append("%").toString(), "");
          reply.replace(new StringBuilder().append("%rank").append(index).append("%").toString(), "");
        }
        reply.replace("%objectId%", String.valueOf(getObjectId()));
        player.sendPacket(reply);
        break;
      case 3:
        if (player.isInEvent()) {
          player.sendHtmlMessage("\u0418\u0433\u0440\u043E\u043A\u0438, \u0443\u0447\u0430\u0432\u0441\u0442\u0432\u0443\u044E\u0449\u0438\u0435 \u0432 \u0438\u0432\u0435\u043D\u0442\u0430\u0445 \u043D\u0435 \u043C\u043E\u0433\u0443\u0442 \u043F\u0440\u043E\u0441\u043C\u0430\u0442\u0440\u0438\u0432\u0430\u0442\u044C.");
          player.sendActionFailed();
          return;
        }

        Olympiad.addSpectator(Integer.parseInt(command.substring(11)), player);
        break;
      default:
        _log.warning(new StringBuilder().append("Olympiad System: Couldnt send packet for request ").append(val).toString());
      }
    }
    else {
      super.onBypassFeedback(player, command);
    }
  }

  private void showChatWindow(L2PcInstance player, int val, String suffix) {
    String filename = "data/html/olympiad/";
    filename = new StringBuilder().append(filename).append("noble_desc").append(val).toString();
    filename = new StringBuilder().append(filename).append(suffix != null ? new StringBuilder().append(suffix).append(".htm").toString() : ".htm").toString();
    if (filename.equals("data/html/olympiad/noble_desc0.htm")) {
      filename = "data/html/olympiad/noble_main.htm";
    }

    showChatWindow(player, filename);
  }
}