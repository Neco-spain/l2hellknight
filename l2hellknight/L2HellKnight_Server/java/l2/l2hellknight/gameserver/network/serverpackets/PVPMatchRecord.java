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

import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.entity.underground_coliseum.UCTeam;

/**
 * This packet represents a dialog shown after the Underground
 * Coliseum's PVP match. The dialog is restricted to 9 players
 * per team AND 9 kills/deaths MAX per player.<BR>
 * You can pass a team of even hundred players, but then only
 * the first nine (of each team) will be shown.<BR>
 * The first record in each team's array will be shown as the
 * team's leader <I>(you can't change that without changing the
 * array)</I>.<BR>
 * Sort by name always works properly.<BR>
 * If a player has more than 9 kills or deaths, it will always
 * be at the end of the list when sorting by kills or deaths.<BR>
 * @author savormix
 */
public class PVPMatchRecord extends L2GameServerPacket
{
	private static final String _S__FE_7E_PVPMatchRecord = "[S] FE:7E PVPMatchRecord";
	
	private final UCTeam _t1;
	private final UCTeam _t2;
	private final boolean _winner;
	
	/**
	 * @param team1 Team 1's player records
	 * @param team2 Team 2's player records
	 * @param t1wins UNK
	 */
	public PVPMatchRecord(UCTeam team1, UCTeam team2, boolean t1wins)
	{
		_t1 = team1;
		_t2 = team2;
		_winner = t1wins;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x7e);
		
		writeD(0x02); // team count?
		writeD(_winner ? 0x01 : 0x02); // winner team no?
		writeD(_winner ? 0x02 : 0x01); // loser team no?
		
		writeD(0x00); // ??
		writeD(0x00); // ??
		
		writeD(_t1.getParty().getMemberCount() + _t2.getParty().getMemberCount()); // total players
		for (L2PcInstance member : _t1.getParty().getPartyMembers())
		{
			if (member == null)
				continue;
			
			writeS(member.getName()); // player name
			writeD(member.getUCKills()); // kills
			writeD(member.getUCDeaths()); // deaths
		}
		for (L2PcInstance member : _t2.getParty().getPartyMembers())
		{
			if (member == null)
				continue;
			
			writeS(member.getName()); // player name
			writeD(member.getUCKills()); // kills
			writeD(member.getUCDeaths()); // deaths
		}
	}
	
	@Override
	public String getType()
	{
		return _S__FE_7E_PVPMatchRecord;
	}
}