package l2rt.loginserver.gameservercon.gspackets;

import l2rt.loginserver.IpManager;
import l2rt.loginserver.gameservercon.AttGS;
import l2rt.loginserver.gameservercon.GSConnection;
import l2rt.loginserver.gameservercon.lspackets.BanIPList;
import l2rt.loginserver.gameservercon.lspackets.IpAction;

public class UnbanIP extends ClientBasePacket
{
	public UnbanIP(byte[] decrypt, AttGS gameserver)
	{
		super(decrypt, gameserver);
	}

	@Override
	public void read()
	{
		String ip = readS();
		IpManager.getInstance().UnbanIp(ip);

		GSConnection.getInstance().broadcastPacket(new BanIPList());
		GSConnection.getInstance().broadcastPacket(new IpAction(ip, false, ""));
	}
}