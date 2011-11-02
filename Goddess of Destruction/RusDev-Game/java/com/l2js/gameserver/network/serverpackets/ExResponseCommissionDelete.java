package com.l2js.gameserver.network.serverpackets;

public class ExResponseCommissionDelete extends L2GameServerPacket
{
	public ExResponseCommissionDelete()
	{
	}

	@Override
	protected final void writeImpl()
	{
        writeC(0xfe);
        writeH(0xf5);	
	}
	
	@Override
	public final String getType()
	{
		return "[S] FE:F5 ExResponseCommissionDelete";
	}
}
