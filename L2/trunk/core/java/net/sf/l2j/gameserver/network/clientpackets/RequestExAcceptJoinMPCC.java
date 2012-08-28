/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.L2CommandChannel;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

/**
 * @author -Wooden-
 *
 */
public final class RequestExAcceptJoinMPCC extends L2GameClientPacket
{
	private static final String _C__D0_0E_REQUESTEXASKJOINMPCC = "[C] D0:0E RequestExAcceptJoinMPCC";
	private int _response;

	/**
	 * @param buf
	 * @param client
	 */
	@Override
	protected void readImpl()
	{
		_response = readD();
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
        if (player != null)
        {
	    	L2PcInstance requestor = player.getActiveRequester();
	    	SystemMessage sm;
			if (requestor == null)
			    return;

			if (_response == 1)
	        {
				boolean newCc = false;
				if(!requestor.getParty().isInCommandChannel())
				{
					new L2CommandChannel(requestor); // Create new CC
					sm = new SystemMessage(SystemMessageId.COMMAND_CHANNEL_FORMED);
					requestor.sendPacket(sm);
					newCc = true;
				}
				requestor.getParty().getCommandChannel().addParty(player.getParty());
				if (!newCc)
				{
					sm = new SystemMessage(SystemMessageId.JOINED_COMMAND_CHANNEL);
					player.sendPacket(sm);
				}
			} 
			
			else
	        {
				SystemMessage sms = new SystemMessage(SystemMessageId.S1_DECLINED_CHANNEL_INVITATION);
				sms.addString(player.getName());
				requestor.sendPacket(sms);
			}

			player.setActiveRequester(null);
			requestor.onTransactionResponse();
        }
	}
	

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__D0_0E_REQUESTEXASKJOINMPCC;
	}

}
