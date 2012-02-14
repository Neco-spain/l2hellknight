package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Player;

public class RequestCreatePledge extends L2GameClientPacket
{
	//Format: cS
	private String _pledgename;

	@Override
	public void readImpl()
	{
		_pledgename = readS(64);
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		System.out.println("Unfinished packet: " + getType() + " // S: " + _pledgename);
	}
}