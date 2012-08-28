package l2p.gameserver.handler.usercommands;

import l2p.gameserver.model.Player;

public abstract interface IUserCommandHandler
{
  public abstract boolean useUserCommand(int paramInt, Player paramPlayer);

  public abstract int[] getUserCommandList();
}