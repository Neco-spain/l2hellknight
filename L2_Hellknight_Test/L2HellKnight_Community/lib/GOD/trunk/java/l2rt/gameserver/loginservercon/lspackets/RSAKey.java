package l2rt.gameserver.loginservercon.lspackets;

import l2rt.Config;
import l2rt.gameserver.loginservercon.AttLS;

import java.util.logging.Logger;

public class RSAKey extends LoginServerBasePacket
{
	private static final Logger log = Logger.getLogger(RSAKey.class.getName());

	public RSAKey(byte[] decrypt, AttLS loginServer)
	{
		super(decrypt, loginServer);
	}

	@Override
	public void read()
	{
		byte[] rsaKey = readB(128);
		getLoginServer().initRSA(rsaKey);
		getLoginServer().initCrypt();

		if(Config.DEBUG_GS_LS)
			log.info("GS Debug: RSAKey packet readed.");
	}
}