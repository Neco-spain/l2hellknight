package scripts.commands.voicedcommandhandlers;

import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.GMViewCharacterInfo;
import scripts.commands.IVoicedCommandHandler;

public class ModSpecial
  implements IVoicedCommandHandler
{
  private static final String[] VOICED_COMMANDS = { "showstat" };

  public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
  {
    if ((activeChar.isModerator()) && (activeChar.getModerRank() <= 2)) {
      String[] cmdParams = command.split(" ");

      String name = cmdParams[1];

      L2PcInstance targetPlayer = L2World.getInstance().getPlayer(name);

      if (targetPlayer == null) {
        activeChar.sendModerResultMessage("\u0418\u0433\u0440\u043E\u043A \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D \u0438\u043B\u0438 \u043E\u0448\u0438\u0431\u043A\u0430 \u043F\u0440\u0438 \u043D\u0430\u0431\u043E\u0440\u0435 \u043D\u0438\u043A\u0430");
        return false;
      }

      if (command.startsWith("showstat")) {
        activeChar.sendModerResultMessage("\u041F\u0440\u043E\u0441\u043C\u043E\u0442\u0440 \u0441\u0442\u0430\u0442\u043E\u0432 " + targetPlayer.getName());
        activeChar.sendPacket(new GMViewCharacterInfo(targetPlayer));
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