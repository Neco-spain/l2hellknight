package l2rt.gameserver.network.clientpackets;

import l2rt.Config;
import l2rt.gameserver.model.L2Player;

/**
 * format ch
 * c: (id) 0xD0
 * h: (subid) 0x29
 *
 */
public class RequestOlympiadObserverEnd extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(Config.ENABLE_OLYMPIAD && activeChar.inObserverMode())
			activeChar.leaveOlympiadObserverMode();
	}
}