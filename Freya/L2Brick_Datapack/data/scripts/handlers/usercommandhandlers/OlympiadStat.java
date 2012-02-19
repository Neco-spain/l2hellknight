/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.usercommandhandlers;

import l2.brick.gameserver.handler.IUserCommandHandler;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.olympiad.Olympiad;
import l2.brick.gameserver.network.SystemMessageId;
import l2.brick.gameserver.network.serverpackets.SystemMessage;

/**
 * Support for /olympiadstat command
 * Added by kamy
 */
public class OlympiadStat implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		109
	};
	
	/**
	 * 
	 * @see l2.brick.gameserver.handler.IUserCommandHandler#useUserCommand(int, l2.brick.gameserver.model.actor.instance.L2PcInstance)
	 */
	public boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		if (id != COMMAND_IDS[0])
			return false;
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_CURRENT_RECORD_FOR_THIS_OLYMPIAD_SESSION_IS_S1_MATCHES_S2_WINS_S3_DEFEATS_YOU_HAVE_EARNED_S4_OLYMPIAD_POINTS);
		sm.addNumber(Olympiad.getInstance().getCompetitionDone(activeChar.getObjectId()));
		sm.addNumber(Olympiad.getInstance().getCompetitionWon(activeChar.getObjectId()));
		sm.addNumber(Olympiad.getInstance().getCompetitionLost(activeChar.getObjectId()));
		sm.addNumber(Olympiad.getInstance().getNoblePoints(activeChar.getObjectId()));
		activeChar.sendPacket(sm);
		return true;
	}
	
	/**
	 * 
	 * @see l2.brick.gameserver.handler.IUserCommandHandler#getUserCommandList()
	 */
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}
