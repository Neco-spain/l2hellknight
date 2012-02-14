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
package l2rt.gameserver.network.serverpackets;

/**
 * 
 * @author mrTJO
 */
public class Ex2ndPasswordAck extends L2GameServerPacket
{
	
	private static final String _S__FE_10B_EX2NDPASSWORDACKPACKET = "[S] FE:10B Ex2NDPasswordAckPacket";
	int _response;
	
	public static int SUCCESS = 0x00;		// Пароль успешно создан
	public static int WRONG_PATTERN = 0x01; // Невозможно создать пароль (повторяютяс символы)
	public static int PASSWORD_BAN = 0x02; // Вы 5 раз неправильно ввели пароль, получили бан 
	
	public Ex2ndPasswordAck(int response)
	{
		_response = response;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x10B);
		
		//2045827B   PUSH Engine.205A3DA0                      ASCII "cdd"
		writeC(0x00);
		writeD(_response);
		writeD(0x00);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_10B_EX2NDPASSWORDACKPACKET;
	}
}
