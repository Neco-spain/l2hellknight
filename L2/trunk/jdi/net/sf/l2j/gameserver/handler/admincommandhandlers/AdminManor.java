package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.CastleManorManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class AdminManor
  implements IAdminCommandHandler
{
  private static final String[] _adminCommands = { "admin_manor", "admin_manor_approve", "admin_manor_setnext", "admin_manor_reset", "admin_manor_setmaintenance", "admin_manor_save", "admin_manor_disable" };

  private static final int REQUIRED_LEVEL = Config.GM_MENU;

  public boolean useAdminCommand(String command, L2PcInstance activeChar) {
    if ((!Config.ALT_PRIVILEGES_ADMIN) && (
      (!checkLevel(activeChar.getAccessLevel())) || (!activeChar.isGM()))) {
      return false;
    }

    StringTokenizer st = new StringTokenizer(command);
    command = st.nextToken();

    if (command.equals("admin_manor")) {
      showMainPage(activeChar);
    } else if (command.equals("admin_manor_setnext")) {
      CastleManorManager.getInstance().setNextPeriod();
      CastleManorManager.getInstance().setNewManorRefresh();
      CastleManorManager.getInstance().updateManorRefresh();
      activeChar.sendMessage("Manor System: set to next period");
      showMainPage(activeChar);
    } else if (command.equals("admin_manor_approve")) {
      CastleManorManager.getInstance().approveNextPeriod();
      CastleManorManager.getInstance().setNewPeriodApprove();
      CastleManorManager.getInstance().updatePeriodApprove();
      activeChar.sendMessage("Manor System: next period approved");
      showMainPage(activeChar);
    } else if (command.equals("admin_manor_reset")) {
      int castleId = 0;
      try {
        castleId = Integer.parseInt(st.nextToken());
      } catch (Exception e) {
      }
      if (castleId > 0) {
        Castle castle = CastleManager.getInstance().getCastleById(castleId);
        castle.setCropProcure(new FastList(), 0);
        castle.setCropProcure(new FastList(), 1);
        castle.setSeedProduction(new FastList(), 0);
        castle.setSeedProduction(new FastList(), 1);
        if (Config.ALT_MANOR_SAVE_ALL_ACTIONS) {
          castle.saveCropData();
          castle.saveSeedData();
        }
        activeChar.sendMessage(new StringBuilder().append("Manor data for ").append(castle.getName()).append(" was nulled").toString());
      } else {
        for (Castle castle : CastleManager.getInstance().getCastles()) {
          castle.setCropProcure(new FastList(), 0);
          castle.setCropProcure(new FastList(), 1);
          castle.setSeedProduction(new FastList(), 0);
          castle.setSeedProduction(new FastList(), 1);
          if (Config.ALT_MANOR_SAVE_ALL_ACTIONS) {
            castle.saveCropData();
            castle.saveSeedData();
          }
        }
        activeChar.sendMessage("Manor data was nulled");
      }
      showMainPage(activeChar);
    } else if (command.equals("admin_manor_setmaintenance")) {
      boolean mode = CastleManorManager.getInstance().isUnderMaintenance();
      CastleManorManager.getInstance().setUnderMaintenance(!mode);
      if (mode)
        activeChar.sendMessage("Manor System: not under maintenance");
      else
        activeChar.sendMessage("Manor System: under maintenance");
      showMainPage(activeChar);
    } else if (command.equals("admin_manor_save")) {
      CastleManorManager.getInstance().save();
      activeChar.sendMessage("Manor System: all data saved");
      showMainPage(activeChar);
    } else if (command.equals("admin_manor_disable")) {
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

  public String[] getAdminCommandList() {
    return _adminCommands;
  }

  private String formatTime(long millis) {
    String s = "";
    int secs = (int)millis / 1000;
    int mins = secs / 60;
    secs -= mins * 60;
    int hours = mins / 60;
    mins -= hours * 60;

    if (hours > 0)
      s = new StringBuilder().append(s).append(hours).append(":").toString();
    s = new StringBuilder().append(s).append(mins).append(":").toString();
    s = new StringBuilder().append(s).append(secs).toString();
    return s;
  }

  private void showMainPage(L2PcInstance activeChar) {
    NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
    TextBuilder replyMSG = new TextBuilder("<html><body>");

    replyMSG.append("<center><font color=\"LEVEL\"> [Manor System] </font></center><br>");
    replyMSG.append("<table width=\"100%\"><tr><td>");
    replyMSG.append(new StringBuilder().append("Disabled: ").append(CastleManorManager.getInstance().isDisabled() ? "yes" : "no").append("</td><td>").toString());
    replyMSG.append(new StringBuilder().append("Under Maintenance: ").append(CastleManorManager.getInstance().isUnderMaintenance() ? "yes" : "no").append("</td></tr><tr><td>").toString());
    replyMSG.append(new StringBuilder().append("Time to refresh: ").append(formatTime(CastleManorManager.getInstance().getMillisToManorRefresh())).append("</td><td>").toString());
    replyMSG.append(new StringBuilder().append("Time to approve: ").append(formatTime(CastleManorManager.getInstance().getMillisToNextPeriodApprove())).append("</td></tr>").toString());
    replyMSG.append("</table>");

    replyMSG.append("<center><table><tr><td>");
    replyMSG.append("<button value=\"Set Next\" action=\"bypass -h admin_manor_setnext\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
    replyMSG.append("<button value=\"Approve Next\" action=\"bypass -h admin_manor_approve\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr><tr><td>");
    replyMSG.append(new StringBuilder().append("<button value=\"").append(CastleManorManager.getInstance().isUnderMaintenance() ? "Set normal" : "Set mainteance").append("\" action=\"bypass -h admin_manor_setmaintenance\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>").toString());
    replyMSG.append(new StringBuilder().append("<button value=\"").append(CastleManorManager.getInstance().isDisabled() ? "Enable" : "Disable").append("\" action=\"bypass -h admin_manor_disable\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr><tr><td>").toString());
    replyMSG.append("<button value=\"Refresh\" action=\"bypass -h admin_manor\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
    replyMSG.append("<button value=\"Back\" action=\"bypass -h admin_admin\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
    replyMSG.append("</table></center>");

    replyMSG.append("<br><center>Castle Information:<table width=\"100%\">");
    replyMSG.append("<tr><td></td><td>Current Period</td><td>Next Period</td></tr>");

    for (Castle c : CastleManager.getInstance().getCastles()) {
      replyMSG.append(new StringBuilder().append("<tr><td>").append(c.getName()).append("</td>").append("<td>").append(c.getManorCost(0)).append("a</td>").append("<td>").append(c.getManorCost(1)).append("a</td>").append("</tr>").toString());
    }

    replyMSG.append("</table><br>");

    replyMSG.append("</body></html>");

    adminReply.setHtml(replyMSG.toString());
    activeChar.sendPacket(adminReply);
  }

  private boolean checkLevel(int level) {
    return level >= REQUIRED_LEVEL;
  }
}