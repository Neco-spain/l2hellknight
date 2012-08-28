package l2p.gameserver.handler.admincommands.impl;

import l2p.gameserver.handler.admincommands.IAdminCommandHandler;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.PlayerAccess;

public class AdminGm
  implements IAdminCommandHandler
{
  public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    if (Boolean.TRUE.booleanValue()) {
      return false;
    }
    if (!activeChar.getPlayerAccess().CanEditChar) {
      return false;
    }
    switch (1.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminGm$Commands[command.ordinal()])
    {
    case 1:
      handleGm(activeChar);
    }

    return true;
  }

  public Enum[] getAdminCommandEnum()
  {
    return Commands.values();
  }

  private void handleGm(Player activeChar)
  {
    if (activeChar.isGM())
    {
      activeChar.getPlayerAccess().IsGM = false;
      activeChar.sendMessage("You no longer have GM status.");
    }
    else
    {
      activeChar.getPlayerAccess().IsGM = true;
      activeChar.sendMessage("You have GM status now.");
    }
  }

  private static enum Commands
  {
    admin_gm;
  }
}