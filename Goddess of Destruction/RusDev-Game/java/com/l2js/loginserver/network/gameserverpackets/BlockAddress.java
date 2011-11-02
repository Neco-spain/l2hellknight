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
package com.l2js.loginserver.network.gameserverpackets;

import com.l2js.util.network.BaseRecievePacket;

/**
 * @author L0ngh0rn
 */
public class BlockAddress extends BaseRecievePacket
{
	private long _expiration;
	private String _address;

	public BlockAddress(byte[] decrypt)
	{
		super(decrypt);
		_expiration = readQ();
		_address = readS();
	}

	public long getExpiration()
	{
		return _expiration;
	}

	public String getAddress()
	{
		return _address;
	}
}
