package scripts.commands;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public abstract interface IUserCommandHandler
{
  public abstract boolean useUserCommand(int paramInt, L2PcInstance paramL2PcInstance);

  public abstract int[] getUserCommandList();
}