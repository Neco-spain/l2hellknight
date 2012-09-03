package l2rt.gameserver.network.clientpackets;

import l2rt.Config;
import l2rt.gameserver.model.L2Player;

/**
 * Format: (c) S
 * S: pledge name?
 */
public class RequestPledgeExtendedInfo extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private String _name;

	@Override
	protected void readImpl()
	{
		_name = readS(Config.CNAME_MAXLEN);
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(activeChar.isGM())
			activeChar.sendMessage("RequestPledgeExtendedInfo");

		// TODO this
	}
}