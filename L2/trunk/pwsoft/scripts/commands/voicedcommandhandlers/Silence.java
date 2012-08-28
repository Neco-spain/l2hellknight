package scripts.commands.voicedcommandhandlers;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import scripts.commands.IVoicedCommandHandler;

public class Silence
  implements IVoicedCommandHandler
{
  private static final String[] VOICED_COMMANDS = { "silence", "unpartner" };

  public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
  {
    if (command.equalsIgnoreCase("silence")) {
      if (activeChar.isWorldIgnore()) {
        activeChar.setWorldIgnore(false);
        activeChar.sendMessage("\u0418\u0433\u043D\u043E\u0440 \u043C\u0438\u0440\u043E\u0432\u043E\u0433\u043E \u0447\u0430\u0442\u0430 \u0432\u044B\u043A\u043B\u044E\u0447\u0435\u043D");
      } else {
        activeChar.setWorldIgnore(true);
        activeChar.sendMessage("\u0418\u0433\u043D\u043E\u0440 \u043C\u0438\u0440\u043E\u0432\u043E\u0433\u043E \u0447\u0430\u0442\u0430 \u0432\u043A\u043B\u044E\u0447\u0435\u043D");
      }
    } else if (command.equalsIgnoreCase("unpartner")) {
      L2PcInstance _partner = activeChar.getPartner();
      if (_partner != null)
        try {
          _partner.despawnMe();
        }
        catch (Exception t) {
        }
    }
    return true;
  }

  public String[] getVoicedCommandList()
  {
    return VOICED_COMMANDS;
  }
}