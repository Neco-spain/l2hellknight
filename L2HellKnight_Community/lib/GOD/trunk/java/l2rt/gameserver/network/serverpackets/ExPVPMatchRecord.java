package l2rt.gameserver.network.serverpackets;

public class ExPVPMatchRecord extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x7E);
		// TODO ddddd d[Sdd] d[Sdd]	(currentState:%d blueTeamTotalKillCnt:%d, redTeamTotalKillCnt:%d)
	}
}