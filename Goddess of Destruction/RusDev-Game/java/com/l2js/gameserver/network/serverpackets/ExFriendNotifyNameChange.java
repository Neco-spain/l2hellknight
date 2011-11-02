package com.l2js.gameserver.network.serverpackets;

public class ExFriendNotifyNameChange extends L2GameServerPacket
{
	public ExFriendNotifyNameChange()
	{
	}

	@Override
	protected final void writeImpl()
	{
        writeC(0xfe);
        writeH(0xf0);
	}
	
	@Override
	public final String getType()
	{
		return "[S] FE:F0 ExFriendNotifyNameChange";
	}
}
