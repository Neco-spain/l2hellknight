package com.l2js.gameserver.network.serverpackets;

public class ExBlockRemoveResult extends L2GameServerPacket
{
	public ExBlockRemoveResult()
	{
	}

	@Override
	protected final void writeImpl()
	{
        writeC(0xfe);
        writeH(0xed);
	}
	
	@Override
	public final String getType()
	{
		return "[S] FE:ED ExBlockRemoveResult";
	}
}
