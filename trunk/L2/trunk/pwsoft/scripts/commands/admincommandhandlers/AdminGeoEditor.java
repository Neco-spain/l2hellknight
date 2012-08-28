package scripts.commands.admincommandhandlers;

import java.util.StringTokenizer;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.geoeditorcon.GeoEditorListener;
import net.sf.l2j.gameserver.geoeditorcon.GeoEditorThread;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import scripts.commands.IAdminCommandHandler;

public class AdminGeoEditor
  implements IAdminCommandHandler
{
  private static final String[] ADMIN_COMMANDS = { "admin_ge_status", "admin_ge_mode", "admin_ge_join", "admin_ge_leave" };

  private static final int REQUIRED_LEVEL = Config.GM_MIN;

  public boolean useAdminCommand(String command, L2PcInstance activeChar)
  {
    if ((!Config.ALT_PRIVILEGES_ADMIN) && (
      (!checkLevel(activeChar.getAccessLevel())) || (!activeChar.isGM()))) {
      return false;
    }
    String target = activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target";
    GMAudit.auditGMAction(activeChar.getName(), command, target, "");

    if (!Config.ACCEPT_GEOEDITOR_CONN)
    {
      activeChar.sendAdmResultMessage("Server do not accepts geoeditor connections now.");
      return true;
    }
    if (command.startsWith("admin_ge_status"))
    {
      activeChar.sendMessage(GeoEditorListener.getInstance().getStatus());
    } else {
      if (command.startsWith("admin_ge_mode"))
      {
        if (GeoEditorListener.getInstance().getThread() == null)
        {
          activeChar.sendAdmResultMessage("Geoeditor not connected.");
          return true;
        }
        try
        {
          String val = command.substring("admin_ge_mode".length());
          StringTokenizer st = new StringTokenizer(val);

          if (st.countTokens() < 1)
          {
            activeChar.sendAdmResultMessage("Usage: //ge_mode X");
            activeChar.sendAdmResultMessage("Mode 0: Don't send coordinates to geoeditor.");
            activeChar.sendAdmResultMessage("Mode 1: Send coordinates at ValidatePosition from clients.");
            activeChar.sendAdmResultMessage("Mode 2: Send coordinates each second.");
            return true;
          }

          int m = Integer.parseInt(st.nextToken());
          GeoEditorListener.getInstance().getThread().setMode(m);
          activeChar.sendAdmResultMessage("Geoeditor connection mode set to " + m + ".");
        }
        catch (Exception e) {
          activeChar.sendAdmResultMessage("Usage: //ge_mode X");
          activeChar.sendAdmResultMessage("Mode 0: Don't send coordinates to geoeditor.");
          activeChar.sendAdmResultMessage("Mode 1: Send coordinates at ValidatePosition from clients.");
          activeChar.sendAdmResultMessage("Mode 2: Send coordinates each second.");
          e.printStackTrace();
        }
        return true;
      }
      if (command.equals("admin_ge_join"))
      {
        if (GeoEditorListener.getInstance().getThread() == null)
        {
          activeChar.sendAdmResultMessage("Geoeditor not connected.");
          return true;
        }
        GeoEditorListener.getInstance().getThread().addGM(activeChar);
        activeChar.sendAdmResultMessage("You added to list for geoeditor.");
      }
      else if (command.equals("admin_ge_leave"))
      {
        if (GeoEditorListener.getInstance().getThread() == null)
        {
          activeChar.sendAdmResultMessage("Geoeditor not connected.");
          return true;
        }
        GeoEditorListener.getInstance().getThread().removeGM(activeChar);
        activeChar.sendAdmResultMessage("You removed from list for geoeditor.");
      }
    }
    return true;
  }

  public String[] getAdminCommandList() {
    return ADMIN_COMMANDS;
  }

  private boolean checkLevel(int level)
  {
    return level >= REQUIRED_LEVEL;
  }
}