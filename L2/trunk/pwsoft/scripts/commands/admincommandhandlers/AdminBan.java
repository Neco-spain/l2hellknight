package scripts.commands.admincommandhandlers;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.LoginServerThread;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.olympiad.Olympiad;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.mysql.Connect;
import net.sf.l2j.util.Log;
import scripts.commands.IAdminCommandHandler;

public class AdminBan
  implements IAdminCommandHandler
{
  private static final String[] ADMIN_COMMANDS = { "admin_ban", "admin_unban", "admin_jail", "admin_unjail", "admin_hwidban" };
  private static final int REQUIRED_LEVEL = Config.GM_BAN;

  public boolean useAdminCommand(String command, L2PcInstance adm) {
    if ((!Config.ALT_PRIVILEGES_ADMIN) && 
      (!checkLevel(adm.getAccessLevel()))) {
      return false;
    }

    StringTokenizer st = new StringTokenizer(command);
    st.nextToken();
    String account_name = "";
    String player = "";
    L2PcInstance plyr = null;
    if (command.startsWith("admin_ban")) {
      try {
        player = st.nextToken();
        plyr = L2World.getInstance().getPlayer(player);
      } catch (Exception e) {
        L2Object target = adm.getTarget();
        if ((target != null) && (target.isPlayer()))
          plyr = (L2PcInstance)target;
        else {
          adm.sendAdmResultMessage("Usage: //ban [account_name] (if none, target char's account gets banned)");
        }
      }
      if ((plyr != null) && (plyr.equals(adm))) {
        plyr.sendPacket(SystemMessage.id(SystemMessageId.CANNOT_USE_ON_YOURSELF));
      } else if (plyr == null) {
        account_name = player;
        LoginServerThread.getInstance().sendAccessLevel(account_name, 0);
        adm.sendAdmResultMessage(new StringBuilder().append("Ban request sent for account ").append(account_name).append(". If you need a playername based commmand, see //ban_menu").toString());
      } else {
        Olympiad.clearPoints(plyr.getObjectId());
        plyr.setAccountAccesslevel(-100);
        account_name = plyr.getAccountName();

        plyr.logout();
        adm.sendAdmResultMessage(new StringBuilder().append("Account ").append(account_name).append(" banned.").toString());
      }
    } else if (command.startsWith("admin_unban")) {
      try {
        account_name = st.nextToken();
        LoginServerThread.getInstance().sendAccessLevel(account_name, 0);
        adm.sendAdmResultMessage(new StringBuilder().append("Unban request sent for account ").append(account_name).append(". If you need a playername based commmand, see //unban_menu").toString());
      } catch (Exception e) {
        adm.sendAdmResultMessage("Usage: //unban <account_name>");
        if (Config.DEBUG)
          e.printStackTrace();
      }
    }
    else if (command.startsWith("admin_jail")) {
      try {
        player = st.nextToken();
        int delay = 0;
        try {
          delay = Integer.parseInt(st.nextToken());
        } catch (NumberFormatException nfe) {
          adm.sendAdmResultMessage("Usage: //jail <charname> [penalty_minutes]");
        } catch (NoSuchElementException nsee) {
        }
        L2PcInstance playerObj = L2World.getInstance().getPlayer(player);
        if (playerObj != null) {
          playerObj.setInJail(true, delay);
          adm.sendAdmResultMessage(new StringBuilder().append("Character ").append(player).append(" jailed for ").append(delay > 0 ? new StringBuilder().append(delay).append(" minutes.").toString() : "ever!").toString());
        } else {
          jailOfflinePlayer(adm, player, delay);
        }
      } catch (NoSuchElementException nsee) {
        adm.sendAdmResultMessage("Usage: //jail <charname> [penalty_minutes]");
      } catch (Exception e) {
        if (Config.DEBUG)
          e.printStackTrace();
      }
    }
    else if (command.startsWith("admin_unjail")) {
      try {
        player = st.nextToken();
        L2PcInstance playerObj = L2World.getInstance().getPlayer(player);

        if (playerObj != null) {
          playerObj.setInJail(false, 0);
          adm.sendAdmResultMessage(new StringBuilder().append("Character ").append(player).append(" removed from jail").toString());
        } else {
          unjailOfflinePlayer(adm, player);
        }
      } catch (NoSuchElementException nsee) {
        adm.sendAdmResultMessage("Specify a character name.");
      } catch (Exception e) {
        if (Config.DEBUG)
          e.printStackTrace();
      }
    }
    else if (command.equalsIgnoreCase("admin_hwidban")) {
      L2Object target = adm.getTarget();
      if ((target != null) && (target.isPlayer())) {
        plyr = (L2PcInstance)target;
      }

      if ((plyr != null) && (plyr.equals(adm))) {
        plyr.sendPacket(SystemMessage.id(SystemMessageId.CANNOT_USE_ON_YOURSELF));
      } else {
        Olympiad.clearPoints(plyr.getObjectId());
        plyr.logout();
        adm.sendAdmResultMessage(new StringBuilder().append("HWID ").append(plyr.getHWID()).append(" banned.").toString());
        Log.banHWID(plyr.getHWID(), plyr.getIP(), plyr.getAccountName());
      }
    }
    GMAudit.auditGMAction(adm.getName(), command, player, "");
    return true;
  }

  private void jailOfflinePlayer(L2PcInstance adm, String name, int delay) {
    Connect con = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=?, y=?, z=?, in_jail=?, jail_timer=? WHERE char_name=?");
      statement.setInt(1, -114356);
      statement.setInt(2, -249645);
      statement.setInt(3, -2984);
      statement.setInt(4, 1);
      statement.setLong(5, delay * 60000L);
      statement.setString(6, name);

      statement.execute();
      int count = statement.getUpdateCount();
      statement.close();

      if (count == 0)
        adm.sendAdmResultMessage("Character not found!");
      else
        adm.sendAdmResultMessage(new StringBuilder().append("Character ").append(name).append(" jailed for ").append(delay > 0 ? new StringBuilder().append(delay).append(" minutes.").toString() : "ever!").toString());
    }
    catch (SQLException e) {
      adm.sendAdmResultMessage("SQLException while jailing player");
      if (Config.DEBUG)
        se.printStackTrace();
    }
    finally {
      try {
        con.close();
      } catch (Exception e) {
        if (Config.DEBUG)
          e.printStackTrace();
      }
    }
  }

  private void unjailOfflinePlayer(L2PcInstance adm, String name)
  {
    Connect con = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=?, y=?, z=?, in_jail=?, jail_timer=? WHERE char_name=?");
      statement.setInt(1, 17836);
      statement.setInt(2, 170178);
      statement.setInt(3, -3507);
      statement.setInt(4, 0);
      statement.setLong(5, 0L);
      statement.setString(6, name);
      statement.execute();
      int count = statement.getUpdateCount();
      statement.close();
      if (count == 0)
        adm.sendAdmResultMessage("Character not found!");
      else
        adm.sendAdmResultMessage(new StringBuilder().append("Character ").append(name).append(" removed from jail").toString());
    }
    catch (SQLException e) {
      adm.sendAdmResultMessage("SQLException while jailing player");
      if (Config.DEBUG)
        se.printStackTrace();
    }
    finally {
      try {
        con.close();
      } catch (Exception e) {
        if (Config.DEBUG)
          e.printStackTrace();
      }
    }
  }

  public String[] getAdminCommandList()
  {
    return ADMIN_COMMANDS;
  }

  private boolean checkLevel(int level) {
    return level >= REQUIRED_LEVEL;
  }
}