package l2rt.gameserver.loginservercon.gspackets;

import l2rt.Config;
import l2rt.extensions.network.MMOSocket;
import l2rt.gameserver.loginservercon.AdvIP;

public class AuthRequest extends GameServerBasePacket
{
	public AuthRequest()
	{
		writeC(0x01);
		writeC(Config.REQUEST_ID);
		writeC(Config.ACCEPT_ALTERNATE_ID ? 0x01 : 0x00);
		writeS(MMOSocket.getInstance(0) == null ? Config.EXTERNAL_HOSTNAME : MMOSocket.getInstance(0));
		writeS(MMOSocket.getInstance(1) == null ? Config.INTERNAL_HOSTNAME : MMOSocket.getInstance(1));
		if(Config.PORTS_GAME.length == 1) // старый формат, однопортовый
			writeH(Config.PORTS_GAME[0]);
		else
		//новый формат, многопортовый
		{
			writeH(0xFFFF);
			writeC(Config.PORTS_GAME.length);
			for(int PORT_GAME : Config.PORTS_GAME)
				writeH(PORT_GAME);
		}
		writeD(Config.MAXIMUM_ONLINE_USERS);
		byte[] data = Config.HEX_ID;
		if(data == null)
			writeD(0);
		else
		{
			writeD(Config.HEX_ID.length);
			writeB(Config.HEX_ID);
		}
		writeD(Config.GAMEIPS.size());
		for(AdvIP ip : Config.GAMEIPS)
		{
			writeS(ip.ipadress);
			writeS(ip.ipmask);
			writeS(ip.bitmask);
		}
		writeH(l2rt.loginserver.gameservercon.lspackets.AuthResponse.LastProtocolVersion);
	}
}