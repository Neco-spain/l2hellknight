package l2.hellknight.gameserver.network.serverpackets;

public class ExChangeAreaState extends L2GameServerPacket
{

    public ExChangeAreaState(int id, int state)
    {
        _areaID = id;
        _state = state;
    }

    protected final void writeImpl()
    {
        writeC(254);
        writeH(193);
        writeD(_areaID);
        writeD(_state);
    }

    public String getType()
    {
        return "[S] FE:C1 ExChangeAreaState";
    }

    @SuppressWarnings("unused")
	private static final String _S__FE_C1__EXCHANGENPCSTATE = "[S] FE:C1 ExChangeAreaState";
    private final int _areaID;
    private final int _state;
}