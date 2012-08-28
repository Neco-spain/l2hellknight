package l2m.gameserver.handler.usercommands;

import l2m.gameserver.model.Player;

public abstract interface IUserCommandHandler
{
  public abstract boolean useUserCommand(int paramInt, Player paramPlayer);

  public abstract int[] getUserCommandList();
}