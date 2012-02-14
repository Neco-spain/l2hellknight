package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Player;

public class RequestChangeAttributeCancel extends L2GameClientPacket
{
	
	@Override
	public void readImpl()
	{
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		
		activeChar.sendActionFailed();
		
	}

}