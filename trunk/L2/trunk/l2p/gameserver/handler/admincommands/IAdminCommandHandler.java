package l2p.gameserver.handler.admincommands;

import l2p.gameserver.model.Player;

public abstract interface IAdminCommandHandler
{
  public abstract boolean useAdminCommand(Enum paramEnum, String[] paramArrayOfString, String paramString, Player paramPlayer);

  public abstract Enum[] getAdminCommandEnum();
}