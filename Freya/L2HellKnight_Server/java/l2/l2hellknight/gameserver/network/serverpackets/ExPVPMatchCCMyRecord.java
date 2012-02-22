package l2.hellknight.gameserver.network.serverpackets;

public class ExPVPMatchCCMyRecord extends L2GameServerPacket
{
	private static final String _S__FE_8A_EXPVPMATCHCCMYRECORD = "[S] FE:8A ExPVPMatchCCMyRecord";

	private final int _kp;

	public ExPVPMatchCCMyRecord(int killPts)
	{
		_kp = killPts;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x8a);

		writeD(_kp);
	}

	@Override
	public String getType()
	{
		return _S__FE_8A_EXPVPMATCHCCMYRECORD;
	}
}