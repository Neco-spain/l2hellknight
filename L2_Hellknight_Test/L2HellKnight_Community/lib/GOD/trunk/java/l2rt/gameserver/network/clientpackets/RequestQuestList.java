package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.network.serverpackets.QuestList;

public class RequestQuestList extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{}

	@Override
	public void runImpl()
	{
		sendPacket(new QuestList(getClient().getActiveChar()));
	}
}