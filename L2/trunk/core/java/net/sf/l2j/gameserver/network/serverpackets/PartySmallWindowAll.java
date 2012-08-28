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
package net.sf.l2j.gameserver.network.serverpackets;

import java.util.List;

import javolution.util.FastList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 *
 *
 * sample
 * 63
 * 01 00 00 00  count
 *
 * c1 b2 e0 4a  object id
 * 54 00 75 00 65 00 73 00 64 00 61 00 79 00 00 00  name
 * 5a 01 00 00  hp
 * 5a 01 00 00  hp max
 * 89 00 00 00  mp
 * 89 00 00 00  mp max
 * 0e 00 00 00  level
 * 12 00 00 00  class
 * 00 00 00 00
 * 01 00 00 00
 *
 *
 * format   d (dSdddddddd)
 *
 *
 * @version $Revision: 1.6.2.1.2.5 $ $Date: 2005/03/27 15:29:57 $
 */
public class PartySmallWindowAll extends L2GameServerPacket
{
	private static final String _S__63_PARTYSMALLWINDOWALL = "[S] 4e PartySmallWindowAll";
	private List<L2PcInstance> _partyMembers = new FastList<L2PcInstance>();
	private L2PcInstance _player;
	private boolean _playerSelected = false;

	public void setPartyList(List<L2PcInstance> party)
	{
		_partyMembers = party;
	}
	public void setCurrentPlayer(L2PcInstance player)
	{
		_player = player;
		_playerSelected = true;
	}
	@Override
	protected final void writeImpl()
	{
		if (!_playerSelected)
		_player = getClient().getActiveChar();
		writeC(0x4e);
		writeD(_partyMembers.get(0).getObjectId()); // c3 party leader id
		writeD(_partyMembers.get(0).getParty().getLootDistribution());// c3
		// party
		// loot
		// type
		// (0,1,2,....)
		writeD(_partyMembers.size() - 1);
		for (int i = 0; i < _partyMembers.size(); i++)
		{
			L2PcInstance member = _partyMembers.get(i);
			if (!member.equals(_player))
			{
				writeD(member.getObjectId());
				writeS(member.getName());

				writeD((int) member.getCurrentCp()); //c4
				writeD(member.getMaxCp()); //c4

				writeD((int) member.getCurrentHp());
				writeD(member.getMaxHp());
				writeD((int) member.getCurrentMp());
				writeD(member.getMaxMp());
				writeD(member.getLevel());
				writeD(member.getClassId().getId());
				writeD(0); //writeD(0x01); ??
				writeD(0);
			}
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__63_PARTYSMALLWINDOWALL;
	}
}
