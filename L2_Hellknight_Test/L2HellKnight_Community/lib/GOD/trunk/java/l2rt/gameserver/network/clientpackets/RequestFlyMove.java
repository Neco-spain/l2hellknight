package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.instancemanager.JumpManager;

public final class RequestFlyMove extends L2GameClientPacket
{
	int _nextPoint;
	
	@Override
	protected void readImpl()
	{
		_nextPoint = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
			
		JumpManager.getInstance().NextJump(activeChar, _nextPoint);
	}
}
