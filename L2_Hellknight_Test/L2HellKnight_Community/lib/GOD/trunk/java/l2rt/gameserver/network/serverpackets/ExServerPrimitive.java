package l2rt.gameserver.network.serverpackets;

public class ExServerPrimitive extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x11);
		// TODO Sdddddd {[c(Sdddd ddd ddd|)] Sddddddd}
	}
}