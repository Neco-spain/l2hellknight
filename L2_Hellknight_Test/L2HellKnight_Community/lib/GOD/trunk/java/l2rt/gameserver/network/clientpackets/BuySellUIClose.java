package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.serverpackets.ItemList;

public class BuySellUIClose extends L2GameClientPacket
{
	private static final String _C__D0_76_REQUESTBUYSELLUICLOSE = "[C] D0:76 BuySellUIClose";

	protected void readImpl()
	{}

	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null){ return; }
		activeChar.sendPacket(new ItemList(activeChar, true));
	}

	public String getType()
	{
		return _C__D0_76_REQUESTBUYSELLUICLOSE;
	}
}