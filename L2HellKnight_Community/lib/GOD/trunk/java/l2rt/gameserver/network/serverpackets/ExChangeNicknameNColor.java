package l2rt.gameserver.network.serverpackets;

public class ExChangeNicknameNColor extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x83);
		writeD(0x00); //FIXME unknown
	}
}