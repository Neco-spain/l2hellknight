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

import l2.hellknight.gameserver.model.entity.underground_coliseum.UCArena;

/**
 * Should be sent just after PVPMatchRecord.<BR>
 * This packet specifies the main score shown in the dialog
 * opened by the mentioned packet, where _t1 is in Blue color
 * and _t2 is in orange.<BR>
 * What is important, it also newly decides the WIN/LOSE status,
 * so with a specifically crafted PVPMatchRecord you can have
 * such a dialog:<BR>
 * WIN 13:10 LWIN (LOSE is below WIN)
 * @author savormix
 */
public class ExPVPMatchUserDie extends L2GameServerPacket
{
	private static final String _S__FE_7F_EXPVPMATCHUSERDIE = "[S] FE:7F ExPVPMatchUserDie";
	
	private final int _t1;
	private final int _t2;
	
	public ExPVPMatchUserDie(UCArena a)
	{
		_t1 = a.getTeams()[0].getKillCount();
		_t2 = a.getTeams()[1].getKillCount();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x7e);
		
		writeD(_t1);
		writeD(_t2);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_7F_EXPVPMATCHUSERDIE;
	}
}