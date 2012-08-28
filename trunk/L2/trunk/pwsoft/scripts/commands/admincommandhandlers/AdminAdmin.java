package scripts.commands.admincommandhandlers;

import java.util.StringTokenizer;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GmListTable;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.NpcWalkerRoutesTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.TeleportLocationTable;
import net.sf.l2j.gameserver.instancemanager.Manager;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2Multisell;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import scripts.commands.IAdminCommandHandler;

public class AdminAdmin
  implements IAdminCommandHandler
{
  private static final String[] ADMIN_COMMANDS = { "admin_admin", "admin_admin1", "admin_admin2", "admin_admin3", "admin_admin4", "admin_admin5", "admin_gmliston", "admin_gmlistoff", "admin_silence", "admin_diet", "admin_tradeoff", "admin_reload", "admin_set", "admin_set_menu", "admin_set_mod", "admin_saveolymp", "admin_manualhero" };

  private static final int REQUIRED_LEVEL = Config.GM_MENU;

  public boolean useAdminCommand(String command, L2PcInstance activeChar)
  {
    if ((!Config.ALT_PRIVILEGES_ADMIN) && (
      (!checkLevel(activeChar.getAccessLevel())) || (!activeChar.isGM()))) {
      return false;
    }
    GMAudit.auditGMAction(activeChar.getName(), command, activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target", "");

    if (command.startsWith("admin_admin"))
    {
      showMainPage(activeChar, command);
    }
    else if (command.startsWith("admin_gmliston"))
    {
      GmListTable.getInstance().showGm(activeChar);
      activeChar.sendAdmResultMessage("Registerd into gm list");
    }
    else if (command.startsWith("admin_gmlistoff"))
    {
      GmListTable.getInstance().hideGm(activeChar);
      activeChar.sendAdmResultMessage("Removed from gm list");
    }
    else if (command.startsWith("admin_silence"))
    {
      if (activeChar.getMessageRefusal())
      {
        activeChar.setMessageRefusal(false);
        activeChar.sendAdmResultMessage("PM refusal disabled");
      }
      else
      {
        activeChar.setMessageRefusal(true);
        activeChar.sendAdmResultMessage("PM refusal enabled");
      }
    }
    else if (command.startsWith("admin_saveolymp"))
    {
      activeChar.sendAdmResultMessage("Olympiad game saved");
    }
    else if (!command.startsWith("admin_manualhero"))
    {
      if (command.startsWith("admin_diet"))
      {
        try
        {
          StringTokenizer st = new StringTokenizer(command);
          st.nextToken();
          if (st.nextToken().equalsIgnoreCase("on"))
          {
            activeChar.setDietMode(true);
            activeChar.sendAdmResultMessage("Diet mode on");
          }
          else if (st.nextToken().equalsIgnoreCase("off"))
          {
            activeChar.setDietMode(false);
            activeChar.sendAdmResultMessage("Diet mode off");
          }
        }
        catch (Exception ex)
        {
          if (activeChar.getDietMode())
          {
            activeChar.setDietMode(false);
            activeChar.sendAdmResultMessage("Diet mode off");
          }
          else
          {
            activeChar.setDietMode(true);
            activeChar.sendAdmResultMessage("Diet mode on");
          }
        }
        finally
        {
          activeChar.refreshOverloaded();
        }
      }
      else if (command.startsWith("admin_tradeoff"))
      {
        try
        {
          String mode = command.substring(15);
          if (mode.equalsIgnoreCase("on"))
          {
            activeChar.setTradeRefusal(true);
            activeChar.sendAdmResultMessage("Trade refusal enabled");
          }
          else if (mode.equalsIgnoreCase("off"))
          {
            activeChar.setTradeRefusal(false);
            activeChar.sendAdmResultMessage("Trade refusal disabled");
          }
        }
        catch (Exception ex)
        {
          if (activeChar.getTradeRefusal())
          {
            activeChar.setTradeRefusal(false);
            activeChar.sendAdmResultMessage("Trade refusal disabled");
          }
          else
          {
            activeChar.setTradeRefusal(true);
            activeChar.sendAdmResultMessage("Trade refusal enabled");
          }
        }
      }
      else if (command.startsWith("admin_reload"))
      {
        StringTokenizer st = new StringTokenizer(command);
        st.nextToken();
        try
        {
          String type = st.nextToken();
          if (type.equals("multisell"))
          {
            L2Multisell.getInstance().reload();
            activeChar.sendAdmResultMessage("Multisell reloaded");
          }
          else if (type.startsWith("teleport"))
          {
            TeleportLocationTable.getInstance().reloadAll();
            activeChar.sendAdmResultMessage("teleport location table reloaded");
          }
          else if (type.startsWith("skill"))
          {
            SkillTable.getInstance().reload();
            activeChar.sendAdmResultMessage("skills reloaded");
          }
          else if (type.equals("npc"))
          {
            NpcTable.getInstance().reloadAllNpc();
            activeChar.sendAdmResultMessage("npcs reloaded");
          }
          else if (type.startsWith("htm"))
          {
            HtmCache.getInstance().reload();
            activeChar.sendAdmResultMessage("Cache[HTML]: " + HtmCache.getInstance().getMemoryUsage() + " megabytes on " + HtmCache.getInstance().getLoadedFiles() + " files loaded");
          }
          else if (type.startsWith("item"))
          {
            ItemTable.getInstance().reload();
            activeChar.sendAdmResultMessage("Item templates reloaded");
          }
          else if (type.startsWith("instancemanager"))
          {
            Manager.reloadAll();
            activeChar.sendAdmResultMessage("All instance manager has been reloaded");
          }
          else if (type.startsWith("npcwalkers"))
          {
            NpcWalkerRoutesTable.getInstance().load();
            activeChar.sendAdmResultMessage("All NPC walker routes have been reloaded");
          }

        }
        catch (Exception e)
        {
          activeChar.sendAdmResultMessage("Usage:  //reload <multisell|skill|npc|htm|item|instancemanager>");
        }

      }
      else if (command.startsWith("admin_set"))
      {
        StringTokenizer st = new StringTokenizer(command);
        String[] cmd = st.nextToken().split("_");
        try
        {
          String[] parameter = st.nextToken().split("=");
          String pName = parameter[0].trim();
          String pValue = parameter[1].trim();
          if (Config.setParameterValue(pName, pValue))
            activeChar.sendAdmResultMessage("parameter " + pName + " succesfully set to " + pValue);
          else {
            activeChar.sendAdmResultMessage("Invalid parameter!");
          }

          if (cmd.length == 3)
          {
            if (cmd[2].equalsIgnoreCase("menu"))
              AdminHelpPage.showHelpPage(activeChar, "settings.htm");
            else if (cmd[2].equalsIgnoreCase("mod"))
              AdminHelpPage.showHelpPage(activeChar, "mods_menu.htm");
          }
        }
        catch (Exception e)
        {
          if (cmd.length == 2) {
            activeChar.sendAdmResultMessage("Usage: //set parameter=vaue");
          }

          if (cmd.length == 3)
          {
            if (cmd[2].equalsIgnoreCase("menu"))
              AdminHelpPage.showHelpPage(activeChar, "settings.htm");
            else if (cmd[2].equalsIgnoreCase("mod"))
              AdminHelpPage.showHelpPage(activeChar, "mods_menu.htm");
          }
        }
        finally
        {
          if (cmd.length == 3)
          {
            if (cmd[2].equalsIgnoreCase("menu"))
              AdminHelpPage.showHelpPage(activeChar, "settings.htm");
            else if (cmd[2].equalsIgnoreCase("mod"))
              AdminHelpPage.showHelpPage(activeChar, "mods_menu.htm"); 
          }
        }
      }
    }
    return true;
  }

  public String[] getAdminCommandList()
  {
    return ADMIN_COMMANDS;
  }

  private boolean checkLevel(int level)
  {
    return level >= REQUIRED_LEVEL;
  }

  private void showMainPage(L2PcInstance activeChar, String command)
  {
    int mode = 0;
    String filename = null;
    try
    {
      mode = Integer.parseInt(command.substring(11));
    } catch (Exception e) {
    }
    switch (mode)
    {
    case 1:
      filename = "main";
      break;
    case 2:
      filename = "game";
      break;
    case 3:
      filename = "effects";
      break;
    case 4:
      filename = "server";
      break;
    case 5:
      filename = "mods";
      break;
    default:
      if (Config.GM_ADMIN_MENU_STYLE.equals("modern"))
        filename = "main";
      else {
        filename = "classic";
      }
    }
    AdminHelpPage.showHelpPage(activeChar, filename + "_menu.htm");
  }
}