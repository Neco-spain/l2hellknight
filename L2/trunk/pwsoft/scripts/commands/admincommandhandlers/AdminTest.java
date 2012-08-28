package scripts.commands.admincommandhandlers;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.LoginServerThread;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import scripts.commands.IAdminCommandHandler;

public class AdminTest
  implements IAdminCommandHandler
{
  private static final int REQUIRED_LEVEL = Config.GM_TEST;
  private static final String[] ADMIN_COMMANDS = { "admin_test", "admin_stats", "admin_skill_test", "admin_st", "admin_mp", "admin_known", "admin_reconls" };

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
        activeChar.sendAdmResultMessage("Command format is //skill_test <ID>");
      }
      catch (NoSuchElementException nsee)
      {
        activeChar.sendAdmResultMessage("Command format is //skill_test <ID>");
      }
    }
    else if (command.startsWith("admin_test uni flush"))
    {
      activeChar.sendAdmResultMessage("Universe Map Saved.");
    }
    else if (!command.startsWith("admin_test uni"))
    {
      if (command.equals("admin_mp on"))
      {
        activeChar.sendAdmResultMessage("command not working");
      }
      else if (command.equals("admin_mp off"))
      {
        activeChar.sendAdmResultMessage("command not working");
      }
      else if (command.equals("admin_mp dump"))
      {
        activeChar.sendAdmResultMessage("command not working");
      }
      else if (command.equals("admin_known on"))
      {
        Config.CHECK_KNOWN = true;
      }
      else if (command.equals("admin_known off"))
      {
        Config.CHECK_KNOWN = false;
      }
      else if (command.equals("admin_reconls"))
        LoginServerThread.getInstance().reConnect(); 
    }
    return true;
  }

  private void adminTestSkill(L2PcInstance activeChar, int id)
  {
    L2Object target = activeChar.getTarget();
    L2Character player;
    L2Character player;
    if ((target == null) || (!target.isL2Character()))
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