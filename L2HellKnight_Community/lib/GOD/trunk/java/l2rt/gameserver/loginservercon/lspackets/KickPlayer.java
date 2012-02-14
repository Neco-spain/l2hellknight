package l2rt.gameserver.loginservercon.lspackets;

import l2rt.gameserver.loginservercon.AttLS;

public class KickPlayer extends LoginServerBasePacket
{
	public KickPlayer(byte[] decrypt, AttLS loginserver)
	{
		super(decrypt, loginserver);
	}

	@Override
	public void read()
	{
		getLoginServer().getCon().kickAccountInGame(readS());
	}
}