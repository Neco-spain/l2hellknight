package com.l2js.gameserver.network.serverpackets;

public class ExChangeToAwakenedClass extends L2GameServerPacket
{
	public ExChangeToAwakenedClass()
	{
	}

	@Override
	protected final void writeImpl()
	{
        writeC(0xfe);
        writeH(0xfe);
	}
	
	@Override
	public final String getType()
	{
		return "[S] FE:FE ExChangeToAwakenedClass";
	}
}
