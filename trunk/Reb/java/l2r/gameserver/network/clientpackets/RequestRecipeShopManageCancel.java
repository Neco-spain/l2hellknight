package l2r.gameserver.network.clientpackets;

import l2r.gameserver.model.Player;

public class RequestRecipeShopManageCancel extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		//TODO [G1ta0] проанализировать
	}
}