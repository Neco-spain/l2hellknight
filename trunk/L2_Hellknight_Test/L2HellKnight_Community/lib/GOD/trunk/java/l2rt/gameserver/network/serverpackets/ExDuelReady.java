package l2rt.gameserver.network.serverpackets;

public class ExDuelReady extends L2GameServerPacket
{
	private int _unk1;

	public ExDuelReady(int unk1)
	{
		_unk1 = unk1;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x4d);

		writeD(_unk1);
	}
}