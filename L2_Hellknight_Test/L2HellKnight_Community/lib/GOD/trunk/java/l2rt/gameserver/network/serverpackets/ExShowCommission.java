package l2rt.gameserver.network.serverpackets;

public class ExShowCommission extends L2GameServerPacket
{
    private static final String _S__FE_F1_ExShowCommission = "[S] FE:FD - ExShowCommission";	

	public ExShowCommission()
	{
	}
	
    @Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0xf1);
        writeD(0x01); // 1 - показывает, 0 - ничего
	}

    @Override
	public String getType()
	{
		return _S__FE_F1_ExShowCommission;
	}
}