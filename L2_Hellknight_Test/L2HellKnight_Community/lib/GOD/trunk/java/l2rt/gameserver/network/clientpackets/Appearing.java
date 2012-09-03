package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Player;

public class Appearing extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{}

	@Override
	public void runImpl()
	{
		final L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isLogoutStarted())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.getObserverMode() == 1)
		{
			activeChar.appearObserverMode();
			return;
		}

		if(activeChar.getObserverMode() == 2)
		{
			activeChar.returnFromObserverMode();
			return;
		}

		if(!activeChar.isTeleporting())
		{
			activeChar.sendActionFailed();
			return;
		}

		activeChar.onTeleported();
	}
}