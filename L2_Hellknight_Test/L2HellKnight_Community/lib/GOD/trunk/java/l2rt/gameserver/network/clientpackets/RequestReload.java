package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2WorldRegion;

public class RequestReload extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{}

	@Override
	public void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;

		player.sendUserInfo(true);

		if(player.getCurrentRegion() != null)
			for(L2WorldRegion neighbor : player.getCurrentRegion().getNeighbors())
				neighbor.showObjectsToPlayer(player);
	}
}