package l2rt.gameserver.network.serverpackets;

public class ExShowTerritory extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x89);
		// TODO ddd[dd]
	}
}