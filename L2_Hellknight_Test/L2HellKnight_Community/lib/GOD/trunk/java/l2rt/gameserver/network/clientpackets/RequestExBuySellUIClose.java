package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.serverpackets.ItemList;

public class RequestExBuySellUIClose extends L2GameClientPacket
{
	@Override
	public void runImpl()
	{
	// trigger
	}

	@Override
	public void readImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null || activeChar.isInventoryDisabled())
			return;

		activeChar.setBuyListId(0);
		activeChar.sendPacket(new ItemList(activeChar, true));
	}
}