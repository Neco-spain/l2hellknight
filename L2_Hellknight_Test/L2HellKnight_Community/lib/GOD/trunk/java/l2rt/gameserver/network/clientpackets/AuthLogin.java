package l2rt.gameserver.network.clientpackets;

import l2rt.Config;
import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.loginservercon.LSConnection;
import l2rt.gameserver.loginservercon.SessionKey;
import l2rt.gameserver.network.L2GameClient;

/**
 * cSddddd
 * cSdddddQ
 * loginName + keys must match what the loginserver used.
 */
public class AuthLogin extends L2GameClientPacket
{
	private String _loginName;
	private int _playKey1;
	private int _playKey2;
	private int _loginKey1;
	private int _loginKey2;
	private byte[] _data = new byte[48];
	
	@Override
	public void readImpl()
	{
		_loginName = readS(32).toLowerCase();
		_playKey2 = readD();
		_playKey1 = readD();
		_loginKey1 = readD();
		_loginKey2 = readD();
		ccpGuard.Protection.doReadAuthLogin(getClient(), _buf, _data);
	
	}

	@Override
	public void runImpl()
	{
		if(!ccpGuard.Protection.doAuthLogin(getClient(), _data, _loginName))
			return;

		SessionKey key = new SessionKey(_loginKey1, _loginKey2, _playKey1, _playKey2);

		final L2GameClient client = getClient();
		client.setSessionId(key);
		client.setLoginName(_loginName);
		LSConnection.getInstance().addWaitingClient(client);
	}

	private class GGTest implements Runnable
	{
		private final L2GameClient targetClient;

		private int attempts = 0;

		public GGTest(L2GameClient targetClient)
		{
			this.targetClient = targetClient;
		}

		public void run()
		{
			if(!targetClient.isGameGuardOk())
				if(attempts < 3)
				{
					targetClient.sendPacket(Msg.GameGuardQuery);
					attempts++;
					ThreadPoolManager.getInstance().scheduleGeneral(this, 500 * attempts);
				}
				else
					targetClient.closeNow(false);
		}
	}
}