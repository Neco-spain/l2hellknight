package l2rt.gameserver.network.serverpackets;

public final class ExCubeTimerStop extends L2GameServerPacket
{
	public ExCubeTimerStop()
	{}

	@Override
	public void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x8B);
	}
}