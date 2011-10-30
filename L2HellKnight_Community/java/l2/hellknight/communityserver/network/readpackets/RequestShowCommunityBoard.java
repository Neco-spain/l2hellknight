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
package l2.hellknight.communityserver.network.readpackets;

import org.netcon.BaseReadPacket;

import l2.hellknight.communityserver.network.GameServerThread;

public final class RequestShowCommunityBoard extends BaseReadPacket
{
	private final GameServerThread _gst;
	
	public RequestShowCommunityBoard(final byte[] data, final GameServerThread gst)
	{
		super(data);
		_gst = gst;
	}
	
	@Override
	public final void run()
	{
		final int playerObjId = super.readD();
		final String cmd = super.readS();
		
		_gst.getCommunityBoardManager().parseCmd(playerObjId, cmd);
	}
}
