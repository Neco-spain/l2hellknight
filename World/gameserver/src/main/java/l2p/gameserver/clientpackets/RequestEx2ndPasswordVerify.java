package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;

/**
 * Format: (ch)S
 * S: numerical password
 */
public class RequestEx2ndPasswordVerify extends L2GameClientPacket
{
	private String _password;
	
	@Override
	protected void readImpl()
	{
		_password = readS();
	}
	
	@Override
	protected void runImpl()
	{
		/* by L2jServer
		if(!Config.SECOND_AUTH_ENABLED)
			return;
		
		getClient().getSecondaryAuth().checkPassword(_password, false);*/
	}
}
