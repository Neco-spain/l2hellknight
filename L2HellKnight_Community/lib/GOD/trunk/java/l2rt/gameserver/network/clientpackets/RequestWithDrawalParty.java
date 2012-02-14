package l2rt.gameserver.network.clientpackets;

import l2rt.extensions.multilang.CustomMessage;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.Reflection;

public class RequestWithDrawalParty extends L2GameClientPacket
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
		if(activeChar.isInParty())
		{
			if(activeChar.isInOlympiadMode())
			{
				activeChar.sendMessage("Вы не можете сейчас выйти из группы.");
				return;
			}
			Reflection r = activeChar.getParty().getReflection();
			activeChar.getParty().oustPartyMember(activeChar);
		}
	}
}