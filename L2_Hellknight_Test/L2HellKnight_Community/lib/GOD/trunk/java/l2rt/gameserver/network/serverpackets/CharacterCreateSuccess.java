package l2rt.gameserver.network.serverpackets;

public class CharacterCreateSuccess extends L2GameServerPacket
{
	@Override
	protected final void writeImpl()
	{
		writeC(0x0f);
		writeD(0x01);
	}
}