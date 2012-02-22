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

import java.util.Calendar;
import java.util.logging.Logger;

import l2.hellknight.gameserver.datatables.ClanTable;
import l2.hellknight.gameserver.instancemanager.CHSiegeManager;
import l2.hellknight.gameserver.model.L2Clan;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.entity.Castle;
import l2.hellknight.gameserver.model.entity.ClanHall;

public class SiegeInfo extends L2GameServerPacket
{
	private static final String _S__C9_SIEGEINFO = "[S] c9 SiegeInfo";
	private static Logger _log = Logger.getLogger(SiegeInfo.class.getName());
	private Castle _castle;
	private ClanHall _hall;
	
	public SiegeInfo(Castle castle)
	{
		_castle = castle;
	}
	
	public SiegeInfo(ClanHall hall)
	{
		_hall = hall;
	}
	
	@Override
	protected final void writeImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null) return;
		
		writeC(0xc9);
		if(_castle != null)
		{
			writeD(_castle.getCastleId());
			
			final int ownerId = _castle.getOwnerId();
			
			writeD(((ownerId == activeChar.getClanId()) && (activeChar.isClanLeader())) ? 0x01 : 0x00);
			writeD(ownerId);
			if (ownerId > 0)
			{
				L2Clan owner = ClanTable.getInstance().getClan(ownerId);
					if (owner != null)
					{
						writeS(owner.getName());        // Clan Name
						writeS(owner.getLeaderName());  // Clan Leader Name
						writeD(owner.getAllyId());      // Ally ID
						writeS(owner.getAllyName());    // Ally Name
					}
					else
						_log.warning("Null owner for castle: " + _castle.getName());
			}
			else
			{
				writeS("");  // Clan Name
				writeS("");     // Clan Leader Name
				writeD(0);      // Ally ID
				writeS("");     // Ally Name
				}
			
				writeD((int) (Calendar.getInstance().getTimeInMillis()/1000));
				writeD((int) (_castle.getSiege().getSiegeDate().getTimeInMillis()/1000));
				writeD(0x00); //number of choices?
		}
		else
		{
			writeD(_hall.getId());
			
			final int ownerId = _hall.getOwnerId();
			
			writeD(((ownerId == activeChar.getClanId()) && (activeChar.isClanLeader())) ? 0x01 : 0x00);
			writeD(ownerId);
			if (ownerId > 0)
			{
			L2Clan owner = ClanTable.getInstance().getClan(ownerId);
			if (owner != null)
			{
				writeS(owner.getName());        // Clan Name
				writeS(owner.getLeaderName());  // Clan Leader Name
				writeD(owner.getAllyId());      // Ally ID
				writeS(owner.getAllyName());    // Ally Name
			}
			else
				_log.warning("Null owner for siegable hall: " + _hall.getName());
			}
			else
			{
				writeS("");  // Clan Name
				writeS("");     // Clan Leader Name
				writeD(0);      // Ally ID
				writeS("");     // Ally Name
			}
		
			writeD((int) (Calendar.getInstance().getTimeInMillis()/1000));
			writeD((int) ((CHSiegeManager.getInstance().getSiegableHall(_hall.getId()).getNextSiegeTime())/1000));
			writeD(0x00); //number of choices?
		}
		
	}
	
	/* (non-Javadoc)
	 * @see l2.hellknight.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__C9_SIEGEINFO;
	}
	
}
