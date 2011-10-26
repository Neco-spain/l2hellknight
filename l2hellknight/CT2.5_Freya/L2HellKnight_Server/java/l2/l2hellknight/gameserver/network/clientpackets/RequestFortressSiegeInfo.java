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
package l2.hellknight.gameserver.network.clientpackets;

import l2.hellknight.gameserver.instancemanager.FortManager;
import l2.hellknight.gameserver.model.entity.Fort;
import l2.hellknight.gameserver.network.L2GameClient;
import l2.hellknight.gameserver.network.serverpackets.ExShowFortressSiegeInfo;

/**
 *
 * @author  KenM
 */
public class RequestFortressSiegeInfo extends L2GameClientPacket
{
	
	/**
	 * @see l2.hellknight.gameserver.network.clientpackets.L2GameClientPacket#getType()
	 */
	@Override
	public String getType()
	{
		return "[C] D0:42 RequestFortressSiegeInfo";
	}
	
	/**
	 * @see l2.hellknight.gameserver.network.clientpackets.L2GameClientPacket#readImpl()
	 */
	@Override
	protected void readImpl()
	{
		// trigger
	}
	
	/**
	 * @see l2.hellknight.gameserver.network.clientpackets.L2GameClientPacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		L2GameClient client = this.getClient();
		if (client != null)
		{
			for (Fort fort : FortManager.getInstance().getForts())
			{
				if (fort != null && fort.getSiege().getIsInProgress())
				{
					client.sendPacket(new ExShowFortressSiegeInfo(fort));
				}
			}
		}
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
