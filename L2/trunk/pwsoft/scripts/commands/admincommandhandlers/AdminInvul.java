package scripts.commands.admincommandhandlers;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import scripts.commands.IAdminCommandHandler;

public class AdminInvul
  implements IAdminCommandHandler
{
  private static Logger _log = Logger.getLogger(AdminInvul.class.getName());
  private static final String[] ADMIN_COMMANDS = { "admin_invul", "admin_setinvul" };
  private static final int REQUIRED_LEVEL = Config.GM_GODMODE;

  public boolean useAdminCommand(String command, L2PcInstance activeChar) {
    if ((!Config.ALT_PRIVILEGES_ADMIN) && (
      (!checkLevel(activeChar.getAccessLevel())) || (!activeChar.isGM()))) return false;

    GMAudit.auditGMAction(activeChar.getName(), command, activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target", "");

    if (command.equals("admin_invul")) handleInvul(activeChar);
    if (command.equals("admin_setinvul")) {
      L2Object target = activeChar.getTarget();
      if (target.isPlayer()) {
        handleInvul((L2PcInstance)target);
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

  private void handleInvul(L2PcInstance activeChar)
  {
    String text;
    if (activeChar.isInvul())
    {
      activeChar.setIsInvul(false);
      String text = activeChar.getName() + " is now mortal";
      if (Config.DEBUG)
        _log.fine("GM: Gm removed invul mode from character " + activeChar.getName() + "(" + activeChar.getObjectId() + ")");
    }
    else {
      activeChar.setIsInvul(true);
      text = activeChar.getName() + " is now invulnerable";
      if (Config.DEBUG)
        _log.fine("GM: Gm activated invul mode for character " + activeChar.getName() + "(" + activeChar.getObjectId() + ")");
    }
    activeChar.sendMessage(text);
  }
}