package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.serverpackets.ExLoadStatWorldRank;

public final class RequestWorldStatistics extends L2GameClientPacket
{
	protected void readImpl()
	{
	}

	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
			
		activeChar.sendPacket(new ExLoadStatWorldRank());
		//activeChar.sendPacket(new ExResponseCommissionItemList(activeChar));
	}
}