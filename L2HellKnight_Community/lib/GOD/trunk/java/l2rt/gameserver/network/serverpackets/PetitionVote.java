package l2rt.gameserver.network.serverpackets;

public class PetitionVote extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		// just trigger
		writeC(0xFC);
	}
}