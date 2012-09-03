package l2rt.gameserver.network.clientpackets;

import l2rt.Config;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.gameserver.model.L2Party;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.Reflection;

public class RequestOustPartyMember extends L2GameClientPacket
{
	//Format: cS
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

		L2Party party = activeChar.getParty();
		if(party != null && party.isLeader(activeChar))
		{
			if(activeChar.isInOlympiadMode())
			{
				activeChar.sendMessage("Вы не можете сейчас выйти из группы.");
				return;
			}
			Reflection r = party.getReflection();
			L2Player oustPlayer = party.getPlayerByName(_name);
			party.oustPartyMember(_name);
		}
	}
}