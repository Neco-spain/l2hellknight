package com.l2js.gameserver.network.serverpackets;

public class ExBlockAddResult extends L2GameServerPacket
{
	public ExBlockAddResult()
	{
	}

	@Override
	protected final void writeImpl()
	{
        writeC(0xfe);
        writeH(0xec);
	}
	
	@Override
	public final String getType()
	{
		return "[S] FE:EC ExBlockAddResult";
	}
}
