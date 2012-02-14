package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.L2GameClient;
import l2rt.gameserver.network.L2GameClient.GameClientState;
import l2rt.gameserver.network.serverpackets.CharSelected;
import l2rt.util.AutoBan;

public class CharacterSelected extends L2GameClientPacket
{
	private int _charSlot;

	/**
	 * Format: cdhddd
	 */
	@Override
	public void readImpl()
	{
		_charSlot = readD();
	}

	@Override
	public void runImpl()
	{
		L2GameClient client = getClient();

		if(client.getActiveChar() != null)
			return;

		L2Player activeChar = client.loadCharFromDisk(_charSlot);
		if(activeChar == null)
			return;

		if(AutoBan.isBanned(activeChar.getObjectId()))
		{
			activeChar.setAccessLevel(-100);
			activeChar.logout(false, false, true, true);
			return;
		}

		if(activeChar.getAccessLevel() < 0)
			activeChar.setAccessLevel(0);

		if(!ccpGuard.Protection.checkPlayerWithHWID(client, activeChar.getObjectId(), activeChar.getName()))
			return;
		client.setState(GameClientState.IN_GAME);
		sendPacket(new CharSelected(activeChar, client.getSessionId().playOkID1));
	}
}