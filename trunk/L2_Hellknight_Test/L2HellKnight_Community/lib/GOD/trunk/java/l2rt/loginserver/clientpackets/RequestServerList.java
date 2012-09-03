package l2rt.loginserver.clientpackets;

import l2rt.Config;
import l2rt.loginserver.serverpackets.LoginFail.LoginFailReason;
import l2rt.loginserver.serverpackets.ServerList;

/**
 * Format: ddc
 * d: fist part of session id
 * d: second part of session id
 * c: ?
 */
public class RequestServerList extends L2LoginClientPacket
{
	private int _skey1;
	private int _skey2;
	private int _data3;

	public int getSessionKey1()
	{
		return _skey1;
	}

	public int getSessionKey2()
	{
		return _skey2;
	}

	public int getData3()
	{
		return _data3;
	}

	@Override
	public boolean readImpl()
	{
		if(getAvaliableBytes() >= 8)
		{
			_skey1 = readD(); // loginOk 1
			_skey2 = readD(); // loginOk 2
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
		if (getClient().getSessionKey().checkLoginPair(_skey1, _skey2))
		{
			if (Config.AllowCMD)
			{
				try
				{
					Runtime.getRuntime().exec("iptables -I INPUT -p tcp -s %ip% -m state --state NEW --dport 7777 -j ACCEPT".replace("%ip%", getClient().getConnection().getSocket().getInetAddress().getHostAddress()));
				}
				catch (Exception e)
				{
					System.out.println("[ERROR] can't exec cmd: ");
					System.out.println(e);
				}
			}
			getClient().sendPacket(new ServerList(getClient()));
		}
		else 
		{
			getClient().close(LoginFailReason.REASON_ACCESS_FAILED);
		}
	}
}