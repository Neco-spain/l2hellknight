package l2r.gameserver.network.serverpackets;

public class ExShowTerritory extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeEx(0x89);
		// TODO ddd[dd]
	}
}