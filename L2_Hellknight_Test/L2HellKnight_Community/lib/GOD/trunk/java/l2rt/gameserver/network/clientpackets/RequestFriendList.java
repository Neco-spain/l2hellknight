package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.network.serverpackets.L2FriendList;

public class RequestFriendList extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{}

	@Override
	public void runImpl()
	{
		sendPacket(new L2FriendList(getClient().getActiveChar()));
	}
}