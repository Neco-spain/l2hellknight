package l2rt.loginserver.clientpackets;

import l2rt.Config;
import l2rt.loginserver.LoginController;
import l2rt.loginserver.SessionKey;
import l2rt.loginserver.serverpackets.LoginFail.LoginFailReason;
import l2rt.loginserver.serverpackets.PlayOk;

/**
 * Fromat is ddc
 * d: first part of session id
 * d: second part of session id
 * c: server ID
 */
public class RequestServerLogin extends L2LoginClientPacket
{
	private int _skey1;
	private int _skey2;
	private int _serverId;

	public int getSessionKey1()
	{
		return _skey1;
	}

	public int getSessionKey2()
	{
		return _skey2;
	}

	public int getServerID()
	{
		return _serverId;
	}

	@Override
	public boolean readImpl()
	{
		if(getAvaliableBytes() >= 9)
		{
			_skey1 = readD();
			_skey2 = readD();
			_serverId = readC();
			return true;
		}
		return false;
	}

	/**
	 * @see l2rt.extensions.network.ReceivablePacket#run()
	 */
	@Override
	public void runImpl()
	{
		SessionKey sk = getClient().getSessionKey();

		// if we didnt showed the license we cant check these values
		if(!Config.SHOW_LICENCE || sk.checkLoginPair(_skey1, _skey2))
		{
			if(LoginController.getInstance().isLoginPossible(getClient(), _serverId))
				getClient().sendPacket(new PlayOk(sk));
			else
				getClient().close(LoginFailReason.REASON_ACCESS_FAILED);
		}
		else
			getClient().close(LoginFailReason.REASON_ACCESS_FAILED);
	}
}