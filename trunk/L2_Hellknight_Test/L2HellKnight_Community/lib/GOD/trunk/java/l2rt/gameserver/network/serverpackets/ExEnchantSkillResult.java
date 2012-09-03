package l2rt.gameserver.network.serverpackets;

public class ExEnchantSkillResult extends L2GameServerPacket
{
	private final int _result;

	public ExEnchantSkillResult(int result)
	{
		_result = result;
	}

	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0xA7);
		writeD(_result);
	}
}