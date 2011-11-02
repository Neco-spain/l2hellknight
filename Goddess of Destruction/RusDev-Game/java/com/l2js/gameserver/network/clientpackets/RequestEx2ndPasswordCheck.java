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
package com.l2js.gameserver.network.clientpackets;

import com.l2js.Config;
import com.l2js.gameserver.network.serverpackets.Ex2ndPasswordCheck;

/**
 * Format: (ch)
 * 
 * @author mrTJO
 */
public class RequestEx2ndPasswordCheck extends L2GameClientPacket
{
	private static final String _C__D0_AD_REQUESTEX2NDPASSWORDCHECK = "[C] D0:AD RequestEx2ndPasswordCheck";
	//private static Logger _log = Logger.getLogger(RequestEx2ndPasswordCheck.class.getName());
	
	@Override
	protected void readImpl()
	{
		
	}
	
	@Override
	protected void runImpl()
	{
		if (!Config.SECOND_AUTH_ENABLED || getClient().getSecondaryAuth().isAuthed())
		{
			sendPacket(new Ex2ndPasswordCheck(Ex2ndPasswordCheck.PASSWORD_OK));
			return;
		}
		
		getClient().getSecondaryAuth().openDialog();
	}

	@Override
	public String getType()
	{
		return _C__D0_AD_REQUESTEX2NDPASSWORDCHECK;
	}
}
