package scripts.commands.voicedcommandhandlers;

import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import scripts.commands.IVoicedCommandHandler;

public class ModTitle
  implements IVoicedCommandHandler
{
  private static final String[] VOICED_COMMANDS = { "cleartitle" };

  public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
  {
    if (activeChar.isModerator())
    {
      String Moder = activeChar.getForumName();

      if (command.equalsIgnoreCase("cleartitle"))
      {
        L2Object mtarget = activeChar.getTarget();
        if (mtarget != null)
        {
          L2PcInstance targetPlayer = null;
          if (mtarget.isPlayer()) {
            targetPlayer = (L2PcInstance)mtarget;
          }
          else {
            activeChar.sendModerResultMessage("\u0422\u043E\u043B\u044C\u043A\u043E \u0438\u0433\u0440\u043E\u043A\u043E\u0432 \u0445_\u0425");
            return false;
          }

          String oldTitle = activeChar.getTitle();

          activeChar.sendModerResultMessage(targetPlayer.getName() + ": \u0442\u0438\u0442\u0443\u043B \u0443\u0434\u0430\u043B\u0435\u043D");
          targetPlayer.setTitle("-_-");
          targetPlayer.broadcastTitleInfo();
          targetPlayer.sendMessage("* * * * * * * * * * * * * *");
          targetPlayer.sendMessage("\u0422\u0438\u0442\u0443\u043B \u0443\u0434\u0430\u043B\u0438\u043B " + activeChar.getName() + "(" + Moder + ")");
          targetPlayer.sendMessage("* * * * * * * * * * * * * *");

          activeChar.logModerAction(Moder, "\u0423\u0434\u0430\u043B\u0438\u043B \u0442\u0438\u0442\u0443\u043B " + oldTitle + " \u0443 " + targetPlayer.getName());
        }
      }
    }
    return true;
  }

  public String[] getVoicedCommandList()
  {
    return VOICED_COMMANDS;
  }
}