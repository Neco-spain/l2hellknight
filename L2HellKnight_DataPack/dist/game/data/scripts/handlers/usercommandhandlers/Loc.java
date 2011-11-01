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

import l2.hellknight.gameserver.handler.IUserCommandHandler;
import l2.hellknight.gameserver.instancemanager.MapRegionManager;
import l2.hellknight.gameserver.instancemanager.ZoneManager;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.base.Race;
import l2.hellknight.gameserver.model.zone.type.L2RespawnZone;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;

public class Loc implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		0
	};
	
	/**
	 * 
	 * @see l2.hellknight.gameserver.handler.IUserCommandHandler#useUserCommand(int, l2.hellknight.gameserver.model.actor.instance.L2PcInstance)
	 */
	public boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		int region;
		L2RespawnZone zone = ZoneManager.getInstance().getZone(activeChar, L2RespawnZone.class);
		
		if (zone != null)
			region = MapRegionManager.getInstance().getRestartRegion(activeChar, zone.getAllRespawnPoints().get(Race.Human)).getLocId();
		else
			region = MapRegionManager.getInstance().getMapRegionLocId(activeChar);
		
		SystemMessage sm = SystemMessage.getSystemMessage(region);
		if(sm.getSystemMessageId().getParamCount() == 3)
		{
			sm.addNumber(activeChar.getX());
			sm.addNumber(activeChar.getY());
			sm.addNumber(activeChar.getZ());
		}
		activeChar.sendPacket(sm);
		return true;
	}
	
	/**
	 * 
	 * @see l2.hellknight.gameserver.handler.IUserCommandHandler#getUserCommandList()
	 */
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}
