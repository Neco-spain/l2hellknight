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
package handlers.admincommandhandlers;

import l2.brick.gameserver.handler.IAdminCommandHandler;
import l2.brick.gameserver.model.L2Object;
import l2.brick.gameserver.model.actor.L2Character;
import l2.brick.gameserver.model.actor.instance.L2ControllableMobInstance;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.network.SystemMessageId;
import l2.brick.gameserver.network.clientpackets.Say2;
import l2.brick.gameserver.network.serverpackets.CreatureSay;

/**
 * This class handles following admin commands: - targetsay <message> = makes talk a L2Character
 * @author nonom
 */
public class AdminTargetSay implements IAdminCommandHandler
{
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_targetsay"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_targetsay"))
		{
			try
			{
				String message = command.substring(16);
				
				L2Object obj = activeChar.getTarget();
				
				if ((obj instanceof L2ControllableMobInstance) || !(obj instanceof L2Character))
				{
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
					return false;
				}
				else
				{
					talk(activeChar, (L2Character) obj, message);
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_SYNTAX);
				return false;
			}
		}
		return true;
	}
	
	private void talk(L2PcInstance activeChar, L2Character target, String message)
	{
		target.broadcastPacket(new CreatureSay(target.getObjectId(), Say2.ALL, target.getName(), message));
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
