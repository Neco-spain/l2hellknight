package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.Universe;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;

public class AdminTest
  implements IAdminCommandHandler
{
  private static final int REQUIRED_LEVEL = Config.GM_TEST;
  private static final String[] ADMIN_COMMANDS = { "admin_test", "admin_stats", "admin_skill_test", "admin_st", "admin_mp", "admin_known" };

  public boolean useAdminCommand(String command, L2PcInstance activeChar)
  {
    if ((!Config.ALT_PRIVILEGES_ADMIN) && 
      (activeChar.getAccessLevel() < REQUIRED_LEVEL)) return false;

    if (command.equals("admin_stats"))
    {
      for (String line : ThreadPoolManager.getInstance().getStats())
      {
        activeChar.sendMessage(line);
      }
    }
    else if ((command.startsWith("admin_skill_test")) || (command.startsWith("admin_st")))
    {
      try
      {
        StringTokenizer st = new StringTokenizer(command);
        st.nextToken();
        int id = Integer.parseInt(st.nextToken());
        adminTestSkill(activeChar, id);
      }
      catch (NumberFormatException e)
      {
        activeChar.sendMessage("Command format is //skill_test <ID>");
      }
      catch (NoSuchElementException nsee)
      {
        activeChar.sendMessage("Command format is //skill_test <ID>");
      }
    }
    else if (command.startsWith("admin_test uni flush"))
    {
      Universe.getInstance().flush();
      activeChar.sendMessage("Universe Map Saved.");
    }
    else if (command.startsWith("admin_test uni"))
    {
      activeChar.sendMessage("Universe Map Size is: " + Universe.getInstance().size());
    }
    else if (command.equals("admin_mp on"))
    {
      activeChar.sendMessage("command not working");
    }
    else if (command.equals("admin_mp off"))
    {
      activeChar.sendMessage("command not working");
    }
    else if (command.equals("admin_mp dump"))
    {
      activeChar.sendMessage("command not working");
    }
    else if (command.equals("admin_known on"))
    {
      Config.CHECK_KNOWN = true;
    }
    else if (command.equals("admin_known off"))
    {
      Config.CHECK_KNOWN = false;
    }
    return true;
  }

  private void adminTestSkill(L2PcInstance activeChar, int id)
  {
    L2Object target = activeChar.getTarget();
    L2Character player;
    L2Character player;
    if ((target == null) || (!(target instanceof L2Character)))
    {
      player = activeChar;
    }
    else
    {
      player = (L2Character)target;
    }
    player.broadcastPacket(new MagicSkillUser(activeChar, player, id, 1, 1, 1));
  }

  public String[] getAdminCommandList()
  {
    return ADMIN_COMMANDS;
  }
}