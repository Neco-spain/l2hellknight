package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class AdminChangeAccessLevel
  implements IAdminCommandHandler
{
  protected static final Logger _log = Logger.getLogger(AdminChangeAccessLevel.class.getName());
  public int[] GmListId = Config.GM_LIST_ID;
  private static final String[] ADMIN_COMMANDS = { "admin_changelvl" };

  private static final int REQUIRED_LEVEL = Config.GM_ACCESSLEVEL;

  public boolean useAdminCommand(String command, L2PcInstance activeChar)
  {
    if ((!Config.ALT_PRIVILEGES_ADMIN) && (
      (!checkLevel(activeChar.getAccessLevel())) || (!activeChar.isGM()))) {
      return false;
    }
    handleChangeLevel(command, activeChar);
    String target = activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target";
    GMAudit.auditGMAction(activeChar.getName(), command, target, "");
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

  private void handleChangeLevel(String command, L2PcInstance activeChar)
  {
    String[] parts = command.split(" ");
    if (parts.length == 2)
    {
      try
      {
        int lvl = Integer.parseInt(parts[1]);
        if ((activeChar.getTarget() instanceof L2PcInstance))
          onLineChange(activeChar, (L2PcInstance)activeChar.getTarget(), lvl);
        else
          activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
      }
      catch (Exception e)
      {
        activeChar.sendMessage("Usage: //changelvl <target_new_level> | <player_name> <new_level>");
      }
    }
    else if (parts.length == 3)
    {
      String name = parts[1];
      int lvl = Integer.parseInt(parts[2]);
      L2PcInstance player = L2World.getInstance().getPlayer(name);
      if (player != null) {
        onLineChange(activeChar, player, lvl);
      }
      else {
        Connection con = null;
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
            activeChar.sendMessage("Character not found or access level unaltered.");
          else
            activeChar.sendMessage("Character's access level is now set to " + lvl);
        }
        catch (SQLException e)
        {
          activeChar.sendMessage("SQLException while changing character's access level");
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

  private void onLineChange(L2PcInstance activeChar, L2PcInstance player, int lvl)
  {
    if (Config.CHECK_IS_GM_BY_ID)
    {
      boolean isOk = false;
      for (int GmId : GmListId) {
        if (GmId != player.getObjectId())
          continue;
        player.setAccessLevel(lvl);
        player.sendMessage("Your access level has been changed to " + lvl);
        _log.info(player.getName() + " access level has been changed to " + lvl);
        isOk = true;
        break;
      }
      if (!isOk)
      {
        _log.info("WARNING: " + player.getName() + "bad access level changed");
        player.sendMessage("Your character has been banned. Bye.");
        player.closeNetConnection(false);
      }
    }
    else
    {
      player.setAccessLevel(lvl);
      if (lvl > 0)
      {
        player.sendMessage("Your access level has been changed to " + lvl);
      }
      else
      {
        player.sendMessage("Your character has been banned. Bye.");
        player.closeNetConnection(false);
      }
      activeChar.sendMessage("Character's access level is now set to " + lvl + ". Effects won't be noticeable until next session.");
    }
  }
}