//L2DDT
package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public final class ObserverReturn extends L2GameClientPacket
{
	private static final String OBSRETURN__C__04 = "[C] b8 ObserverReturn";

	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		if (activeChar.inObserverMode()) activeChar.leaveObserverMode();
	}
	@Override
	public String getType()
	{
		return OBSRETURN__C__04;
	}
}