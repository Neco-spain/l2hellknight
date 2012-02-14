package l2rt.gameserver.network.clientpackets;

public class RequestExBR_EventRankerList extends L2GameClientPacket
{
	private int unk, unk2, unk3;

	@Override
	public void runImpl()
	{
		System.out.println(getType() + " :: " + unk + " :: " + unk2 + " :: " + unk3);
	}

	/**
	 * format: ddd
	 */
	@Override
	public void readImpl()
	{
		unk = readD();
		unk2 = readD();
		unk3 = readD();
	}
}