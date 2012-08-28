package l2m.gameserver.handler.voicecommands.impl;

import l2m.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2m.gameserver.instancemanager.HellboundManager;
import l2m.gameserver.model.Player;
import l2m.gameserver.scripts.Functions;

public class Hellbound extends Functions
  implements IVoicedCommandHandler
{
  private final String[] _commandList = { "hellbound" };

  public String[] getVoicedCommandList()
  {
    return _commandList;
  }

  public boolean useVoicedCommand(String command, Player activeChar, String target)
  {
    if (command.equals("hellbound"))
    {
      activeChar.sendMessage("Hellbound level: " + HellboundManager.getHellboundLevel());
      activeChar.sendMessage("Confidence: " + HellboundManager.getConfidence());
    }
    return false;
  }
}