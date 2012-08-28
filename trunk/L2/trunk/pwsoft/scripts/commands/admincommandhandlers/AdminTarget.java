package scripts.commands.admincommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import scripts.commands.IAdminCommandHandler;

public class AdminTarget
  implements IAdminCommandHandler
{
  private static final String[] ADMIN_COMMANDS = { "admin_target" };
  private static final int REQUIRED_LEVEL = Config.GM_MIN;

  public boolean useAdminCommand(String command, L2PcInstance activeChar) {
    if ((!Config.ALT_PRIVILEGES_ADMIN) && (
      (!checkLevel(activeChar.getAccessLevel())) || (!activeChar.isGM()))) return false;

    if (command.startsWith("admin_target")) handleTarget(command, activeChar);
    return true;
  }

  public String[] getAdminCommandList() {
    return ADMIN_COMMANDS;
  }

  private boolean checkLevel(int level) {
    return level >= REQUIRED_LEVEL;
  }

  private void handleTarget(String command, L2PcInstance activeChar) {
    try {
      String targetName = command.substring(13);
      L2Object obj = L2World.getInstance().getPlayer(targetName);
      if ((obj != null) && (obj.isPlayer())) {
        obj.onAction(activeChar);
      } else {
        SystemMessage sm = SystemMessage.id(SystemMessageId.S1_S2);
        sm.addString("Player " + targetName + " not found");
        activeChar.sendPacket(sm);
      }
    } catch (IndexOutOfBoundsException e) {
      SystemMessage sm = SystemMessage.id(SystemMessageId.S1_S2);
      sm.addString("Please specify correct name.");
      activeChar.sendPacket(sm);
    }
  }
}