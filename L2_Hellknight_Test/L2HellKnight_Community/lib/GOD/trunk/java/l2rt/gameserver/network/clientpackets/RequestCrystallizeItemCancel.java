package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.serverpackets.ExGetCrystalizingFail;
import l2rt.gameserver.network.serverpackets.L2GameServerPacket;

public class RequestCrystallizeItemCancel extends L2GameClientPacket
{
	public void readImpl()
	{
	}

	public void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		player.sendPacket(new L2GameServerPacket[] { new ExGetCrystalizingFail(0) });
	}
}