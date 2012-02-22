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

import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.network.serverpackets.PledgeReceiveWarList;


/**
 * Format: (ch) dd
 * @author  -Wooden-
 *
 */
public final class RequestPledgeWarList extends L2GameClientPacket
{
	protected static final Logger _log = Logger.getLogger(RequestPledgeWarList.class.getName());
	private static final String _C__D0_1E_REQUESTPLEDGEWARLIST = "[C] D0:1E RequestPledgeWarList";
	@SuppressWarnings("unused")
	private int _unk1;
	private int _tab;
	
	
	@Override
	protected void readImpl()
	{
		_unk1 = readD();
		_tab = readD();
	}
	
	/**
	 * @see l2.hellknight.util.network.BaseRecievePacket.ClientBasePacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		//_log.info("C5: RequestPledgeWarList d:"+_unk1);
		//_log.info("C5: RequestPledgeWarList d:"+_tab);
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if (activeChar.getClan() == null) return;
		
		//do we need powers to do that??
		activeChar.sendPacket(new PledgeReceiveWarList(activeChar.getClan(),_tab));
	}
	
	/**
	 * @see l2.hellknight.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__D0_1E_REQUESTPLEDGEWARLIST;
	}
	
}