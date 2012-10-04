package l2p.gameserver.serverpackets;

public class ExDuelEnemyRelation extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeEx(0x59);
		// just trigger
	}
}