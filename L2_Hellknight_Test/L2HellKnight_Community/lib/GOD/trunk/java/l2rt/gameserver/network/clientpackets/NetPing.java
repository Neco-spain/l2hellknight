package l2rt.gameserver.network.clientpackets;

/**
 * format: ddd
 */
public class NetPing extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int unk, unk2, unk3;

	@Override
	public void runImpl()
	{
	//System.out.println(getType() + " :: " + unk + " :: " + unk2 + " :: " + unk3);
	}

	@Override
	public void readImpl()
	{
		unk = readD();
		unk2 = readD();
		unk3 = readD();
	}
}