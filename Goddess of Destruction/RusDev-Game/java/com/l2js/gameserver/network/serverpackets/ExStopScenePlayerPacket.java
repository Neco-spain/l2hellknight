package com.l2js.gameserver.network.serverpackets;

public class ExStopScenePlayerPacket extends L2GameServerPacket
{
	public ExStopScenePlayerPacket()
	{
	}

	@Override
	protected final void writeImpl()
	{
        writeC(0xfe);
        writeH(0xe6);
	}
	
	@Override
	public final String getType()
	{
		return "[S] FE:E6 ExStopScenePlayerPacket";
	}
}
