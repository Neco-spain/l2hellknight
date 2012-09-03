package l2rt.gameserver.loginservercon.lspackets;

import l2rt.gameserver.loginservercon.AttLS;
import l2rt.gameserver.network.L2GameClient;

public class PointConnection extends LoginServerBasePacket
{
	public PointConnection(byte[] decrypt, AttLS loginServer)
	{
		super(decrypt, loginServer);
	}
	@Override
	public void read()
	{
		String player = readS();
		int point = readD();
		L2GameClient client = getLoginServer().getCon().getAccountInGame(player);
		if(client != null)
			client.setPoint(point);
	}
}