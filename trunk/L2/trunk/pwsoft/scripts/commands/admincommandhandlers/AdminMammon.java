package scripts.commands.admincommandhandlers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.SevenSigns;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.model.AutoSpawnHandler;
import net.sf.l2j.gameserver.model.AutoSpawnHandler.AutoSpawnInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import scripts.commands.IAdminCommandHandler;

public class AdminMammon
  implements IAdminCommandHandler
{
  private static final String[] ADMIN_COMMANDS = { "admin_mammon_find", "admin_mammon_respawn", "admin_list_spawns", "admin_msg" };
  private static final int REQUIRED_LEVEL = Config.GM_MENU;

  private boolean _isSealValidation = SevenSigns.getInstance().isSealValidationPeriod();

  public boolean useAdminCommand(String command, L2PcInstance activeChar)
  {
    if ((!Config.ALT_PRIVILEGES_ADMIN) && (
      (!checkLevel(activeChar.getAccessLevel())) || (!activeChar.isGM()))) {
      return false;
    }
    int npcId = 0;
    int teleportIndex = -1;
    AutoSpawnHandler.AutoSpawnInstance blackSpawnInst = AutoSpawnHandler.getInstance().getAutoSpawnInstance(31126, false);

    AutoSpawnHandler.AutoSpawnInstance merchSpawnInst = AutoSpawnHandler.getInstance().getAutoSpawnInstance(31113, false);

    if (command.startsWith("admin_mammon_find"))
    {
      try
      {
        if (command.length() > 17) teleportIndex = Integer.parseInt(command.substring(18));
      }
      catch (Exception NumberFormatException)
      {
        activeChar.sendAdmResultMessage("Usage: //mammon_find [teleportIndex] (where 1 = Blacksmith, 2 = Merchant)");
      }

      if (!_isSealValidation)
      {
        activeChar.sendAdmResultMessage("The competition period is currently in effect.");
        return true;
      }
      if (blackSpawnInst != null)
      {
        L2NpcInstance[] blackInst = blackSpawnInst.getNPCInstanceList();
        if (blackInst.length > 0)
        {
          int x1 = blackInst[0].getX(); int y1 = blackInst[0].getY(); int z1 = blackInst[0].getZ();
          activeChar.sendAdmResultMessage("Blacksmith of Mammon: " + x1 + " " + y1 + " " + z1);
          if (teleportIndex == 1)
            activeChar.teleToLocation(x1, y1, z1, true);
        }
      }
      else {
        activeChar.sendAdmResultMessage("Blacksmith of Mammon isn't registered for spawn.");
      }if (merchSpawnInst != null)
      {
        L2NpcInstance[] merchInst = merchSpawnInst.getNPCInstanceList();
        if (merchInst.length > 0)
        {
          int x2 = merchInst[0].getX(); int y2 = merchInst[0].getY(); int z2 = merchInst[0].getZ();
          activeChar.sendAdmResultMessage("Merchant of Mammon: " + x2 + " " + y2 + " " + z2);
          if (teleportIndex == 2)
            activeChar.teleToLocation(x2, y2, z2, true);
        }
      }
      else {
        activeChar.sendAdmResultMessage("Merchant of Mammon isn't registered for spawn.");
      }
    }
    else if (command.startsWith("admin_mammon_respawn"))
    {
      if (!_isSealValidation)
      {
        activeChar.sendAdmResultMessage("The competition period is currently in effect.");
        return true;
      }
      if (merchSpawnInst != null)
      {
        long merchRespawn = AutoSpawnHandler.getInstance().getTimeToNextSpawn(merchSpawnInst);
        activeChar.sendAdmResultMessage("The Merchant of Mammon will respawn in " + merchRespawn / 60000L + " minute(s).");
      }
      else {
        activeChar.sendAdmResultMessage("Merchant of Mammon isn't registered for spawn.");
      }if (blackSpawnInst != null)
      {
        long blackRespawn = AutoSpawnHandler.getInstance().getTimeToNextSpawn(blackSpawnInst);
        activeChar.sendAdmResultMessage("The Blacksmith of Mammon will respawn in " + blackRespawn / 60000L + " minute(s).");
      }
      else {
        activeChar.sendAdmResultMessage("Blacksmith of Mammon isn't registered for spawn.");
      }
    }
    else if (command.startsWith("admin_list_spawns"))
    {
      try
      {
        String[] params = command.split(" ");
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher regexp = pattern.matcher(params[1]);
        if (regexp.matches()) {
          npcId = Integer.parseInt(params[1]);
        }
        else {
          params[1] = params[1].replace('_', ' ');
          npcId = NpcTable.getInstance().getTemplateByName(params[1]).npcId;
        }
        if (params.length > 2) teleportIndex = Integer.parseInt(params[2]);
      }
      catch (Exception e)
      {
        activeChar.sendPacket(SystemMessage.sendString("Command format is //list_spawns <npcId|npc_name> [tele_index]"));
      }

      SpawnTable.getInstance().findNPCInstances(activeChar, npcId, teleportIndex);
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
}