package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.network.serverpackets.ExBR_MinigameLoadScoresPacket;

public class RequestBR_MinigameLoadScores extends L2GameClientPacket
{
	@Override
	protected void readImpl() throws Exception
	{
	//just a trigger
	}

	@Override
	protected void runImpl() throws Exception
	{
		new ExBR_MinigameLoadScoresPacket();
		//TODO send
	}
}