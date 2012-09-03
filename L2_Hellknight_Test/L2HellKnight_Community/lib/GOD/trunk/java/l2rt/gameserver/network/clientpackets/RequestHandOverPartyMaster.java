package l2rt.gameserver.network.clientpackets;

import l2rt.Config;
import l2rt.gameserver.model.L2Player;

public class RequestHandOverPartyMaster extends L2GameClientPacket
{
	private String _name;

	@Override
	public void readImpl()
	{
		_name = readS(Config.CNAME_MAXLEN);
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isInParty() && activeChar.getParty().isLeader(activeChar))
			activeChar.getParty().changePartyLeader(_name);
	}
}