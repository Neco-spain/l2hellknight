package l2p.gameserver.clientpackets;

import l2p.gameserver.serverpackets.CharacterSelectionInfo;

public class GotoLobby extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		CharacterSelectionInfo cl = new CharacterSelectionInfo(getClient().getLogin(), getClient().getSessionKey().playOkID1);
		sendPacket(cl);
	}
}