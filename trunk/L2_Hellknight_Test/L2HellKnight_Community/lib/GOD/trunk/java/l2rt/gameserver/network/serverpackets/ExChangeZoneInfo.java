package l2rt.gameserver.network.serverpackets;

/**
 *
 * @author ~ExTaZy~
 */
public class ExChangeZoneInfo extends L2GameServerPacket
{
	private int _unkInt = 0x00;
	private int _zone = 0x00;

    public ExChangeZoneInfo(int unkInt, int zone)
	{
		_unkInt = unkInt;
		_zone = zone;
	}

	protected final void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0xC1);
		writeD(_unkInt);
		writeD(_zone);
	}
}