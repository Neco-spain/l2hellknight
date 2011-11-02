package com.l2js.gameserver.network.serverpackets;

public class ExFriendDetailInfo extends L2GameServerPacket
{
	public ExFriendDetailInfo()
	{
	}

	@Override
	protected final void writeImpl()
	{
        writeC(0xfe);
        writeH(0xeb);
	}
	
	@Override
	public final String getType()
	{
		return "[S] FE:EB ExFriendDetailInfo";
	}
}
