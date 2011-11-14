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
package com.l2js.gameserver.network.serverpackets;

import com.l2js.gameserver.model.actor.instance.L2PcInstance;
import com.l2js.gameserver.util.Point3D;

public class ExMoveToLocationInAirShip extends L2GameServerPacket
{
	private int _charObjId;
	private int _airShipId;
	private Point3D _destination;
	private int _heading;
	
	/**
	 * @param actor
	 * @param destination
	 * @param origin
	 */
	public ExMoveToLocationInAirShip(L2PcInstance player)
	{
		_charObjId = player.getObjectId();
		_airShipId = player.getAirShip().getObjectId();
		_destination = player.getInVehiclePosition();
		_heading = player.getHeading();
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x6D);
		writeD(_charObjId);
		writeD(_airShipId);
		writeD(_destination.getX());
		writeD(_destination.getY());
		writeD(_destination.getZ());
		writeD(_heading);
	}

	@Override
	public String getType()
	{
		return "[S] 6D MoveToLocationInAirShip";
	}
}