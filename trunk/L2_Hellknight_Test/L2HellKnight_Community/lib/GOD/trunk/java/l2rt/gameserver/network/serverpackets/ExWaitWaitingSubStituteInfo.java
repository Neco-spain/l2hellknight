package l2rt.gameserver.network.serverpackets;

// Автоматический поиск партии
public class ExWaitWaitingSubStituteInfo extends L2GameServerPacket
{
    private static final String _S__FE_103_ExWaitWaitingSubStituteInfo = "[S] FE:103 - ExWaitWaitingSubStituteInfo";
	
	private int _code;
	
	public ExWaitWaitingSubStituteInfo( int code)
	{
		_code = code;
	}
	
    @Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x103);
        writeD(_code); // 01 - Поиск групы включен, 00 - выключен
	}

    @Override
	public String getType()
	{
		return _S__FE_103_ExWaitWaitingSubStituteInfo;
	}
}