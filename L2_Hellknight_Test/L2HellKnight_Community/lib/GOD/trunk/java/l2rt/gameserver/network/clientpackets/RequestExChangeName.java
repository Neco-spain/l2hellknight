package l2rt.gameserver.network.clientpackets;

public class RequestExChangeName extends L2GameClientPacket
{
	@Override
	protected void runImpl()
	{}

	@Override
	protected void readImpl()
	{
		int unk1 = readD();
		String name = readS();
		int unk2 = readD();
		System.out.println(getType() + " | " + name + " | unk1: " + unk1 + " | unk2: " + unk2);
	}
}