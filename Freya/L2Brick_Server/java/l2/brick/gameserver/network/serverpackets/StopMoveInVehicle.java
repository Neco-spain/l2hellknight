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
package l2.brick.gameserver.network.serverpackets;

import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.util.Point3D;

/**
 * @author Maktakien
 *
 */
public class StopMoveInVehicle  extends L2GameServerPacket
{
	private int _charObjId;
	private int _boatId;
	private Point3D _pos;
	private int _heading;
	
	public StopMoveInVehicle(L2PcInstance player, int boatId)
	{
		_charObjId = player.getObjectId();
		_boatId = boatId;
		_pos = player.getInVehiclePosition();
		_heading = player.getHeading();
	}
	
	/* (non-Javadoc)
	 * @see l2.brick.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0x7f);
		writeD(_charObjId);
		writeD(_boatId);
		writeD(_pos.getX());
		writeD(_pos.getY());
		writeD(_pos.getZ());
		writeD(_heading);
	}
	
	/* (non-Javadoc)
	 * @see l2.brick.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return "[S] 7f StopMoveInVehicle";
	}
}
