package l2rt.gameserver.network.serverpackets;

public final class ExShowCubeTimer extends L2GameServerPacket
{
	private int _x;

	public ExShowCubeTimer(int x)
	{
		_x = x;
	}

	@Override
	public void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x8A); // Gracia Final
		//writeH(0x89); // Gracia Part 2
		writeD(_x);
	}
}