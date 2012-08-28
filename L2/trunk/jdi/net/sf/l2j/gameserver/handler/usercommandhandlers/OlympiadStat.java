package net.sf.l2j.gameserver.handler.usercommandhandlers;

import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class OlympiadStat
  implements IUserCommandHandler
{
  private static final int[] COMMAND_IDS = { 109 };

  public boolean useUserCommand(int id, L2PcInstance activeChar)
  {
    if (id != COMMAND_IDS[0]) return false;

    SystemMessage sm = SystemMessage.sendString("Your current record for this Grand Olympiad is " + Olympiad.getInstance().getCompetitionDone(activeChar.getObjectId()) + " match(s) played. You have earned " + Olympiad.getInstance().getNoblePoints(activeChar.getObjectId()) + " Olympiad Point(s)");
    activeChar.sendPacket(sm);
    return true;
  }

  public int[] getUserCommandList()
  {
    return COMMAND_IDS;
  }
}