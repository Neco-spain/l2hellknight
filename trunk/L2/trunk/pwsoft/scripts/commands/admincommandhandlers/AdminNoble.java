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

public class AdminNoble
  implements IAdminCommandHandler
{
  private static String[] _adminCommands = { "admin_setnoble" };

  private static final Log _log = LogFactory.getLog(AdminNoble.class.getName());
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
    if (command.startsWith("admin_setnoble"))
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
      if (player.isNoble())
      {
        player.setNoble(false);
        sm.addString("You are no longer a server noble.");
        GmListTable.broadcastMessageToGMs("GM " + activeChar.getName() + " removed noble stat of player" + target.getName());
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
          statement = connection.prepareStatement("UPDATE characters SET nobless=0 WHERE obj_id=?");
          statement.setInt(1, objId);
          statement.execute();
          statement.close();
          connection.close();
        }
        catch (Exception e)
        {
          _log.warn("could not set noble stats of char:", e);
        }
        finally {
          try {
            connection.close();
          } catch (Exception e) {
          }
        }
      } else {
        player.setNoble(true);
        sm.addString("You are now a server noble, congratulations!");
        GmListTable.broadcastMessageToGMs("GM " + activeChar.getName() + " has given noble stat for player " + target.getName() + ".");
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
          statement = connection.prepareStatement("UPDATE characters SET nobless=1 WHERE obj_id=?");
          statement.setInt(1, objId);
          statement.execute();
          statement.close();
          connection.close();
        }
        catch (Exception e)
        {
          _log.warn("could not set noble stats of char:", e);
        }
        finally {
          try {
            connection.close(); } catch (Exception e) {  }

        }
      }
      player.sendPacket(sm);
      player.broadcastUserInfo();
      if (player.isNoble() != true);
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