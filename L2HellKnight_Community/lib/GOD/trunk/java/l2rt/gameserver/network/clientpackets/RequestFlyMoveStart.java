package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.instancemanager.JumpManager;

public final class RequestFlyMoveStart extends L2GameClientPacket
{
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		
		JumpManager.getInstance().StartJump(activeChar);
	}
	
}
