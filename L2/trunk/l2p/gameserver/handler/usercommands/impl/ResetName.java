package l2p.gameserver.handler.usercommands.impl;

import l2p.gameserver.handler.usercommands.IUserCommandHandler;
import l2p.gameserver.model.Player;

public class ResetName
  implements IUserCommandHandler
{
  private static final int[] COMMAND_IDS = { 117 };

  public boolean useUserCommand(int id, Player activeChar)
  {
    if (COMMAND_IDS[0] != id) {
      return false;
    }
    if (activeChar.getVar("oldtitle") != null)
    {
      activeChar.setTitleColor(16777079);
      activeChar.setTitle(activeChar.getVar("oldtitle"));
      activeChar.broadcastUserInfo(true);
      return true;
    }
    return false;
  }

  public final int[] getUserCommandList()
  {
    return COMMAND_IDS;
  }
}