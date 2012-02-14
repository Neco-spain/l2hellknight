package l2rt.gameserver.network.serverpackets;

public class ExMagicSkillUseInAirShip extends L2GameServerPacket
{
	/**
	 * заготовка!!!
	 * Format: ddddddddddh[h]h[ddd]
	 */

	@Override
	protected final void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x73);
	}
}