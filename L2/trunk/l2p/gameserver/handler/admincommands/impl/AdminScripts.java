package l2p.gameserver.handler.admincommands.impl;

import java.util.Map;
import l2p.gameserver.handler.admincommands.IAdminCommandHandler;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.quest.Quest;
import l2p.gameserver.model.quest.QuestState;
import l2p.gameserver.scripts.Scripts;

public class AdminScripts
  implements IAdminCommandHandler
{
  public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    if (!activeChar.isGM()) {
      return false;
    }
    switch (1.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminScripts$Commands[command.ordinal()])
    {
    case 1:
    case 2:
      if (wordList.length < 2)
        return false;
      String param = wordList[1];
      if (param.equalsIgnoreCase("all"))
      {
        activeChar.sendMessage("Scripts reload starting...");
        if (!Scripts.getInstance().reload())
          activeChar.sendMessage("Scripts reloaded with errors. Loaded " + Scripts.getInstance().getClasses().size() + " classes.");
        else
          activeChar.sendMessage("Scripts successfully reloaded. Loaded " + Scripts.getInstance().getClasses().size() + " classes.");
      }
      else if (!Scripts.getInstance().reload(param)) {
        activeChar.sendMessage("Script(s) reloaded with errors.");
      } else {
        activeChar.sendMessage("Script(s) successfully reloaded.");
      }break;
    case 3:
      if (wordList.length < 2)
        return false;
      String quest = wordList[1];
      if (!Scripts.getInstance().reload("quests/" + quest))
        activeChar.sendMessage("Quest \"" + quest + "\" reloaded with errors.");
      else
        activeChar.sendMessage("Quest \"" + quest + "\" successfully reloaded.");
      reloadQuestStates(activeChar);
    }

    return true;
  }

  private void reloadQuestStates(Player p)
  {
    for (QuestState qs : p.getAllQuestsStates()) {
      p.removeQuestState(qs.getQuest().getName());
    }
    Quest.restoreQuestStates(p);
  }

  public Enum[] getAdminCommandEnum()
  {
    return Commands.values();
  }

  private static enum Commands
  {
    admin_scripts_reload, 
    admin_sreload, 
    admin_sqreload;
  }
}