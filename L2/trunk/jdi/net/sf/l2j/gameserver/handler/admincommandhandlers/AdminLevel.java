package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.actor.stat.PlayableStat;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class AdminLevel
  implements IAdminCommandHandler
{
  private static final int REQUIRED_LEVEL = Config.GM_CHAR_EDIT;
  private static final String[] ADMIN_COMMANDS = { "admin_add_level", "admin_set_level" };

  public boolean useAdminCommand(String command, L2PcInstance activeChar)
  {
    if (activeChar == null) return false;

    if ((!Config.ALT_PRIVILEGES_ADMIN) && 
      (activeChar.getAccessLevel() < REQUIRED_LEVEL)) return false;

    L2Object targetChar = activeChar.getTarget();
    String target = targetChar != null ? targetChar.getName() : "no-target";
    GMAudit.auditGMAction(activeChar.getName(), command, target, "");

    StringTokenizer st = new StringTokenizer(command, " ");
    String actualCommand = st.nextToken();

    String val = "";
    if (st.countTokens() >= 1) val = st.nextToken();

    if (actualCommand.equalsIgnoreCase("admin_add_level"))
    {
      try
      {
        if ((targetChar instanceof L2PlayableInstance))
          ((L2PlayableInstance)targetChar).getStat().addLevel(Byte.parseByte(val));
      } catch (NumberFormatException e) {
        activeChar.sendMessage("Wrong Number Format");
      }
    }
    else if (actualCommand.equalsIgnoreCase("admin_set_level"))
    {
      try
      {
        if ((targetChar == null) || (!(targetChar instanceof L2PcInstance)))
        {
          activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
          return false;
        }
        L2PcInstance targetPlayer = (L2PcInstance)targetChar;

        byte lvl = Byte.parseByte(val);
        if ((lvl >= 1) && (lvl <= 81))
        {
          long pXp = targetPlayer.getExp();
          long tXp = net.sf.l2j.gameserver.model.base.Experience.LEVEL[lvl];

          if (pXp > tXp)
          {
            targetPlayer.removeExpAndSp(pXp - tXp, 0);
          } else if (pXp < tXp)
          {
            targetPlayer.addExpAndSp(tXp - pXp, 0);
          }
        }
        else
        {
          activeChar.sendMessage("You must specify level between 1 and 81.");
          return false;
        }
      }
      catch (NumberFormatException e)
      {
        activeChar.sendMessage("You must specify level between 1 and 81.");
        return false;
      }
    }
    return true;
  }

  public String[] getAdminCommandList()
  {
    return ADMIN_COMMANDS;
  }
}