package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.communitybbs.CommunityBoard;
import net.sf.l2j.gameserver.handler.AdminCommandHandler;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.handler.VoicedCommandHandler;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.L2Event;
import net.sf.l2j.gameserver.model.entity.events.CTF;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public final class RequestBypassToServer extends L2GameClientPacket
{
  private static final String _C__21_REQUESTBYPASSTOSERVER = "[C] 21 RequestBypassToServer";
  private static Logger _log = Logger.getLogger(RequestBypassToServer.class.getName());
  private String _command;

  protected void readImpl()
  {
    _command = readS();
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();

    if (activeChar == null)
      return;
    try
    {
      if (_command.startsWith("admin_"))
      {
        if ((Config.ALT_PRIVILEGES_ADMIN) && (!AdminCommandHandler.getInstance().checkPrivileges(activeChar, _command)))
        {
          _log.info("<GM>" + activeChar + " does not have sufficient privileges for command '" + _command + "'.");
          return;
        }

        IAdminCommandHandler ach = AdminCommandHandler.getInstance().getAdminCommandHandler(_command);

        if (ach != null)
          ach.useAdminCommand(_command, activeChar);
        else
          _log.warning("No handler registered for bypass '" + _command + "'");
      }
      else if ((_command.equals("come_here")) && (activeChar.getAccessLevel() >= Config.GM_ACCESSLEVEL))
      {
        comeHere(activeChar);
      }
      else if (_command.startsWith("player_help "))
      {
        playerHelp(activeChar, _command.substring(12));
      }
      else if (_command.startsWith("npc_"))
      {
        if (!activeChar.validateBypass(_command)) {
          return;
        }
        if ((activeChar.isSitting()) || (activeChar.isFakeDeath()) || (activeChar.getPrivateStoreType() != 0) || (activeChar.getActiveTradeList() != null) || (activeChar.getActiveEnchantItem() != null) || (activeChar.isTeleporting()))
        {
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

          if (_command.substring(endOfId + 1).startsWith("event_participate")) {
            L2Event.inscribePlayer(activeChar);
          } else if (_command.substring(endOfId + 1).startsWith("ctf_player_join "))
          {
            String teamName = _command.substring(endOfId + 1).substring(16);

            if (CTF._joining)
              CTF.addPlayer(activeChar, teamName);
            else
              activeChar.sendMessage("The event is already started. You can not join now!");
          }
          else if (_command.substring(endOfId + 1).startsWith("ctf_player_leave"))
          {
            if (CTF._joining)
              CTF.removePlayer(activeChar);
            else
              activeChar.sendMessage("The event is already started. You can not leave now!");
          }
          else if ((object != null) && ((object instanceof L2NpcInstance)) && (endOfId > 0) && (activeChar.isInsideRadius(object, 150, false, false)))
          {
            ((L2NpcInstance)object).onBypassFeedback(activeChar, _command.substring(endOfId + 1));
          }
          activeChar.sendPacket(new ActionFailed());
        }
        catch (NumberFormatException nfe) {
        }
      }
      else if (_command.equals("menu_select?ask=-16&reply=1"))
      {
        L2Object object = activeChar.getTarget();
        if ((object instanceof L2NpcInstance))
        {
          ((L2NpcInstance)object).onBypassFeedback(activeChar, _command);
        }
      }
      else if (_command.equals("menu_select?ask=-16&reply=2"))
      {
        L2Object object = activeChar.getTarget();
        if ((object instanceof L2NpcInstance))
        {
          ((L2NpcInstance)object).onBypassFeedback(activeChar, _command);
        }

      }
      else if (_command.startsWith("manor_menu_select?"))
      {
        L2Object object = activeChar.getTarget();
        if ((object instanceof L2NpcInstance))
        {
          ((L2NpcInstance)object).onBypassFeedback(activeChar, _command);
        }
      }
      else if (_command.startsWith("bbs_"))
      {
        CommunityBoard.getInstance().handleCommands((L2GameClient)getClient(), _command);
      }
      else if (_command.startsWith("_bbs"))
      {
        CommunityBoard.getInstance().handleCommands((L2GameClient)getClient(), _command);
      }
      else if (_command.startsWith("arenawtt"))
      {
        Olympiad.bypassChangeArena(_command, activeChar);
      }
      else if (_command.startsWith("eon_menu_"))
      {
        IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler("eon_menu_");
        vch.useVoicedCommand(_command, activeChar, null);
      }
      else if (_command.startsWith("moder"))
      {
        IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler("moder");
        vch.useVoicedCommand(_command, activeChar, null);
      }
      else if (_command.startsWith("Quest "))
      {
        if (!activeChar.validateBypass(_command)) {
          return;
        }
        L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
        if (player == null) return;

        String p = _command.substring(6).trim();
        int idx = p.indexOf(' ');
        if (idx < 0)
          player.processQuestEvent(p, "");
        else
          player.processQuestEvent(p.substring(0, idx), p.substring(idx).trim());
      }
    }
    catch (Exception e)
    {
      _log.log(Level.WARNING, "Bad RequestBypassToServer: ", e);
    }
  }

  private void comeHere(L2PcInstance activeChar)
  {
    L2Object obj = activeChar.getTarget();
    if (obj == null) return;
    if ((obj instanceof L2NpcInstance))
    {
      L2NpcInstance temp = (L2NpcInstance)obj;
      temp.setTarget(activeChar);
      temp.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(activeChar.getX(), activeChar.getY(), activeChar.getZ(), 0));
    }
  }

  private void playerHelp(L2PcInstance activeChar, String path)
  {
    if (path.indexOf("..") != -1) {
      return;
    }
    String filename = "data/html/help/" + path;
    NpcHtmlMessage html = new NpcHtmlMessage(1);
    html.setFile(filename);
    activeChar.sendPacket(html);
  }

  public String getType()
  {
    return "[C] 21 RequestBypassToServer";
  }
}