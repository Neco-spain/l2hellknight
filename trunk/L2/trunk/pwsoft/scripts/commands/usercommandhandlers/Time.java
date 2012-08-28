package scripts.commands.usercommandhandlers;

import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import scripts.commands.IUserCommandHandler;

public class Time
  implements IUserCommandHandler
{
  private static final int[] COMMAND_IDS = { 77 };

  public boolean useUserCommand(int id, L2PcInstance activeChar)
  {
    if (COMMAND_IDS[0] != id) return false;

    int t = GameTimeController.getInstance().getGameTime();
    String h = "" + t / 60 % 24;
    String m;
    String m;
    if (t % 60 < 10)
      m = "0" + t % 60;
    else {
      m = "" + t % 60;
    }
    if (GameTimeController.getInstance().isNowNight())
      activeChar.sendPacket(SystemMessage.id(SystemMessageId.TIME_S1_S2_IN_THE_NIGHT).addString(h).addString(m));
    else {
      activeChar.sendPacket(SystemMessage.id(SystemMessageId.TIME_S1_S2_IN_THE_DAY).addString(h).addString(m));
    }
    return true;
  }

  public int[] getUserCommandList() {
    return COMMAND_IDS;
  }
}