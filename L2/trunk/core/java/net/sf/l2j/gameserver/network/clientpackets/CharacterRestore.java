package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.network.serverpackets.CharSelectInfo;

public final class CharacterRestore extends L2GameClientPacket
{
	private static final String _C__62_CHARACTERRESTORE = "[C] 62 CharacterRestore";

    private int _charSlot;

	@Override
	protected void readImpl()
	{
		_charSlot = readD();
	}

	@Override
	protected void runImpl()
	{
	    try
	    {
		getClient().markRestoredChar(_charSlot);
	    } catch (Exception e){}
		CharSelectInfo cl = new CharSelectInfo(getClient().getAccountName(), getClient().getSessionId().playOkID1, 0);
		sendPacket(cl);
		getClient().setCharSelection(cl.getCharInfo());
	}

	@Override
	public String getType()
	{
		return _C__62_CHARACTERRESTORE;
	}
}
