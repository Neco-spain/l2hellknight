package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.network.L2GameClient;

/**
 * format ch
 * c: (id) 0xD0
 * h: (subid) 0x13
 * @author -Wooden-
 *
 */
public final class RequestOlympiadMatchList extends L2GameClientPacket
{
	private static final String _C__D0_13_REQUESTOLYMPIADMATCHLIST = "[C] D0:13 RequestOlympiadMatchList";


	@Override
	protected void readImpl()
	{
		// trigger packet
	}

	@Override
	protected void runImpl()
	{
	   L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
		 if (activeChar == null)
		      return;
		 if (activeChar.inObserverMode()) Olympiad.sendMatchList(activeChar);
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__D0_13_REQUESTOLYMPIADMATCHLIST;
	}

}