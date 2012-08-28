package scripts.commands.voicedcommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import scripts.commands.IVoicedCommandHandler;

public class ModBanChat
  implements IVoicedCommandHandler
{
  private static final String[] VOICED_COMMANDS = { "banchat", "unbanchat" };

  public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
  {
    if (activeChar.isModerator()) {
      String name = "";
      String[] cmdParams = command.split(" ");
      try {
        name = cmdParams[1];
      } catch (Exception e) {
        name = "_npe";
      }

      if (name.equalsIgnoreCase("_npe")) {
        activeChar.sendModerResultMessage("\u041E\u0448\u0438\u0431\u043A\u0430 \u043F\u0440\u0438 \u043D\u0430\u0431\u043E\u0440\u0435 \u043D\u0438\u043A\u0430");
        return false;
      }

      L2PcInstance targetPlayer = L2World.getInstance().getPlayer(name);
      if (targetPlayer == null) {
        activeChar.sendModerResultMessage("\u0418\u0433\u0440\u043E\u043A \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D \u0438\u043B\u0438 \u043E\u0448\u0438\u0431\u043A\u0430 \u043F\u0440\u0438 \u043D\u0430\u0431\u043E\u0440\u0435 \u043D\u0438\u043A\u0430");
        return false;
      }

      String Moder = activeChar.getForumName();

      if (command.startsWith("banchat")) {
        long banLengthMins = Integer.parseInt(cmdParams[2]);

        if (targetPlayer.isChatBanned()) {
          activeChar.sendModerResultMessage("\u0427\u0430\u0442 " + targetPlayer.getName() + " \u0443\u0436\u0435 \u0437\u0430\u0431\u043B\u043E\u043A\u0438\u0440\u043E\u0432\u0430\u043D \u043A\u0435\u043C-\u0442\u043E \u0434\u0440\u0443\u0433\u0438\u043C");
          return false;
        }

        if ((targetPlayer.isModerator()) && (activeChar.getModerRank() > 1)) {
          return false;
        }

        if (banLengthMins > Config.MAX_BAN_CHAT) {
          banLengthMins = Config.MAX_BAN_CHAT;
        }

        long banLength = banLengthMins * 60L;

        activeChar.sendModerResultMessage("\u0427\u0430\u0442 " + targetPlayer.getName() + " \u0437\u0430\u0431\u043B\u043E\u043A\u0438\u0440\u043E\u0432\u0430\u043D \u043D\u0430 " + banLength + " \u0441\u0435\u043A\u0443\u043D\u0434. (" + banLengthMins + " \u043C\u0438\u043D\u0443\u0442)");
        targetPlayer.sendMessage("* * * * * * * * * * * * * *");
        targetPlayer.setChatBanned(true, banLength, "");
        targetPlayer.sendMessage("\u041D\u0430\u043A\u0430\u0437\u0430\u043B " + activeChar.getName() + "(" + Moder + ")");
        targetPlayer.sendMessage("\u0414\u043B\u044F \u043E\u0441\u043F\u0430\u0440\u0438\u0432\u0430\u043D\u0438\u044F, \u0440\u0430\u0441\u0442\u0435\u043D\u0438\u0442\u0435 \u0447\u0430\u0442 \u0438 \u0441\u0434\u0435\u043B\u0430\u0439\u0442\u0435 \u0441\u043A\u0440\u0438\u043D,");
        targetPlayer.sendMessage("\u0440\u0430\u0437\u0434\u0435\u043B \u043D\u0430 \u0444\u043E\u0440\u0443\u043C\u0435 - \u0410\u0440\u0431\u0438\u0442\u0440\u0430\u0436.");
        targetPlayer.sendMessage("* * * * * * * * * * * * * *");

        activeChar.logModerAction(Moder, "\u0411\u0430\u043D \u0447\u0430\u0442\u0430 " + targetPlayer.getName() + " \u043D\u0430 " + banLengthMins + " \u043C\u0438\u043D\u0443\u0442");
      } else if (command.startsWith("unbanchat")) {
        activeChar.sendModerResultMessage("\u0427\u0430\u0442 " + targetPlayer.getName() + " \u0440\u0430\u0437\u0431\u043B\u043E\u043A\u0438\u0440\u043E\u0432\u0430\u043D");
        targetPlayer.sendMessage("* * * * * * * * * * * * * *");
        targetPlayer.setChatBanned(false, 0L, "");
        targetPlayer.sendMessage("\u0410\u043C\u043D\u0438\u0441\u0442\u0438\u0440\u043E\u0432\u0430\u043B " + activeChar.getName() + "(" + Moder + ")");
        targetPlayer.sendMessage("* * * * * * * * * * * * * *");

        activeChar.logModerAction(Moder, "\u0421\u043D\u044F\u043B \u0431\u0430\u043D \u0447\u0430\u0442\u0430 " + targetPlayer.getName());
      }
      return true;
    }
    return false;
  }

  public String[] getVoicedCommandList()
  {
    return VOICED_COMMANDS;
  }
}