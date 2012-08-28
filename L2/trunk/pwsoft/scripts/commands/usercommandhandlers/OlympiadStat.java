package scripts.commands.usercommandhandlers;

import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.olympiad.Olympiad;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import scripts.commands.IUserCommandHandler;

public class OlympiadStat
  implements IUserCommandHandler
{
  private static final int[] COMMAND_IDS = { 109 };

  public boolean useUserCommand(int id, L2PcInstance activeChar)
  {
    if (id != COMMAND_IDS[0]) {
      return false;
    }
    if (!activeChar.isNoble())
      activeChar.sendPacket(Static.THIS_COMMAND_CAN_ONLY_BE_USED_BY_A_NOBLESSE);
    else {
      activeChar.sendPacket(SystemMessage.id(SystemMessageId.THE_PRESENT_RECORD_DURING_THE_CURRENT_OLYMPIAD_SESSION_IS_S1_WINS_S2_DEFEATS_YOU_HAVE_EARNED_S3_OLYMPIAD_POINTS).addNumber(Olympiad.getCompetitionWin(activeChar.getObjectId())).addNumber(Olympiad.getCompetitionLoose(activeChar.getObjectId())).addNumber(Olympiad.getNoblePoints(activeChar.getObjectId())));
    }
    return true;
  }

  public int[] getUserCommandList()
  {
    return COMMAND_IDS;
  }
}