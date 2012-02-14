package l2rt.gameserver.network.serverpackets;

public class ExDuelStart extends L2GameServerPacket
{
	int _duelType;

	public ExDuelStart(int duelType)
	{
		_duelType = duelType;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x4e);
		writeD(_duelType); // неизвестный, возможно тип дуэли.
	}
}