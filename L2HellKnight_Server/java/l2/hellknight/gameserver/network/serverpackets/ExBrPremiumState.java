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


/**
 * format: dc
 * @author  GodKratos
 */
public class ExBrPremiumState extends L2GameServerPacket
{
	private static final String _S__FE_D9_EXBRPREMIUMSTATE = "[S] FE:D9 ExBrPremiumState";
	private final int _objId;
	private final int _state;
	
	public ExBrPremiumState(int id, int state)
	{
		_objId = id;
		_state = state;
	}
	
	/**
	 * @see l2.hellknight.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xD9);
		writeD(_objId);
		writeC(_state);
	}
	
	/**
	 * @see l2.hellknight.gameserver.network.serverpackets.L2GameServerPacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__FE_D9_EXBRPREMIUMSTATE;
	}
}
