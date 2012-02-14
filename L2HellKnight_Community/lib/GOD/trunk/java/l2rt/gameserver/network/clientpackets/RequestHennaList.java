package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.serverpackets.HennaEquipList;
import l2rt.gameserver.tables.HennaTreeTable;

public class RequestHennaList extends L2GameClientPacket
{
	// format: cd
	@SuppressWarnings("unused")
	private int _unknown;

	@Override
	public void readImpl()
	{
		_unknown = readD(); // ??
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		activeChar.sendPacket(new HennaEquipList(activeChar, HennaTreeTable.getInstance().getAvailableHenna(activeChar.getClassId(), activeChar.getSex())));
	}
}