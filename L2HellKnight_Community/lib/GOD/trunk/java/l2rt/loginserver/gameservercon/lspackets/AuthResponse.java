package l2rt.loginserver.gameservercon.lspackets;

import l2rt.Config;
import l2rt.loginserver.GameServerTable;

public class AuthResponse extends ServerBasePacket
{
	public static final int LastProtocolVersion = 2;

	public AuthResponse(int serverId)
	{
		writeC(0x02);
		writeC(serverId);
		writeS(GameServerTable.getInstance().getServerNameById(serverId));
		writeC(Config.SHOW_LICENCE ? 0 : 1);
		writeH(LastProtocolVersion);
	}
}