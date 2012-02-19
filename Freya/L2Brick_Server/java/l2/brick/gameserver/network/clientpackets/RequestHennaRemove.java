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
package l2.brick.gameserver.network.clientpackets;

import l2.brick.gameserver.model.L2HennaInstance;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.network.SystemMessageId;
import l2.brick.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 *
 * @version $Revision$ $Date$
 */
public final class RequestHennaRemove extends L2GameClientPacket
{
	private static final String _C__BC_RequestHennaRemove = "[C] bc RequestHennaRemove";
	private int _symbolId;
	// format  cd
	
	/**
	 * packet type id 0xbb
	 * format:		cd
	 * @param decrypt
	 */
	@Override
	protected void readImpl()
	{
		_symbolId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("HennaRemove"))
			return;
		
		for (int i = 1; i <= 3; i++)
		{
			L2HennaInstance henna = activeChar.getHenna(i);
			if (henna != null && henna.getSymbolId() == _symbolId)
			{
				if (activeChar.getAdena() >= (henna.getPrice() / 5))
				{
					activeChar.removeHenna(i);
					break;
				}
				else
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see l2.brick.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__BC_RequestHennaRemove;
	}
}
