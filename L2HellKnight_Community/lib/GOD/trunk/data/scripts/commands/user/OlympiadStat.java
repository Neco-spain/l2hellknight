package commands.user;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.handler.IUserCommandHandler;
import l2rt.gameserver.handler.UserCommandHandler;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.entity.olympiad.Olympiad;
import l2rt.gameserver.network.serverpackets.SystemMessage;

/**
 * Support for /olympiadstat command
 */
public class OlympiadStat implements IUserCommandHandler, ScriptFile
{
	private static final int[] COMMAND_IDS = { 109 };

	public boolean useUserCommand(int id, L2Player activeChar)
	{
		if(id != COMMAND_IDS[0])
			return false;

		if(!activeChar.isNoble())
			activeChar.sendPacket(Msg.THIS_COMMAND_CAN_ONLY_BE_USED_BY_A_NOBLESSE);
		else
		{
			SystemMessage sm = new SystemMessage(SystemMessage.THE_CURRENT_FOR_THIS_OLYMPIAD_IS_S1_WINS_S2_DEFEATS_S3_YOU_HAVE_EARNED_S4_OLYMPIAD_POINTS);
			sm.addNumber(Olympiad.getCompetitionDone(activeChar.getObjectId()));
			sm.addNumber(Olympiad.getCompetitionWin(activeChar.getObjectId()));
			sm.addNumber(Olympiad.getCompetitionLoose(activeChar.getObjectId()));
			sm.addNumber(Olympiad.getNoblePoints(activeChar.getObjectId()));
			activeChar.sendPacket(sm);
		}
		return true;
	}

	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}

	public void onLoad()
	{
		UserCommandHandler.getInstance().registerUserCommandHandler(this);
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}