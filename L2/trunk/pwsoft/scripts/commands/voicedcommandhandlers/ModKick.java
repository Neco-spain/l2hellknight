package scripts.commands.voicedcommandhandlers;

import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import scripts.commands.IVoicedCommandHandler;

public class ModKick
  implements IVoicedCommandHandler
{
  private static final String[] VOICED_COMMANDS = { "kick" };

  public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
  {
    if (activeChar.isModerator())
    {
      String Moder = activeChar.getForumName();

      if (command.equalsIgnoreCase("kick"))
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

          if (targetPlayer.getPrivateStoreType() != 0)
          {
            activeChar.sendModerResultMessage(targetPlayer.getName() + " \u043A\u0438\u043A\u043D\u0443\u0442 \u0438\u0437 \u0438\u0433\u0440\u044B");
            targetPlayer.sendMessage("* * * * * * * * * * * * * *");
            targetPlayer.sendMessage("\u0412\u0430\u0441 \u043A\u0438\u043A\u043D\u0443\u043B " + activeChar.getName() + "(" + Moder + ")");
            targetPlayer.sendMessage("* * * * * * * * * * * * * *");
            targetPlayer.kick();

            activeChar.logModerAction(Moder, "\u041A\u0438\u043A\u043D\u0443\u043B " + targetPlayer.getName());
          }
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