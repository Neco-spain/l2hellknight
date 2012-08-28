package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;
import javolution.text.TextBuilder;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GmListTable;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.NpcWalkerRoutesTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.TeleportLocationTable;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.instancemanager.CustomNpcInstanceManager;
import net.sf.l2j.gameserver.instancemanager.Manager;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2Multisell;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.SayFilter;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

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
      activeChar.sendMessage("Registerd into gm list");
    }
    else if (command.startsWith("admin_gmlistoff"))
    {
      GmListTable.getInstance().hideGm(activeChar);
      activeChar.sendMessage("Removed from gm list");
    }
    else if (command.startsWith("admin_silence"))
    {
      if (activeChar.getMessageRefusal())
      {
        activeChar.setMessageRefusal(false);
        activeChar.sendPacket(new SystemMessage(SystemMessageId.MESSAGE_ACCEPTANCE_MODE));
      }
      else
      {
        activeChar.setMessageRefusal(true);
        activeChar.sendPacket(new SystemMessage(SystemMessageId.MESSAGE_REFUSAL_MODE));
      }
    }
    else if (command.startsWith("admin_saveolymp"))
    {
      try
      {
        Olympiad.getInstance().save();
      } catch (Exception e) {
        e.printStackTrace();
      }activeChar.sendMessage("olympiad stuff saved!!");
    }
    else if (command.startsWith("admin_manualhero"))
    {
      try
      {
        Olympiad.getInstance().manualSelectHeroes();
      } catch (Exception e) {
        e.printStackTrace();
      }activeChar.sendMessage("Heroes formed");
    }
    else if (command.startsWith("admin_diet"))
    {
      try
      {
        StringTokenizer st = new StringTokenizer(command);
        st.nextToken();
        if (st.nextToken().equalsIgnoreCase("on"))
        {
          activeChar.setDietMode(true);
          activeChar.sendMessage("Diet mode on");
        }
        else if (st.nextToken().equalsIgnoreCase("off"))
        {
          activeChar.setDietMode(false);
          activeChar.sendMessage("Diet mode off");
        }
      }
      catch (Exception ex)
      {
        if (activeChar.getDietMode())
        {
          activeChar.setDietMode(false);
          activeChar.sendMessage("Diet mode off");
        }
        else
        {
          activeChar.setDietMode(true);
          activeChar.sendMessage("Diet mode on");
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
          activeChar.sendMessage("Trade refusal enabled");
        }
        else if (mode.equalsIgnoreCase("off"))
        {
          activeChar.setTradeRefusal(false);
          activeChar.sendMessage("Trade refusal disabled");
        }
      }
      catch (Exception ex)
      {
        if (activeChar.getTradeRefusal())
        {
          activeChar.setTradeRefusal(false);
          activeChar.sendMessage("Trade refusal disabled");
        }
        else
        {
          activeChar.setTradeRefusal(true);
          activeChar.sendMessage("Trade refusal enabled");
        }
      }
    }
    else if (command.startsWith("admin_reload"))
    {
      sendReloadPage(activeChar);
      StringTokenizer st = new StringTokenizer(command);
      st.nextToken();
      try
      {
        String type = st.nextToken();
        if (type.equals("multisell"))
        {
          L2Multisell.getInstance().reload();
          sendReloadPage(activeChar);
          activeChar.sendMessage("multisell reloaded");
        }
        else if (type.startsWith("npcpolymorph"))
        {
          CustomNpcInstanceManager.getInstance().reload();
          sendReloadPage(activeChar);
          activeChar.sendMessage("npc_polymorph reloaded");
        }
        else if (type.startsWith("teleport"))
        {
          TeleportLocationTable.getInstance().reloadAll();
          sendReloadPage(activeChar);
          activeChar.sendMessage("teleport location table reloaded");
        }
        else if (type.startsWith("skill"))
        {
          SkillTable.getInstance().reload();
          sendReloadPage(activeChar);
          activeChar.sendMessage("skills reloaded");
        }
        else if (type.equals("npc"))
        {
          NpcTable.getInstance().reloadAllNpc();
          sendReloadPage(activeChar);
          activeChar.sendMessage("npcs reloaded");
        }
        else if (type.startsWith("htm"))
        {
          HtmCache.getInstance().reload();
          sendReloadPage(activeChar);
          activeChar.sendMessage("Cache[HTML]: " + HtmCache.getInstance().getMemoryUsage() + " megabytes on " + HtmCache.getInstance().getLoadedFiles() + " files loaded");
        }
        else if (type.startsWith("item"))
        {
          ItemTable.getInstance().reload();
          sendReloadPage(activeChar);
          activeChar.sendMessage("Item templates reloaded");
        }
        else if (type.startsWith("instancemanager"))
        {
          Manager.reloadAll();
          sendReloadPage(activeChar);
          activeChar.sendMessage("All instance manager has been reloaded");
        }
        else if (type.startsWith("npcwalkers"))
        {
          NpcWalkerRoutesTable.getInstance().load();
          sendReloadPage(activeChar);
          activeChar.sendMessage("All NPC walker routes have been reloaded");
        }
        else if (type.equals("config"))
        {
          Config.load();
          SayFilter.getInstance().loadSayFilter();
          SayFilter.getInstance().loadSayFilterExceptions();
          sendReloadPage(activeChar);
          activeChar.sendMessage("Server Config Reloaded.");
        }

      }
      catch (Exception e)
      {
        activeChar.sendMessage("Usage:  //reload <type>");
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
          activeChar.sendMessage("parameter " + pName + " succesfully set to " + pValue);
        else {
          activeChar.sendMessage("Invalid parameter!");
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
          activeChar.sendMessage("Usage: //set parameter=vaue");
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
    return true;
  }

  private void sendReloadPage(L2PcInstance player)
  {
    NpcHtmlMessage menu = new NpcHtmlMessage(5);
    TextBuilder replyMSG = new TextBuilder("<html><center><body>");
    replyMSG.append("<img src=L2Font-e.replay_logo-e width=258 height=60><br>");
    replyMSG.append("<img src=L2UI_CH3.herotower_deco width=256 height=32>");
    replyMSG.append("<font color=AAAAAA>Reload Menu<br>");
    replyMSG.append("<a action=\"bypass -h admin_reload npcpolymorph\">Reload Npc_Polymorph</a><br1>");
    replyMSG.append("<a action=\"bypass -h admin_reload multisell\">Reload Multisell</a><br1>");
    replyMSG.append("<a action=\"bypass -h admin_reload teleport\">Reload Teleports</a><br1>");
    replyMSG.append("<a action=\"bypass -h admin_reload skill\">Reload Skills</a><br1>");
    replyMSG.append("<a action=\"bypass -h admin_reload npc\">Reload Npcs</a><br1>");
    replyMSG.append("<a action=\"bypass -h admin_reload htm\">Reload Html</a><br1>");
    replyMSG.append("<a action=\"bypass -h admin_reload item\">Reload Item Tabels</a><br1>");
    replyMSG.append("<a action=\"bypass -h admin_reload instancemanager\">Reload Instance Managers</a><br1>");
    replyMSG.append("<a action=\"bypass -h admin_reload npcwalkers\">Reload Npc Walkers</a><br1>");
    replyMSG.append("<a action=\"bypass -h admin_reload config\">Reload Configs</a><br1>");
    replyMSG.append("</font></body></center></html>");
    menu.setHtml(replyMSG.toString());
    player.sendPacket(menu);
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