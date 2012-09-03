package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.serverpackets.ExGetBookMarkInfo;

public class RequestBookMarkSlotInfo extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{
	//just trigger
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		activeChar.sendPacket(new ExGetBookMarkInfo(activeChar));
	}
}