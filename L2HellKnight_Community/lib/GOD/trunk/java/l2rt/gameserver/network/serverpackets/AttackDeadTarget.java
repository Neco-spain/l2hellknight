package l2rt.gameserver.network.serverpackets;

public class AttackDeadTarget extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		// just trigger - без аргументов
		writeC(0x04);
	}
}