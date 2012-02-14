package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.entity.siege.territory.TerritorySiege;
import l2rt.gameserver.network.serverpackets.ExReplyDominionInfo;
import l2rt.gameserver.network.serverpackets.ExShowOwnthingPos;

public class RequestExDominionInfo extends L2GameClientPacket
{
	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		activeChar.sendPacket(new ExReplyDominionInfo());
		if(TerritorySiege.isInProgress())
			activeChar.sendPacket(new ExShowOwnthingPos());
	}

	@Override
	public void readImpl()
	{}
}