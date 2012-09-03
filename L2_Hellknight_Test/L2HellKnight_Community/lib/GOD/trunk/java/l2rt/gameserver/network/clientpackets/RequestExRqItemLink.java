package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.serverpackets.ExRpItemLink;

public class RequestExRqItemLink extends L2GameClientPacket
{
	// format: (ch)d
	int _item;

	@Override
	public void readImpl()
	{
		_item = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar != null)
			activeChar.sendPacket(new ExRpItemLink(_item));
	}
}