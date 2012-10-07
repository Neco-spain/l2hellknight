package l2p.gameserver.clientpackets;

import l2p.gameserver.data.xml.holder.ResidenceHolder;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.residence.Residence;
import l2p.gameserver.serverpackets.CastleSiegeAttackerList;

public class RequestCastleSiegeAttackerList extends L2GameClientPacket
{
	private int _unitId;

	@Override
	protected void readImpl()
	{
		_unitId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		Residence residence = ResidenceHolder.getInstance().getResidence(_unitId);
		if(residence != null)
			sendPacket(new CastleSiegeAttackerList(residence));
	}
}