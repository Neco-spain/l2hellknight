package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
//by Sarkazm
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
          targetPlayer.sendMessage("Теперь вы дворянин!");
          targetPlayer.addItem("Tiara", 7694, 1, null, true);
        }
        else
        {
          targetPlayer.setNoble(false);
          targetPlayer.sendMessage("Вы больше не дворянин.");
          //targetPlayer.destroyItem("Tiara", 7694, 1, null, true);
        }

        targetPlayer = null;
      }
      else
      {
        activeChar.sendMessage("Выделите в таргет того, кому хотите выдать дворянство.");
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