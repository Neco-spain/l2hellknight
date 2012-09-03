package l2rt.gameserver.network.serverpackets;

public class ExChangeToAwakenedClass extends L2GameServerPacket
{
    private static final String _S__FE_FE_ExChangeToAwakenedClass = "[S] FE:FE - ExChangeToAwakenedClass";
	
	private int _newClass;
	
	public ExChangeToAwakenedClass(int newClass)
	{
		_newClass = newClass;
	}
	
    @Override
	protected void writeImpl()
	{
        writeC(EXTENDED_PACKET);
        writeH(0xfe);
		writeD(_newClass);
		writeD(0x00);
	}

    @Override
	public String getType()
	{
		return _S__FE_FE_ExChangeToAwakenedClass;
	}
}