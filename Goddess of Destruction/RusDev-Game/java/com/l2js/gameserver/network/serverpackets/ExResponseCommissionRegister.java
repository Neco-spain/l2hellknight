package com.l2js.gameserver.network.serverpackets;

public class ExResponseCommissionRegister extends L2GameServerPacket
{
	public ExResponseCommissionRegister()
	{
	}

	@Override
	protected final void writeImpl()
	{
        writeC(0xfe);
        writeH(0xf4);	
	}
	
	@Override
	public final String getType()
	{
		return "[S] FE:F4 ExResponseCommissionRegister";
	}
}
