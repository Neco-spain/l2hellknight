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
package l2.hellknight.loginserver.network.gameserverpackets;

import l2.hellknight.loginserver.mail.MailSystem;
import l2.hellknight.util.network.BaseRecievePacket;

/**
 * @author mrTJO
 */
public class RequestSendMail extends BaseRecievePacket
{
	String _accountName, _mailId;
	String[] _args;
	
	/**
	 * @param decrypt
	 */
	public RequestSendMail(byte[] decrypt)
	{
		super(decrypt);
		_accountName = readS();
		_mailId = readS();
		int argNum = readC();
		_args = new String[argNum];
		for (int i = 0; i < argNum; i++)
		{
			_args[i] = readS();
		}
		
		MailSystem.getInstance().sendMail(_accountName, _mailId, _args);
	}
}