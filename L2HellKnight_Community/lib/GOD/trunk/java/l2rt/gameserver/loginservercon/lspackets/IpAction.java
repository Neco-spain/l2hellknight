package l2rt.gameserver.loginservercon.lspackets;

import l2rt.gameserver.loginservercon.AttLS;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;

public class IpAction extends LoginServerBasePacket
{
	public IpAction(byte[] decrypt, AttLS loginServer)
	{
		super(decrypt, loginServer);
	}

	@Override
	public void read()
	{
		String ip = readS();
		boolean isBan = readC() == 1;

		String gm = null;
		if(isBan)
			gm = readS();

		String message = isBan ? "IP: " + ip + " has been banned by " + gm : "IP: " + ip + " has been unbanned";
		for(L2Player player : L2ObjectsStorage.getAllPlayersForIterate())
			if(player.isGM())
				player.sendMessage(message);
	}
}