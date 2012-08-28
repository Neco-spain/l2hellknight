package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.instancemanager.QuestManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class AdminQuest
  implements IAdminCommandHandler
{
  private static final int REQUIRED_LEVEL = Config.GM_TEST;
  private static final String[] ADMIN_COMMANDS = { "admin_quest_reload" };

  public boolean useAdminCommand(String command, L2PcInstance activeChar)
  {
    if (activeChar == null) return false;

    if ((!Config.ALT_PRIVILEGES_ADMIN) && 
      (activeChar.getAccessLevel() < REQUIRED_LEVEL)) return false;

    if (command.startsWith("admin_quest_reload"))
    {
      String[] parts = command.split(" ");
      if (parts.length < 2)
      {
        activeChar.sendMessage("Syntax: //quest_reload <questFolder>.<questSubFolders...>.questName> or //quest_reload <id>");
      }
      else
      {
        try
        {
          int questId = Integer.parseInt(parts[1]);
          if (QuestManager.getInstance().reload(questId))
          {
            activeChar.sendMessage("Quest Reloaded Successfully.");
          }
          else
          {
            activeChar.sendMessage("Quest Reloaded Failed");
          }
        }
        catch (NumberFormatException e)
        {
          if (QuestManager.getInstance().reload(parts[1]))
          {
            activeChar.sendMessage("Quest Reloaded Successfully.");
          }
          else
          {
            activeChar.sendMessage("Quest Reloaded Failed");
          }
        }
      }
    }
    return true;
  }

  public String[] getAdminCommandList()
  {
    return ADMIN_COMMANDS;
  }
}