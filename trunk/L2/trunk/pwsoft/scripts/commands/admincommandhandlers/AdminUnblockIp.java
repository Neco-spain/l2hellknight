package scripts.commands.admincommandhandlers;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import scripts.commands.IAdminCommandHandler;

public class AdminUnblockIp
  implements IAdminCommandHandler
{
  private static final Logger _log = Logger.getLogger(AdminTeleport.class.getName());

  private static final int REQUIRED_LEVEL = Config.GM_UNBLOCK;
  private static final String[] ADMIN_COMMANDS = { "admin_unblockip" };

  public boolean useAdminCommand(String command, L2PcInstance activeChar)
  {
    if ((!Config.ALT_PRIVILEGES_ADMIN) && (
      (!checkLevel(activeChar.getAccessLevel())) || (!activeChar.isGM()))) return false;

    if (command.startsWith("admin_unblockip "))
    {
      try
      {
        String ipAddress = command.substring(16);
        if (unblockIp(ipAddress, activeChar))
        {
          SystemMessage sm = SystemMessage.id(SystemMessageId.S1_S2);
          sm.addString("Removed IP " + ipAddress + " from blocklist!");
          activeChar.sendPacket(sm);
        }

      }
      catch (StringIndexOutOfBoundsException e)
      {
        SystemMessage sm = SystemMessage.id(SystemMessageId.S1_S2);
        sm.addString("Usage mode: //unblockip <ip>");
        activeChar.sendPacket(sm);
      }
    }

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

  private boolean unblockIp(String ipAddress, L2PcInstance activeChar)
  {
    _log.warning("IP removed by GM " + activeChar.getName());
    return true;
  }
}