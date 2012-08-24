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

/**
 * @author mochitto
 *
 * Format: (ch)d
 * d: points - max 7200 is 100%
 */
public class ExNavitAdventPointInfoPacket extends L2GameServerPacket
{
	private final int _points;
	
	public ExNavitAdventPointInfoPacket(int points)
	{
		_points = points;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xDF);
		writeD(_points); // 72 = 1%
	}
}
