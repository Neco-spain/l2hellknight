/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2js.gameserver.handler.admincommandhandlers;

import com.l2js.gameserver.datatables.ClassBalanceTable;
import com.l2js.gameserver.datatables.ClassVsClassTable;
import com.l2js.gameserver.handler.IAdminCommandHandler;
import com.l2js.gameserver.model.actor.instance.L2PcInstance;
import com.l2js.gameserver.network.clientpackets.Say2;

/**
 * @author L0ngh0rn
 */
public class AdminL2jS implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
			"admin_reload_balance_class", "admin_reload_class_vs_class"
	};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
			return false;

		if (command.equals("admin_reload_balance_class"))
		{
			ClassBalanceTable.getInstance().loadClassBalance();
			activeChar.sendChatMessage(0, Say2.ALL, "SYS", "Class Balance Table reloaded.");
		}
		else if (command.equals("admin_reload_class_vs_class"))
		{
			ClassVsClassTable.getInstance().loadClassVsClass();
			activeChar.sendChatMessage(0, Say2.ALL, "SYS", "Class Vs Class Table reloaded.");
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
