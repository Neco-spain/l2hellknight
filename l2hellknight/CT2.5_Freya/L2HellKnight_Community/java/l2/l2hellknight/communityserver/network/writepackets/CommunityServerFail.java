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
package l2.hellknight.communityserver.network.writepackets;

import l2.hellknight.communityserver.network.netcon.BaseWritePacket;

public final class CommunityServerFail extends BaseWritePacket
{
	public static final byte REASON_IP_BANNED = 1;
	public static final byte REASON_IP_RESERVED = 2;
	public static final byte REASON_WRONG_HEXID = 3;
	public static final byte REASON_ID_RESERVED = 4;
	public static final byte REASON_NO_FREE_ID = 5;
	public static final byte NOT_AUTHED = 6;
	public static final byte REASON_ALREADY_LOGGED_IN = 7;
	
	public CommunityServerFail(final int reason)
	{
		writeC(0x00);
		writeC(0x02);
		writeC(reason);
	}
}