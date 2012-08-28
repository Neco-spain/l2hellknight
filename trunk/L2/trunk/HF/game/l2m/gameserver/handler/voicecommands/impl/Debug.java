package l2m.gameserver.handler.voicecommands.impl;

import l2m.gameserver.Config;
import l2m.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2m.gameserver.model.Player;
import l2m.gameserver.network.serverpackets.components.CustomMessage;

public class Debug
  implements IVoicedCommandHandler
{
  private final String[] _commandList = { "debug" };

  public String[] getVoicedCommandList()
  {
    return _commandList;
  }

  public boolean useVoicedCommand(String command, Player player, String args)
  {
    if (!Config.ALT_DEBUG_ENABLED) {
      return false;
    }
    if (player.isDebug())
    {
      player.setDebug(false);
      player.sendMessage(new CustomMessage("voicedcommandhandlers.Debug.Disabled", player, new Object[0]));
    }
    else
    {
      player.setDebug(true);
      player.sendMessage(new CustomMessage("voicedcommandhandlers.Debug.Enabled", player, new Object[0]));
    }
    return true;
  }
}