package scripts.commands.admincommandhandlers;

import java.io.PrintStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import scripts.commands.IAdminCommandHandler;

public class AdminChangeAccessLevel
  implements IAdminCommandHandler
{
  private static final String[] ADMIN_COMMANDS = { "admin_banchar", "admin_unbanchar", "admin_changelvl" };

  private static final int REQUIRED_LEVEL = Config.GM_ACCESSLEVEL;

  public boolean useAdminCommand(String command, L2PcInstance adm)
  {
    if ((!Config.ALT_PRIVILEGES_ADMIN) && (
      (!checkLevel(adm.getAccessLevel())) || (!adm.isGM()))) {
      return false;
    }
    String nick = "no-target";
    if (command.startsWith("admin_banchar"))
    {
      String cmd = command.substring(14);
      nick = cmd.split(" ")[0];
      String reason = cmd.replace(nick + " ", "");
      banPlayer(adm, nick.trim(), reason, -100);
    }
    else if (command.startsWith("admin_unbanchar"))
    {
      nick = command.substring(16).trim();
      banPlayer(adm, nick, "", 0);
    }
    else if (command.startsWith("admin_changelvl"))
    {
      handleChangeLevel(command, adm);
      nick = adm.getTarget() != null ? adm.getTarget().getName() : "no-target";
    }
    GMAudit.auditGMAction(adm.getName(), command, nick, "");
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

  private void handleChangeLevel(String command, L2PcInstance adm)
  {
    String[] parts = command.split(" ");
    if (parts.length == 2)
    {
      try
      {
        int lvl = Integer.parseInt(parts[1]);
        if (adm.getTarget().isPlayer())
          onLineChange(adm, (L2PcInstance)adm.getTarget(), lvl);
        else
          adm.sendPacket(SystemMessage.id(SystemMessageId.INCORRECT_TARGET));
      }
      catch (Exception e)
      {
        adm.sendAdmResultMessage("Usage: //changelvl <target_new_level> | <player_name> <new_level>");
      }
    }
    else if (parts.length == 3)
    {
      String name = parts[1];
      int lvl = Integer.parseInt(parts[2]);
      L2PcInstance player = L2World.getInstance().getPlayer(name);
      if (player != null) {
        onLineChange(adm, player, lvl);
      }
      else {
        Connect con = null;
        try
        {
          con = L2DatabaseFactory.getInstance().getConnection();
          PreparedStatement statement = con.prepareStatement("UPDATE characters SET accesslevel=? WHERE char_name=?");
          statement.setInt(1, lvl);
          statement.setString(2, name);
          statement.execute();
          int count = statement.getUpdateCount();
          statement.close();
          if (count == 0)
            adm.sendAdmResultMessage("Character not found or access level unaltered.");
          else
            adm.sendAdmResultMessage("Character's access level is now set to " + lvl);
        }
        catch (SQLException e)
        {
          adm.sendAdmResultMessage("SQLException while changing character's access level");
          if (Config.DEBUG)
            se.printStackTrace();
        }
        finally
        {
          try
          {
            con.close();
          }
          catch (Exception e)
          {
          }
        }
      }
    }
  }

  private void banPlayer(L2PcInstance adm, String nick, String reason, int acs) {
    L2PcInstance player = L2World.getInstance().getPlayer(nick);
    if (player != null)
    {
      player.setAccessLevel(acs);
      player.kick();
    }

    Connect con = null;
    PreparedStatement st = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      st = con.prepareStatement("UPDATE `characters` SET `accesslevel`=?, `BanReason`=? WHERE `char_name`=?");
      st.setInt(1, acs);
      st.setString(2, reason);
      st.setString(3, nick);
      st.execute();
      int count = st.getUpdateCount();
      if (count == 0) {
        adm.sendAdmResultMessage("\u0418\u0433\u0440\u043E\u043A " + nick + " \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D \u0432 \u0431\u0430\u0437\u0435 \u0434\u0430\u043D\u043D\u044B\u0445.");
      }
      else if (acs < 0)
      {
        Announcements.getInstance().announceToAll("\u0417\u0430\u0431\u0430\u043D\u0435\u043D \u0438\u0433\u0440\u043E\u043A " + nick + ".");
        Announcements.getInstance().announceToAll("(" + reason + ")");
        adm.sendAdmResultMessage("\u0418\u0433\u0440\u043E\u043A " + nick + " \u0437\u0430\u0431\u0430\u043D\u0435\u043D. (" + reason + ")");
      }
      else
      {
        adm.sendAdmResultMessage("\u0418\u0433\u0440\u043E\u043A " + nick + " \u0440\u0430\u0437\u0431\u0430\u043D\u0435\u043D.");
      }

    }
    catch (SQLException e)
    {
      System.out.println("[ERROR] AdminBan, banPlayer() " + e);
    }
    finally
    {
      Close.CS(con, st);
    }
  }

  private void onLineChange(L2PcInstance adm, L2PcInstance player, int lvl)
  {
    player.setAccessLevel(lvl);
    if (lvl > 0) {
      player.sendAdmResultMessage("Your access level has been changed to " + lvl);
    }
    else {
      player.sendAdmResultMessage("Your character has been banned. Bye.");
      player.logout();
    }
    adm.sendAdmResultMessage("Character's access level is now set to " + lvl + ". Effects won't be noticeable until next session.");
  }
}