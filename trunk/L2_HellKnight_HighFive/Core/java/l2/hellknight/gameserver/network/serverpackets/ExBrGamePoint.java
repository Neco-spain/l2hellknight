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
package l2.hellknight.gameserver.network.serverpackets;

import l2.hellknight.ExternalConfig;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;

public class ExBrGamePoint extends L2GameServerPacket
{
	private int _objId;
	private long _points;

	public ExBrGamePoint(L2PcInstance player)
	{
		_objId = player.getObjectId();

		if(ExternalConfig.GAME_POINT_ITEM_ID == -1)
		{
			_points = player.getgame_points();
		}
		else
		{
			_points = player.getInventory().getInventoryItemCount(ExternalConfig.GAME_POINT_ITEM_ID, -1);
		}
	}

	@Override
	public void writeImpl()
	{
		writeC(0xFE);
	 	writeH(0xD5);//writeH(0xB8);
		writeD(_objId);
		writeQ(_points);
		writeD(0x00);   //??
	}
}