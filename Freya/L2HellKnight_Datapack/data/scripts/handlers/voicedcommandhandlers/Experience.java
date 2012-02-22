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
package handlers.voicedcommandhandlers;

import l2.hellknight.Config;
import l2.hellknight.gameserver.handler.IVoicedCommandHandler;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;

public class Experience implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"expon",
		"expoff"
	};
	
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (!Config.ENABLE_BLOCK_EXP)
		{
			activeChar.sendMessage("Command Disabled");
				return false;
		}
		
		if (command.equalsIgnoreCase("expon"))
		{
			activeChar.setGainXp(true);
			activeChar.sendMessage("Experience Gain: Enabled");
			activeChar.sendMessage("Skill Point Gain: Enabled");
		}
		else if (command.equalsIgnoreCase("expoff"))
		{
			activeChar.setGainXp(false);
			activeChar.sendMessage("Experience Gain: Disabled");
			activeChar.sendMessage("Skill Point Gain: Disabled");
		}
		return true;
	}
	
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}