package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.serverpackets.Ex2ndPasswordAck;

/**
 * (ch)cS{S}
 * c: change pass?
 * S: current password
 * S: new password
 */
public class RequestEx2ndPasswordReq extends L2GameClientPacket
{
	private int _changePass;
	private String _password, _newPassword;
	
	@Override
	protected void readImpl()
	{
		_changePass = readC();
		_password = readS();
		if(_changePass == 2)
			_newPassword = readS();
	}
	
	@Override
	protected void runImpl()
	{
		/* by L2jServer
		if(!Config.SECOND_AUTH_ENABLED)
			return;
		
		SecondaryPasswordAuth spa = getClient().getSecondaryAuth();
		boolean exVal = false;
		
		if (_changePass == 0 && !spa.passwordExist())
			exVal = spa.savePassword(_password);
		else if (_changePass == 2 && spa.passwordExist())
			exVal = spa.changePassword(_password, _newPassword);
		
		if (exVal)
			getClient().sendPacket(new Ex2ndPasswordAck(Ex2ndPasswordAck.SUCCESS));*/
	}
}
