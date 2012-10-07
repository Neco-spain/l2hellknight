package l2p.gameserver.serverpackets;

public class ExRequestHackShield extends L2GameServerPacket
{
	@Override
	protected final void writeImpl()
	{
		writeEx(0x49);
	}
}