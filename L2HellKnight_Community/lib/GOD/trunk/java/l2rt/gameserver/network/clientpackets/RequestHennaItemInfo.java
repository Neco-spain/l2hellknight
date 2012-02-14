package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2HennaInstance;
import l2rt.gameserver.network.serverpackets.HennaItemInfo;
import l2rt.gameserver.tables.HennaTable;
import l2rt.gameserver.templates.L2Henna;

public class RequestHennaItemInfo extends L2GameClientPacket
{
	// format  cd
	private int SymbolId;

	@Override
	public void readImpl()
	{
		SymbolId = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		L2Henna template = HennaTable.getInstance().getTemplate(SymbolId);
		if(template != null)
			activeChar.sendPacket(new HennaItemInfo(new L2HennaInstance(template), activeChar));
	}
}