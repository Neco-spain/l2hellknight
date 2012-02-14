package l2rt.gameserver.network.serverpackets;

/**
 * @author ALF
 */
 
public final class ExNotifyFlyMoveStart extends L2GameServerPacket
{
	private static final String _S__FE_E7_EXFLYMOVE = "[S] FE:E7 ExFlyMove";
	
	public ExNotifyFlyMoveStart()
	{		
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x114);
	}

	@Override
	public String getType()
	{
		return _S__FE_E7_EXFLYMOVE;
	}
}