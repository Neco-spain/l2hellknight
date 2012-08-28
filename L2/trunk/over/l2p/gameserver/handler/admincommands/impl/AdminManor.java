package l2p.gameserver.handler.admincommands.impl;

import java.util.ArrayList;
import java.util.StringTokenizer;
import l2p.gameserver.data.xml.holder.ResidenceHolder;
import l2p.gameserver.handler.admincommands.IAdminCommandHandler;
import l2p.gameserver.instancemanager.CastleManorManager;
import l2p.gameserver.instancemanager.ServerVariables;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.PlayerAccess;
import l2p.gameserver.model.entity.residence.Castle;
import l2p.gameserver.serverpackets.NpcHtmlMessage;

public class AdminManor
  implements IAdminCommandHandler
{
  public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    if (!activeChar.getPlayerAccess().Menu) {
      return false;
    }
    StringTokenizer st = new StringTokenizer(fullString);
    fullString = st.nextToken();

    if (fullString.equals("admin_manor")) {
      showMainPage(activeChar);
    } else if (fullString.equals("admin_manor_reset"))
    {
      int castleId = 0;
      try
      {
        castleId = Integer.parseInt(st.nextToken());
      }
      catch (Exception e)
      {
      }
      if (castleId > 0)
      {
        Castle castle = (Castle)ResidenceHolder.getInstance().getResidence(Castle.class, castleId);
        castle.setCropProcure(new ArrayList(), 0);
        castle.setCropProcure(new ArrayList(), 1);
        castle.setSeedProduction(new ArrayList(), 0);
        castle.setSeedProduction(new ArrayList(), 1);
        castle.saveCropData();
        castle.saveSeedData();
        activeChar.sendMessage(new StringBuilder().append("Manor data for ").append(castle.getName()).append(" was nulled").toString());
      }
      else
      {
        for (Castle castle : ResidenceHolder.getInstance().getResidenceList(Castle.class))
        {
          castle.setCropProcure(new ArrayList(), 0);
          castle.setCropProcure(new ArrayList(), 1);
          castle.setSeedProduction(new ArrayList(), 0);
          castle.setSeedProduction(new ArrayList(), 1);
          castle.saveCropData();
          castle.saveSeedData();
        }
        activeChar.sendMessage("Manor data was nulled");
      }
      showMainPage(activeChar);
    }
    else if (fullString.equals("admin_manor_save"))
    {
      CastleManorManager.getInstance().save();
      activeChar.sendMessage("Manor System: all data saved");
      showMainPage(activeChar);
    }
    else if (fullString.equals("admin_manor_disable"))
    {
      boolean mode = CastleManorManager.getInstance().isDisabled();
      CastleManorManager.getInstance().setDisabled(!mode);
      if (mode)
        activeChar.sendMessage("Manor System: enabled");
      else
        activeChar.sendMessage("Manor System: disabled");
      showMainPage(activeChar);
    }

    return true;
  }

  public Enum[] getAdminCommandEnum()
  {
    return Commands.values();
  }

  private void showMainPage(Player activeChar)
  {
    NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
    StringBuilder replyMSG = new StringBuilder("<html><body>");

    replyMSG.append("<center><font color=\"LEVEL\"> [Manor System] </font></center><br>");
    replyMSG.append("<table width=\"100%\">");
    replyMSG.append(new StringBuilder().append("<tr><td>Disabled: ").append(CastleManorManager.getInstance().isDisabled() ? "yes" : "no").append("</td>").toString());
    replyMSG.append(new StringBuilder().append("<td>Under Maintenance: ").append(CastleManorManager.getInstance().isUnderMaintenance() ? "yes" : "no").append("</td></tr>").toString());
    replyMSG.append(new StringBuilder().append("<tr><td>Approved: ").append(ServerVariables.getBool("ManorApproved") ? "yes" : "no").append("</td></tr>").toString());
    replyMSG.append("</table>");

    replyMSG.append("<center><table>");
    replyMSG.append(new StringBuilder().append("<tr><td><button value=\"").append(CastleManorManager.getInstance().isDisabled() ? "Enable" : "Disable").append("\" action=\"bypass -h admin_manor_disable\" width=110 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>").toString());
    replyMSG.append("<td><button value=\"Reset\" action=\"bypass -h admin_manor_reset\" width=110 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
    replyMSG.append("<tr><td><button value=\"Refresh\" action=\"bypass -h admin_manor\" width=110 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
    replyMSG.append("<td><button value=\"Back\" action=\"bypass -h admin_admin\" width=110 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
    replyMSG.append("</table></center>");

    replyMSG.append("<br><center>Castle Information:<table width=\"100%\">");
    replyMSG.append("<tr><td></td><td>Current Period</td><td>Next Period</td></tr>");

    for (Castle c : ResidenceHolder.getInstance().getResidenceList(Castle.class))
    {
      replyMSG.append(new StringBuilder().append("<tr><td>").append(c.getName()).append("</td>").append("<td>").append(c.getManorCost(0)).append("a</td>").append("<td>").append(c.getManorCost(1)).append("a</td>").append("</tr>").toString());
    }
    replyMSG.append("</table><br>");

    replyMSG.append("</body></html>");

    adminReply.setHtml(replyMSG.toString());
    activeChar.sendPacket(adminReply);
  }

  private static enum Commands
  {
    admin_manor, 
    admin_manor_reset, 
    admin_manor_save, 
    admin_manor_disable;
  }
}