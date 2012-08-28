package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.L2Event;
import net.sf.l2j.gameserver.model.entity.olympiad.OlympiadDiary;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import scripts.commands.AdminCommandHandler;
import scripts.commands.IAdminCommandHandler;
import scripts.commands.IVoicedCommandHandler;
import scripts.commands.VoicedCommandHandler;
import scripts.communitybbs.CommunityBoard;

public final class RequestBypassToServer extends L2GameClientPacket
{
  private static final Logger _log = Logger.getLogger(RequestBypassToServer.class.getName());
  private String _command;

  protected void readImpl()
  {
    _command = readS();
    if (_command != null)
      _command = _command.trim();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }

    if (System.currentTimeMillis() - player.getCPA() < 100L) {
      return;
    }
    player.setCPA();

    if (player.getActiveTradeList() != null) {
      player.cancelActiveTrade();
    }
    try
    {
      if (_command.startsWith("npc_")) {
        if ((!player.validateBypass(_command)) || (player.isParalyzed())) {
          return;
        }

        int endOfId = _command.indexOf('_', 5);
        String id;
        String id;
        if (endOfId > 0)
          id = _command.substring(4, endOfId);
        else
          id = _command.substring(4);
        try
        {
          L2Object object = L2World.getInstance().findObject(Integer.parseInt(id));

          if (_command.substring(endOfId + 1).startsWith("event_participate"))
            L2Event.inscribePlayer(player);
          else if ((object != null) && (object.isL2Npc()) && (endOfId > 0) && (player.isInsideRadius(object, 150, false, false))) {
            ((L2NpcInstance)object).onBypassFeedback(player, _command.substring(endOfId + 1));
          }
          player.sendActionFailed();
        } catch (NumberFormatException nfe) {
        }
      } else if (_command.startsWith("Quest ")) {
        if ((!player.validateBypass(_command)) || (player.isParalyzed())) {
          return;
        }

        String p = _command.substring(6).trim();
        int idx = p.indexOf(' ');
        if (idx < 0)
          player.processQuestEvent(p, "");
        else {
          player.processQuestEvent(p.substring(0, idx), p.substring(idx).trim());
        }
      }
      else if (_command.equals("menu_select?ask=-16&reply=1")) {
        L2Object object = player.getTarget();
        if (object.isL2Npc())
          ((L2NpcInstance)object).onBypassFeedback(player, _command);
      }
      else if (_command.equals("menu_select?ask=-16&reply=2")) {
        L2Object object = player.getTarget();
        if (object.isL2Npc())
          ((L2NpcInstance)object).onBypassFeedback(player, _command);
      }
      else if (_command.startsWith("menu_")) {
        IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler("menu_");
        if (vch != null)
          vch.useVoicedCommand(_command, player, null);
      }
      else if (_command.startsWith("security_")) {
        IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler("security_");
        if (vch != null)
          vch.useVoicedCommand(_command, player, null);
      }
      else if ((_command.startsWith("admin_")) && (player.getAccessLevel() >= 1))
      {
        if (player.isParalyzed()) {
          return;
        }

        String command = _command.split(" ")[0];
        IAdminCommandHandler ach = AdminCommandHandler.getInstance().getAdminCommandHandler(command);

        if (ach != null)
          ach.useAdminCommand(_command, player);
        else {
          _log.warning("No handler registered for bypass '" + _command + "'");
        }
      }
      else if (_command.startsWith("manor_menu_select?")) {
        L2Object object = player.getTarget();
        if (object.isL2Npc())
          ((L2NpcInstance)object).onBypassFeedback(player, _command);
      }
      else if (_command.startsWith("bbs_")) {
        CommunityBoard.getInstance().handleCommands((L2GameClient)getClient(), _command);
      } else if (_command.startsWith("_bbs")) {
        CommunityBoard.getInstance().handleCommands((L2GameClient)getClient(), _command);
      } else if ((_command.equals("come_here")) && (player.getAccessLevel() >= Config.GM_ACCESSLEVEL)) {
        comeHere(player);
      } else if (_command.startsWith("player_help ")) {
        playerHelp(player, _command.substring(12));
      } else if (_command.startsWith("olympiad_observ_")) {
        if (!player.inObserverMode()) {
          return;
        }

      }
      else if (_command.startsWith("ench_click"))
      {
        int pwd = 0;
        try {
          pwd = Integer.parseInt(_command.substring(10).trim());
        }
        catch (Exception ignored) {
        }
        if (player.getEnchClicks() == pwd)
          player.showAntiClickOk();
        else {
          player.showAntiClickPWD();
        }

      }
      else if (_command.startsWith("_diary?class="))
      {
        OlympiadDiary.show(player, _command.substring(13));
      }
    } catch (Exception e) {
      _log.log(Level.WARNING, "Bad RequestBypassToServer: ", e);
    }
  }

  private void comeHere(L2PcInstance player)
  {
    L2Object obj = player.getTarget();
    if (obj == null) {
      return;
    }
    if (obj.isL2Npc()) {
      L2NpcInstance temp = (L2NpcInstance)obj;
      temp.setTarget(player);
      temp.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(player.getX(), player.getY(), player.getZ(), 0));
    }
  }

  private void playerHelp(L2PcInstance player, String path)
  {
    if (path.indexOf("..") != -1) {
      return;
    }

    String filename = "data/html/help/" + path;
    NpcHtmlMessage html = NpcHtmlMessage.id(1);
    html.setFile(filename);
    player.sendPacket(html);
  }
}