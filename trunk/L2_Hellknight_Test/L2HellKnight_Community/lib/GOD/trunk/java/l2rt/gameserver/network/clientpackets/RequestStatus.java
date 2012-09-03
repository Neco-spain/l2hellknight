package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.network.serverpackets.SendStatus;

import java.util.logging.Logger;

public final class RequestStatus extends L2GameClientPacket
{
	static Logger _log = Logger.getLogger(RequestStatus.class.getName());

	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		getClient().close(new SendStatus());
	}
}