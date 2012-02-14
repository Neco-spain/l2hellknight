package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.instancemanager.PartyRoomManager;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.serverpackets.PartyMatchDetail;

public class RequestPartyMatchConfig extends L2GameClientPacket
{
	private int _page;
	private int _region;
	private int _allLevels;

	/**
	 * Format: ddd
	 */
	@Override
	public void readImpl()
	{
		_page = readD();
		_region = readD(); // 0 to 15, or -1
		_allLevels = readD(); // 1 -> all levels, 0 -> only levels matching my level
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		activeChar.setPartyMatchingLevels(_allLevels);
		activeChar.setPartyMatchingRegion(_region);

		PartyRoomManager.getInstance().addToWaitingList(activeChar);

		activeChar.sendPacket(new PartyMatchDetail(_region, _allLevels, _page, activeChar));
	}
}