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
package com.l2js.gameserver.network.loginserverpackets;

import com.l2js.util.network.BaseRecievePacket;

/**
 * @author L0ngh0rn
 */
public class BanInfo extends BaseRecievePacket
{
	public enum BanStatus
	{
		BAN_UNSUCCESSFUL, BAN_SUCCESSFUL, ALREADY_BANNED, UNBAN_SUCCESSFUL, UNBAN_UNSUCCESSFUL, UNKNOWN
	}

	private final String _address;
	private final BanStatus _status;

	public BanInfo(byte[] decrypt)
	{
		super(decrypt);

		_address = readS();
		int tmp = readD();

		if (tmp < 0 || tmp > BanStatus.values().length - 1)
			tmp = 5;

		_status = BanStatus.values()[tmp];
	}

	public String getAddress()
	{
		return _address;
	}

	public BanStatus getStatus()
	{
		return _status;
	}
}
