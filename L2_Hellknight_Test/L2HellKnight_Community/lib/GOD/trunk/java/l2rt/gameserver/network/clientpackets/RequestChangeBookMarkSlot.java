package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Player;

public class RequestChangeBookMarkSlot extends L2GameClientPacket
{
	private int slot_old, slot_new;

	@Override
	public void readImpl()
	{
		slot_old = readD();
		slot_new = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		System.out.println(getType() + "@" + activeChar + "::" + slot_old + "::" + slot_new);
	}
}