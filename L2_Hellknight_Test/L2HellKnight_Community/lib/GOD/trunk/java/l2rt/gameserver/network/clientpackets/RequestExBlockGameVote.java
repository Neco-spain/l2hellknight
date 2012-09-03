package l2rt.gameserver.network.clientpackets;

public class RequestExBlockGameVote extends L2GameClientPacket
{
	private int unk, unk2;

	@Override
	public void runImpl()
	{
		System.out.println(getType() + " :: " + unk + " :: " + unk2);
	}

	/**
	 * format: dd
	 */
	@Override
	public void readImpl()
	{
		unk = readD();
		unk2 = readD();
	}
}