package l2r.gameserver.handler.voicecommands.impl;

import l2r.gameserver.Config;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.scripts.Functions;

public class Online extends Functions  implements IVoicedCommandHandler
{
  private final String[] _commandList = { "online" };

  public String[] getVoicedCommandList()
  {
    return this._commandList;
  }

  public boolean useVoicedCommand(String command, Player activeChar, String target)
  {
    if ((!Config.ALLOW_TOTAL_ONLINE))
      return false;
    if (command.equals("online"))
    {
      int i = 0;
      int j = 0;
      for (Player player : GameObjectsStorage.getAllPlayersForIterate())
      {
        i++;
        if (player.isInOfflineMode())
          j++;
      }
      {
        activeChar.sendMessage("На сервере играют " + i + " игроков.");
        activeChar.sendMessage("Из них " + j + " находятся в оффлайн торге.");
      }
      return true;
    }
    return false;
  }
}