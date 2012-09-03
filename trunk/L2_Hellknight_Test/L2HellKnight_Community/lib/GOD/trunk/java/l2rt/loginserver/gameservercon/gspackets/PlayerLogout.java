package l2rt.loginserver.gameservercon.gspackets;

import l2rt.Config;
import l2rt.loginserver.GameServerTable;
import l2rt.loginserver.LoginController;
import l2rt.loginserver.gameservercon.AttGS;

import java.util.logging.Logger;

public class PlayerLogout extends ClientBasePacket
{
	public static final Logger log = Logger.getLogger(PlayerLogout.class.getName());

	public PlayerLogout(byte[] decrypt, AttGS gameserver)
	{
		super(decrypt, gameserver);
	}

	@Override
	public void read()
	{
		String account = readS();

		getGameServer().removeAccountFromGameServer(account);
		LoginController.getInstance().removeAuthedLoginClient(account);

		if(Config.LOGIN_DEBUG)
			log.info("Player " + account + " logged out from gameserver [" + getGameServer().getServerId() + "] " + GameServerTable.getInstance().getServerNameById(getGameServer().getServerId()));
	}
}