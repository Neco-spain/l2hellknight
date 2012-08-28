package scripts.commands.admincommandhandlers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.GmListTable;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.mysql.Connect;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import scripts.commands.IAdminCommandHandler;

public class AdminDonator
  implements IAdminCommandHandler
{
  private static String[] _adminCommands = { "admin_setdonator" };

  private static final Log _log = LogFactory.getLog(AdminDonator.class.getName());
  private static final int REQUIRED_LEVEL = Config.GM_MENU;

  public boolean useAdminCommand(String command, L2PcInstance activeChar)
  {
    if (!Config.ALT_PRIVILEGES_ADMIN)
    {
      if ((!checkLevel(activeChar.getAccessLevel())) || (!activeChar.isGM()))
      {
        return false;
      }
    }
    if (command.startsWith("admin_setdonator"))
    {
      L2Object target = activeChar.getTarget();
      L2PcInstance player = null;
      SystemMessage sm = SystemMessage.id(SystemMessageId.S1_S2);
      if (target.isPlayer())
      {
        player = (L2PcInstance)target;
      }
      else
        player = activeChar;
      int i;
      if (player.isDonator())
      {
        player.setDonator(false);
        player.updateNameTitleColor();
        sm.addString("You are no longer a server donator.");
        GmListTable.broadcastMessageToGMs("GM " + activeChar.getName() + " removed donator stat of player" + target.getName());
        Connect connection = null;
        try
        {
          connection = L2DatabaseFactory.getInstance().getConnection();

          PreparedStatement statement = connection.prepareStatement("SELECT obj_id FROM characters where char_name=?");
          statement.setString(1, target.getName());
          ResultSet rset = statement.executeQuery();
          int objId = 0;
          if (rset.next())
          {
            objId = rset.getInt(1);
          }
          rset.close();
          statement.close();

          if (objId == 0) { connection.close(); i = 0;
            return i;
          }
          statement = connection.prepareStatement("UPDATE characters SET donator=0 WHERE obj_id=?");
          statement.setInt(1, objId);
          statement.execute();
          statement.close();
          connection.close();
        }
        catch (Exception e)
        {
          _log.warn("could not set donator stats of char:", e);
        }
        finally {
          try {
            connection.close();
          } catch (Exception e) {
          }
        }
      } else {
        player.setDonator(true);
        player.updateNameTitleColor();
        sm.addString("You are now a server donator, congratulations!");
        GmListTable.broadcastMessageToGMs("GM " + activeChar.getName() + " has given donator stat for player " + target.getName() + ".");
        Connect connection = null;
        try
        {
          connection = L2DatabaseFactory.getInstance().getConnection();

          PreparedStatement statement = connection.prepareStatement("SELECT obj_id FROM characters where char_name=?");
          statement.setString(1, target.getName());
          ResultSet rset = statement.executeQuery();
          int objId = 0;
          if (rset.next())
          {
            objId = rset.getInt(1);
          }
          rset.close();
          statement.close();

          if (objId == 0) { connection.close(); i = 0;
            return i;
          }
          statement = connection.prepareStatement("UPDATE characters SET donator=1 WHERE obj_id=?");
          statement.setInt(1, objId);
          statement.execute();
          statement.close();
          connection.close();
        }
        catch (Exception e)
        {
          _log.warn("could not set donator stats of char:", e);
        }
        finally {
          try {
            connection.close(); } catch (Exception e) {  }

        }
      }
      player.sendPacket(sm);
      player.broadcastUserInfo();
      if (player.isDonator() != true);
    }
    return false;
  }
  public String[] getAdminCommandList() {
    return _adminCommands;
  }

  private boolean checkLevel(int level) {
    return level >= REQUIRED_LEVEL;
  }
}