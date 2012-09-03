package l2rt.gameserver.network.clientpackets;

public class RequestHennaUnequipInfo extends L2GameClientPacket
{
	private int _symbolId;

	@Override
	public void runImpl()
	{
		System.out.println(getType() + " :: " + _symbolId);
	}

	/**
	 * format: d
	 */
	@Override
	public void readImpl()
	{
		_symbolId = readD();
	}
}