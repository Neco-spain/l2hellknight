package l2rt.gameserver.network.clientpackets;

public class RequestExOustFromMpccRoom extends L2GameClientPacket
{
	private int unk;

	@Override
	public void runImpl()
	{
		System.out.println(getType() + " :: " + unk);
	}

	/**
	 * format: d
	 */
	@Override
	public void readImpl()
	{
		unk = readD();
	}
}