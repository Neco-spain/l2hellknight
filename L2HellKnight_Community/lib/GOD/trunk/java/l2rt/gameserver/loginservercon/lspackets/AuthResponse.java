package l2rt.gameserver.loginservercon.lspackets;

import l2rt.Config;
import l2rt.gameserver.loginservercon.AttLS;
import l2rt.gameserver.loginservercon.Attribute;
import l2rt.gameserver.loginservercon.gspackets.PlayerInGame;
import l2rt.gameserver.loginservercon.gspackets.PlayersInGame;
import l2rt.gameserver.loginservercon.gspackets.ServerStatus;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;
import l2rt.util.GArray;
import l2rt.util.Stats;

import java.util.logging.Logger;

public class AuthResponse extends LoginServerBasePacket
{
	private static final Logger log = Logger.getLogger(AuthResponse.class.getName());

	private int _serverId;
	private String _serverName;

	public AuthResponse(byte[] decrypt, AttLS loginServer)
	{
		super(decrypt, loginServer);
	}

	@Override
	public void read()
	{
		_serverId = readC();
		_serverName = readS();
		getLoginServer().setLicenseShown(readC() == 1);
		try
		{
			getLoginServer().setProtocolVersion(readH());
		}
		catch(Exception e)
		{
			getLoginServer().setProtocolVersion(0);
		}

		log.info("Registered on login as Server " + _serverId + " : " + _serverName);

		GArray<Attribute> attributes = new GArray<Attribute>();

		attributes.add(new Attribute(Attribute.SERVER_LIST_SQUARE_BRACKET, Config.SERVER_LIST_BRACKET ? Attribute.ON : Attribute.OFF));
		attributes.add(new Attribute(Attribute.SERVER_LIST_CLOCK, Config.SERVER_LIST_CLOCK ? Attribute.ON : Attribute.OFF));
		attributes.add(new Attribute(Attribute.TEST_SERVER, Config.SERVER_LIST_TESTSERVER ? Attribute.ON : Attribute.OFF));
		attributes.add(new Attribute(Attribute.SERVER_LIST_STATUS, Config.SERVER_GMONLY ? Attribute.STATUS_GM_ONLY : Attribute.STATUS_AUTO));

		getLoginServer().setAuthResponsed(true);
		sendPacket(new ServerStatus(attributes));

		if(L2ObjectsStorage.getAllPlayersCount() > 0)
		{
			GArray<String> playerList = new GArray<String>();
			for(L2Player player : L2ObjectsStorage.getAllPlayers())
			{
				if(player.isInOfflineMode())
					continue;
				if(player.getAccountName() == null || player.getAccountName().isEmpty())
				{
					log.warning("AuthResponse: empty accname for " + player);
					continue;
				}
				playerList.add(player.getAccountName());
				getLoginServer().getCon().addAccountInGame(player.getNetConnection());
			}

			int online = Stats.getOnline(true);

			sendPacket(new PlayerInGame(null, online));
			if(getLoginServer().getProtocolVersion() > 1)
				sendPackets(PlayersInGame.makePlayersInGame(online, playerList));
			else
				for(String name : playerList)
					sendPacket(new PlayerInGame(name, online));
		}
	}
}