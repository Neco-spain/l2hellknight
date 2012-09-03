package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.instancemanager.FortressManager;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.entity.residence.Fortress;
import l2rt.gameserver.network.serverpackets.ExShowFortressSiegeInfo;

public class RequestFortressSiegeInfo extends L2GameClientPacket
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
		for(Fortress fort : FortressManager.getInstance().getFortresses().values())
			if(fort != null && fort.getSiege().isInProgress())
				activeChar.sendPacket(new ExShowFortressSiegeInfo(fort));
	}
}