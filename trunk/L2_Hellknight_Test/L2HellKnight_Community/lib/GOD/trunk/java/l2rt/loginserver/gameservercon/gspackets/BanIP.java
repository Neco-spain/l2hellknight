package l2rt.loginserver.gameservercon.gspackets;

import l2rt.loginserver.IpManager;
import l2rt.loginserver.gameservercon.AttGS;
import l2rt.loginserver.gameservercon.GSConnection;
import l2rt.loginserver.gameservercon.lspackets.BanIPList;
import l2rt.loginserver.gameservercon.lspackets.IpAction;

public class BanIP extends ClientBasePacket
{

	public BanIP(byte[] decrypt, AttGS gameserver)
	{
		super(decrypt, gameserver);
	}

	@Override
	public void read()
	{
		String ip = readS();
		String admin = readS();

		IpManager.getInstance().BanIp(ip, admin, 0, "");
		GSConnection.getInstance().broadcastPacket(new BanIPList());
		GSConnection.getInstance().broadcastPacket(new IpAction(ip, true, admin));
	}
}