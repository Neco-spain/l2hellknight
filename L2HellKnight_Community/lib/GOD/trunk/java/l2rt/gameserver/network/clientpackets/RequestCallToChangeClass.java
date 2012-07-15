package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.instancemanager.AwakingManager;

public final class RequestCallToChangeClass extends L2GameClientPacket
{
	protected void readImpl()
	{
	}

	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		AwakingManager.getInstance().onStartQuestAccept(activeChar);
	}
}