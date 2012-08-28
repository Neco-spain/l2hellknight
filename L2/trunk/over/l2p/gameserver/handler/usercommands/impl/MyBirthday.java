package l2p.gameserver.handler.usercommands.impl;

import java.util.Calendar;
import l2p.gameserver.handler.usercommands.IUserCommandHandler;
import l2p.gameserver.model.Player;
import l2p.gameserver.serverpackets.SystemMessage2;
import l2p.gameserver.serverpackets.components.SystemMsg;

public class MyBirthday
  implements IUserCommandHandler
{
  private static final int[] COMMAND_IDS = { 126 };

  public boolean useUserCommand(int id, Player activeChar)
  {
    if (activeChar.getCreateTime() == 0L) {
      return false;
    }
    Calendar c = Calendar.getInstance();
    c.setTimeInMillis(activeChar.getCreateTime());

    activeChar.sendPacket(((SystemMessage2)((SystemMessage2)((SystemMessage2)new SystemMessage2(SystemMsg.C1S_BIRTHDAY_IS_S3S4S2).addName(activeChar)).addInteger(c.get(1))).addInteger(c.get(2) + 1)).addInteger(c.get(5)));

    if ((c.get(2) == 1) && (c.get(7) == 29))
      activeChar.sendPacket(SystemMsg.A_CHARACTER_BORN_ON_FEBRUARY_29_WILL_RECEIVE_A_GIFT_ON_FEBRUARY_28);
    return true;
  }

  public final int[] getUserCommandList()
  {
    return COMMAND_IDS;
  }
}