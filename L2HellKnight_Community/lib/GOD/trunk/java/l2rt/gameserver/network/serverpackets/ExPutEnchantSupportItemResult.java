package l2rt.gameserver.network.serverpackets;

public class ExPutEnchantSupportItemResult extends L2GameServerPacket
{
	private int _result;

	public ExPutEnchantSupportItemResult(int result)
	{
		_result = result;
	}

	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x82);
		writeD(_result);
	}
}