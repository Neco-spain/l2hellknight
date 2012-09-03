package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.serverpackets.ExGetBookMarkInfo;

/**
 * SdS
 */
public class RequestSaveBookMarkSlot extends L2GameClientPacket
{
	private String name, acronym;
	private int icon;

	@Override
	public void readImpl()
	{
		name = readS(32);
		icon = readD();
		acronym = readS(4);
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar != null && activeChar.bookmarks.add(name, acronym, icon))
			activeChar.sendPacket(new ExGetBookMarkInfo(activeChar));
	}
}