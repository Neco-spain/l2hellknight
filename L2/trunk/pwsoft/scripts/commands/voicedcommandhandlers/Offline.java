package scripts.commands.voicedcommandhandlers;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import scripts.commands.IVoicedCommandHandler;

public class Offline
  implements IVoicedCommandHandler
{
  private static final String[] VOICED_COMMANDS = { "offline" };

  public boolean useVoicedCommand(String command, L2PcInstance player, String target)
  {
    if (command.equalsIgnoreCase("offline"))
    {
      if ((player.isOutOfControl()) || (player.isParalyzed())) {
        return false;
      }
      if (player.underAttack()) {
        return false;
      }
      if (player.isInZonePeace())
      {
        if (player.getPrivateStoreType() != 0)
          player.setOfflineMode(true);
        else
          player.sendMessage("\u0421\u043D\u0430\u0447\u0430\u043B\u0430 \u0432\u044B\u0441\u0442\u0430\u0432\u044C\u0442\u0435 \u043F\u0440\u0435\u0434\u043C\u0435\u0442\u044B \u043D\u0430 \u043F\u0440\u043E\u0434\u0430\u0436\u0443/\u043F\u043E\u043A\u0443\u043F\u043A\u0443");
      }
      else
        player.sendMessage("\u0422\u043E\u043B\u044C\u043A\u043E \u0432 \u0431\u0435\u0437\u043E\u043F\u0430\u0441\u043D\u043E\u0439 \u0437\u043E\u043D\u0435");
    }
    return true;
  }

  public String[] getVoicedCommandList()
  {
    return VOICED_COMMANDS;
  }
}