package scripts.commands.admincommandhandlers;

import java.util.StringTokenizer;
import javolution.text.TextBuilder;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.instancemanager.CursedWeaponsManager;
import net.sf.l2j.gameserver.model.CursedWeapon;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import scripts.commands.IAdminCommandHandler;

public class AdminCursedWeapons
  implements IAdminCommandHandler
{
  private static final String[] ADMIN_COMMANDS = { "admin_cw_info", "admin_cw_remove", "admin_cw_goto", "admin_cw_reload", "admin_cw_add", "admin_cw_info_menu" };
  private static final int REQUIRED_LEVEL = Config.GM_MIN;
  private int itemId;

  public boolean useAdminCommand(String command, L2PcInstance activeChar)
  {
    if ((!Config.ALT_PRIVILEGES_ADMIN) && 
      (!checkLevel(activeChar.getAccessLevel()))) {
      return false;
    }
    CursedWeaponsManager cwm = CursedWeaponsManager.getInstance();
    int id = 0;

    StringTokenizer st = new StringTokenizer(command);
    st.nextToken();

    if (command.startsWith("admin_cw_info"))
    {
      if (!command.contains("menu"))
      {
        activeChar.sendAdmResultMessage("====== Cursed Weapons: ======");
        for (CursedWeapon cw : cwm.getCursedWeapons())
        {
          activeChar.sendAdmResultMessage(new StringBuilder().append("> ").append(cw.getName()).append(" (").append(cw.getItemId()).append(")").toString());
          if (cw.isActivated())
          {
            L2PcInstance pl = cw.getPlayer();
            activeChar.sendAdmResultMessage(new StringBuilder().append("  Player holding: ").append(pl).toString() == null ? "null" : pl.getName());
            activeChar.sendAdmResultMessage(new StringBuilder().append("    Player karma: ").append(cw.getPlayerKarma()).toString());
            activeChar.sendAdmResultMessage(new StringBuilder().append("    Time Remaining: ").append(cw.getTimeLeft() / 60000L).append(" min.").toString());
            activeChar.sendAdmResultMessage(new StringBuilder().append("    Kills : ").append(cw.getNbKills()).toString());
          }
          else if (cw.isDropped())
          {
            activeChar.sendAdmResultMessage("  Lying on the ground.");
            activeChar.sendAdmResultMessage(new StringBuilder().append("    Time Remaining: ").append(cw.getTimeLeft() / 60000L).append(" min.").toString());
            activeChar.sendAdmResultMessage(new StringBuilder().append("    Kills : ").append(cw.getNbKills()).toString());
          }
          else
          {
            activeChar.sendAdmResultMessage("  Don't exist in the world.");
          }
          activeChar.sendPacket(SystemMessage.id(SystemMessageId.FRIEND_LIST_FOOT));
        }
      }
      else
      {
        TextBuilder replyMSG = new TextBuilder();
        NpcHtmlMessage adminReply = NpcHtmlMessage.id(5);
        adminReply.setFile("data/html/admin/cwinfo.htm");
        for (CursedWeapon cw : cwm.getCursedWeapons())
        {
          itemId = cw.getItemId();
          replyMSG.append(new StringBuilder().append("<table width=270><tr><td>Name:</td><td>").append(cw.getName()).append("</td></tr>").toString());
          if (cw.isActivated())
          {
            L2PcInstance pl = cw.getPlayer();
            replyMSG.append(new StringBuilder().append("<tr><td>Weilder:</td><td>").append(pl == null ? "null" : pl.getName()).append("</td></tr>").toString());
            replyMSG.append(new StringBuilder().append("<tr><td>Karma:</td><td>").append(String.valueOf(cw.getPlayerKarma())).append("</td></tr>").toString());
            replyMSG.append(new StringBuilder().append("<tr><td>Kills:</td><td>").append(String.valueOf(cw.getPlayerPkKills())).append("/").append(String.valueOf(cw.getNbKills())).append("</td></tr>").toString());
            replyMSG.append(new StringBuilder().append("<tr><td>Time remaining:</td><td>").append(String.valueOf(cw.getTimeLeft() / 60000L)).append(" min.</td></tr>").toString());
            replyMSG.append(new StringBuilder().append("<tr><td><button value=\"Remove\" action=\"bypass -h admin_cw_remove ").append(String.valueOf(itemId)).append("\" width=73 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>").toString());
            replyMSG.append(new StringBuilder().append("<td><button value=\"Go\" action=\"bypass -h admin_cw_goto ").append(String.valueOf(itemId)).append("\" width=73 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>").toString());
          }
          else if (cw.isDropped())
          {
            replyMSG.append("<tr><td>Position:</td><td>Lying on the ground</td></tr>");
            replyMSG.append(new StringBuilder().append("<tr><td>Time remaining:</td><td>").append(String.valueOf(cw.getTimeLeft() / 60000L)).append(" min.</td></tr>").toString());
            replyMSG.append(new StringBuilder().append("<tr><td>Kills:</td><td>").append(String.valueOf(cw.getNbKills())).append("</td></tr>").toString());
            replyMSG.append(new StringBuilder().append("<tr><td><button value=\"Remove\" action=\"bypass -h admin_cw_remove ").append(String.valueOf(itemId)).append("\" width=73 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>").toString());
            replyMSG.append(new StringBuilder().append("<td><button value=\"Go\" action=\"bypass -h admin_cw_goto ").append(String.valueOf(itemId)).append("\" width=73 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>").toString());
          }
          else
          {
            replyMSG.append("<tr><td>Position:</td><td>Doesn't exist.</td></tr>");
            replyMSG.append(new StringBuilder().append("<tr><td><button value=\"Give to Target\" action=\"bypass -h admin_cw_add ").append(String.valueOf(itemId)).append("\" width=99 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td></td></tr>").toString());
          }
          replyMSG.append("</table>");
          replyMSG.append("<br>");
        }
        adminReply.replace("%cwinfo%", replyMSG.toString());
        activeChar.sendPacket(adminReply);
      }
    }
    else if (command.startsWith("admin_cw_reload"))
    {
      cwm.reload();
    }
    else
    {
      CursedWeapon cw = null;
      try
      {
        String parameter = st.nextToken();
        if (parameter.matches("[0-9]*")) {
          id = Integer.parseInt(parameter);
        }
        else {
          parameter = parameter.replace('_', ' ');
          for (CursedWeapon cwp : cwm.getCursedWeapons())
          {
            if (cwp.getName().toLowerCase().contains(parameter.toLowerCase()))
            {
              id = cwp.getItemId();
              break;
            }
          }
        }
        cw = cwm.getCursedWeapon(id);
        if (cw == null)
        {
          activeChar.sendAdmResultMessage("Unknown cursed weapon ID.");
          return false;
        }
      }
      catch (Exception e)
      {
        activeChar.sendAdmResultMessage("Usage: //cw_remove|//cw_goto|//cw_add <itemid|name>");
      }

      if (command.startsWith("admin_cw_remove "))
      {
        cw.endOfLife();
      }
      else if (command.startsWith("admin_cw_goto "))
      {
        cw.goTo(activeChar);
      }
      else if (command.startsWith("admin_cw_add"))
      {
        if (cw == null)
        {
          activeChar.sendAdmResultMessage("Usage: //cw_add <itemid|name>");
          return false;
        }
        if (cw.isActive()) {
          activeChar.sendAdmResultMessage("This cursed weapon is already active.");
        }
        else {
          L2Object target = activeChar.getTarget();
          if ((target != null) && (target.isPlayer()))
            ((L2PcInstance)target).addItem("AdminCursedWeaponAdd", id, 1, target, true);
          else
            activeChar.addItem("AdminCursedWeaponAdd", id, 1, activeChar, true);
        }
      }
      else
      {
        activeChar.sendAdmResultMessage("Unknown command.");
      }
    }
    return true;
  }

  public String[] getAdminCommandList() {
    return ADMIN_COMMANDS;
  }

  private boolean checkLevel(int level) {
    return level >= REQUIRED_LEVEL;
  }
}