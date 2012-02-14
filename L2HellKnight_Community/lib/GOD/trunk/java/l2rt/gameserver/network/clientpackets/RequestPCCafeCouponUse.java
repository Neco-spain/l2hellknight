package l2rt.gameserver.network.clientpackets;

/**
 * format: chS
 */
public class RequestPCCafeCouponUse extends L2GameClientPacket
{
	// format: (ch)S
	private String _unknown;

	@Override
	public void readImpl()
	{
		_unknown = readS();
	}

	@Override
	public void runImpl()
	{
		System.out.println("Unfinished packet: " + getType() + " / S: " + _unknown);
	}
}