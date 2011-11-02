package com.l2js.gameserver.network.serverpackets;

public class ExShowCommission extends L2GameServerPacket
{
	public ExShowCommission()
	{
	}

	@Override
	protected final void writeImpl()
	{
        writeC(0xfe);
        writeH(0xf1);
	}
	
	@Override
	public final String getType()
	{
		return "[S] FE:F1 ExShowCommission";
	}
}
