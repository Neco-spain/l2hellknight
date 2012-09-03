package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.serverpackets.ItemList;

public class RequestItemList extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null || !activeChar.getPlayerAccess().UseInventory || activeChar.isInventoryDisabled())
			return;
		sendPacket(new ItemList(activeChar, true));
	}
}