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
package l2.brick.gameserver.network.serverpackets;

import l2.brick.gameserver.model.actor.instance.L2PcInstance;


/**
 * @author Kerberos
 */
public class ExBrExtraUserInfo extends L2GameServerPacket
{
	private int _charObjId;
	private int _val;
	
	public ExBrExtraUserInfo(L2PcInstance player)
	{
		_charObjId = player.getObjectId();
		_val = player.getEventEffectId();
		_invisible = player.getAppearance().getInvisible();
	}
	
	@Override
	protected final void writeImpl()
	{
		
		writeC(0xfe);
		writeH(0xcf);
		writeD(_charObjId); //object ID of Player
		writeD(_val);		// event effect id
		//writeC(0x00);		// Event flag, added only if event is active
		
	}
	
	/* (non-Javadoc)
	 * @see l2.brick.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return "[S] FE:CF ExBrExtraUSerInfo".intern();
	}
}
