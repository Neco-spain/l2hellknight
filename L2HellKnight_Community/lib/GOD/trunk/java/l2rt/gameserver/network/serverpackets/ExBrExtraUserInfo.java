package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.L2Player;

public class ExBrExtraUserInfo extends L2GameServerPacket
{
	private int _charId;

	public ExBrExtraUserInfo(L2Player cha)
	{
		_charId = cha.getObjectId();
	}

	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0xCF);
		writeD(_charId);
		writeD(0);
		//writeC(0); // Event Flag
	}
}