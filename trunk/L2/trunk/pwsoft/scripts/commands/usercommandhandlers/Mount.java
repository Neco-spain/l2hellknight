package scripts.commands.usercommandhandlers;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import scripts.commands.IUserCommandHandler;

public class Mount
  implements IUserCommandHandler
{
  private static final int[] COMMAND_IDS = { 61 };

  public synchronized boolean useUserCommand(int id, L2PcInstance player)
  {
    if (id != COMMAND_IDS[0]) {
      return false;
    }

    player.tryMountPet(player.getPet());
    return true;
  }

  public int[] getUserCommandList()
  {
    return COMMAND_IDS;
  }
}