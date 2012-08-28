package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class ExOlympiadUserInfo extends L2GameServerPacket
{
	private static final String _S__FE_29_OLYMPIADUSERINFO = "[S] FE:2C OlympiadUserInfo";
	private static L2PcInstance _activeChar;

	public ExOlympiadUserInfo(L2PcInstance player)
	{
		_activeChar = player;
	}


	@Override
	protected final void writeImpl()
	{
		writeC(0xfe);
		writeH(0x2c);
		writeD(_activeChar.getObjectId());
		writeS(_activeChar.getName());
		writeD(_activeChar.getClassId().getId());
		writeD((int)_activeChar.getCurrentHp());
		writeD(_activeChar.getMaxHp());
		writeD((int)_activeChar.getCurrentCp());
		writeD(_activeChar.getMaxCp());
	}

	@Override
	public String getType()
	{
		return _S__FE_29_OLYMPIADUSERINFO;
	}
}
