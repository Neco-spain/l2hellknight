package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class AdminSetNoble
  implements IAdminCommandHandler
{
  private static final int REQUIRED_LEVEL = Config.GM_CHAR_EDIT;

  private static String[] ADMIN_COMMANDS = { "admin_setnoble" };

  public boolean useAdminCommand(String command, L2PcInstance activeChar)
  {
    if (!Config.ALT_PRIVILEGES_ADMIN)
    {
      if ((!checkLevel(activeChar.getAccessLevel())) || (!activeChar.isGM()))
      {
        return false;
      }
    }

    if (activeChar == null) {
      return false;
    }
    if (command.startsWith("admin_setnoble"))
    {
      L2Object target = activeChar.getTarget();

      if ((target instanceof L2PcInstance))
      {
        L2PcInstance targetPlayer = (L2PcInstance)target;

        boolean isNoble = targetPlayer.isNoble();

        if (!isNoble)
        {
          targetPlayer.setNoble(true);
          targetPlayer.sendMessage("\u0422\u0435\u043F\u0435\u0440\u044C \u0432\u044B \u0434\u0432\u043E\u0440\u044F\u043D\u0438\u043D!");
          targetPlayer.addItem("Tiara", 7694, 1, null, true);
        }
        else
        {
          targetPlayer.setNoble(false);
          targetPlayer.sendMessage("\u0412\u044B \u0431\u043E\u043B\u044C\u0448\u0435 \u043D\u0435 \u0434\u0432\u043E\u0440\u044F\u043D\u0438\u043D.");
        }

        targetPlayer = null;
      }
      else
      {
        activeChar.sendMessage("\u0412\u044B\u0434\u0435\u043B\u0438\u0442\u0435 \u0432 \u0442\u0430\u0440\u0433\u0435\u0442 \u0442\u043E\u0433\u043E, \u043A\u043E\u043C\u0443 \u0445\u043E\u0442\u0438\u0442\u0435 \u0432\u044B\u0434\u0430\u0442\u044C \u0434\u0432\u043E\u0440\u044F\u043D\u0441\u0442\u0432\u043E.");
        return false;
      }

      target = null;
    }

    return true;
  }

  private boolean checkLevel(int level)
  {
    return level >= REQUIRED_LEVEL;
  }

  public String[] getAdminCommandList()
  {
    return ADMIN_COMMANDS;
  }
}