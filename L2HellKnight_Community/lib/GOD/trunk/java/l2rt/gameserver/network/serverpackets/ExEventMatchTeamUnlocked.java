package l2rt.gameserver.network.serverpackets;

public class ExEventMatchTeamUnlocked extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x06);
		// TODO dc
	}
}