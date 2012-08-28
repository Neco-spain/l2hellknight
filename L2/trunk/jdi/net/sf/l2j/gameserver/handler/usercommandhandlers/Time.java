package net.sf.l2j.gameserver.handler.usercommandhandlers;

import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

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
    else
      m = "" + t % 60;
    SystemMessage sm;
    if (GameTimeController.getInstance().isNowNight()) {
      SystemMessage sm = new SystemMessage(SystemMessageId.TIME_S1_S2_IN_THE_NIGHT);
      sm.addString(h);
      sm.addString(m);
    }
    else {
      sm = new SystemMessage(SystemMessageId.TIME_S1_S2_IN_THE_DAY);
      sm.addString(h);
      sm.addString(m);
    }
    activeChar.sendPacket(sm);
    return true;
  }

  public int[] getUserCommandList() {
    return COMMAND_IDS;
  }
}