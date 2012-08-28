package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.LoginServerThread;
import net.sf.l2j.gameserver.communitybbs.Manager.RegionBBSManager;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class AdminBan
  implements IAdminCommandHandler
{
  private static final String[] ADMIN_COMMANDS = { "admin_ban", "admin_unban", "admin_jail", "admin_unjail" };
  private static final int REQUIRED_LEVEL = Config.GM_BAN;

  public boolean useAdminCommand(String command, L2PcInstance activeChar)
  {
    if ((!Config.ALT_PRIVILEGES_ADMIN) && 
      (!checkLevel(activeChar.getAccessLevel())))
      return false;
    StringTokenizer st = new StringTokenizer(command);
    st.nextToken();
    String account_name = "";
    String player = "";
    L2PcInstance plyr = null;
    if (command.startsWith("admin_ban"))
    {
      try
      {
        player = st.nextToken();
        plyr = L2World.getInstance().getPlayer(player);
      }
      catch (Exception e)
      {
        L2Object target = activeChar.getTarget();
        if ((target != null) && ((target instanceof L2PcInstance)))
          plyr = (L2PcInstance)target;
        else
          activeChar.sendMessage("Usage: //ban [account_name] (if none, target char's account gets banned)");
      }
      if ((plyr != null) && (plyr.equals(activeChar))) {
        plyr.sendPacket(new SystemMessage(SystemMessageId.CANNOT_USE_ON_YOURSELF));
      } else if (plyr == null)
      {
        account_name = player;
        LoginServerThread.getInstance().sendAccessLevel(account_name, 0);
        activeChar.sendMessage(new StringBuilder().append("Ban request sent for account ").append(account_name).append(". If you need a playername based commmand, see //ban_menu").toString());
      }
      else
      {
        plyr.setAccountAccesslevel(-100);
        account_name = plyr.getAccountName();
        RegionBBSManager.getInstance().changeCommunityBoard();
        plyr.closeNetConnection(false);
        activeChar.sendMessage(new StringBuilder().append("Account ").append(account_name).append(" banned.").toString());
      }
    }
    else if (command.startsWith("admin_unban"))
    {
      try
      {
        account_name = st.nextToken();
        LoginServerThread.getInstance().sendAccessLevel(account_name, 0);
        activeChar.sendMessage(new StringBuilder().append("Unban request sent for account ").append(account_name).append(". If you need a playername based commmand, see //unban_menu").toString());
      }
      catch (Exception e)
      {
        activeChar.sendMessage("Usage: //unban <account_name>");
      }
    }
    else if (command.startsWith("admin_jail"))
    {
      try
      {
        player = st.nextToken();
        int delay = 0;
        try
        {
          delay = Integer.parseInt(st.nextToken());
        }
        catch (NumberFormatException nfe)
        {
          activeChar.sendMessage("Usage: //jail <charname> [penalty_minutes]");
        } catch (NoSuchElementException nsee) {
        }
        L2PcInstance playerObj = L2World.getInstance().getPlayer(player);
        if (playerObj != null)
        {
          playerObj.setInJail(true, delay);
          activeChar.sendMessage(new StringBuilder().append("Character ").append(player).append(" jailed for ").append(delay > 0 ? new StringBuilder().append(delay).append(" minutes.").toString() : "ever!").toString());
        }
        else {
          jailOfflinePlayer(activeChar, player, delay);
        }
      }
      catch (NoSuchElementException nsee) {
        activeChar.sendMessage("Usage: //jail <charname> [penalty_minutes]");
      }
      catch (Exception e)
      {
      }
    }
    else if (command.startsWith("admin_unjail"))
    {
      try
      {
        player = st.nextToken();
        L2PcInstance playerObj = L2World.getInstance().getPlayer(player);

        if (playerObj != null)
        {
          playerObj.setInJail(false, 0);
          activeChar.sendMessage(new StringBuilder().append("Character ").append(player).append(" removed from jail").toString());
        }
        else {
          unjailOfflinePlayer(activeChar, player);
        }
      }
      catch (NoSuchElementException nsee) {
        activeChar.sendMessage("Specify a character name.");
      }
      catch (Exception e)
      {
      }
    }
    GMAudit.auditGMAction(activeChar.getName(), command, player, "");
    return true;
  }

  private void jailOfflinePlayer(L2PcInstance activeChar, String name, int delay)
  {
    Connection con = null;
    try
    {
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
        activeChar.sendMessage("Character not found!");
      else
        activeChar.sendMessage(new StringBuilder().append("Character ").append(name).append(" jailed for ").append(delay > 0 ? new StringBuilder().append(delay).append(" minutes.").toString() : "ever!").toString());
    }
    catch (SQLException e) {
      activeChar.sendMessage("SQLException while jailing player");
    } finally {
      try {
        con.close();
      }
      catch (Exception e) {
      }
    }
  }

  private void unjailOfflinePlayer(L2PcInstance activeChar, String name) {
    Connection con = null;
    try
    {
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
        activeChar.sendMessage("Character not found!");
      else
        activeChar.sendMessage(new StringBuilder().append("Character ").append(name).append(" removed from jail").toString());
    }
    catch (SQLException e) {
      activeChar.sendMessage("SQLException while jailing player");
    }
    finally
    {
      try {
        con.close();
      }
      catch (Exception e)
      {
      }
    }
  }

  public String[] getAdminCommandList() {
    return ADMIN_COMMANDS;
  }

  private boolean checkLevel(int level) {
    return level >= REQUIRED_LEVEL;
  }
}