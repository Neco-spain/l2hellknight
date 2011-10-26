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

import java.util.logging.Logger;

import l2.hellknight.Config;
import l2.hellknight.gameserver.instancemanager.LeaderboardFisherman;
import l2.hellknight.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * Format: (ch)
 * just a trigger
 * @author  -Wooden-
 *
 */
public final class RequestExFishRanking extends L2GameClientPacket
{
	protected static final Logger _log = Logger.getLogger(RequestExFishRanking.class.getName());
	private static final String _C__D0_1F_REQUESTEXFISHRANKING = "[C] D0:1F RequestExFishRanking";
	
	@Override
	protected void readImpl()
	{
		// trigger
	}
	
	/**
	 * @see l2.hellknight.util.network.BaseRecievePacket.ClientBasePacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		if (Config.RANK_FISHERMAN_ENABLED)
    	{
			NpcHtmlMessage htm = new NpcHtmlMessage(0);
			htm.setHtml(LeaderboardFisherman.getInstance().showHtm(getClient().getActiveChar().getObjectId()));
			getClient().getActiveChar().sendPacket(htm);
   	}
    	else
    		_log.info("C5: RequestExFishRanking");
	}
	
	/**
	 * @see l2.hellknight.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__D0_1F_REQUESTEXFISHRANKING;
	}
	
}