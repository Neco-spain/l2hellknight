package l2rt.gameserver.network.clientpackets;

public class RequestBidItemAuction extends L2GameClientPacket
{
	@Override
	protected void runImpl()
	{}

	@Override
	protected void readImpl()
	{
		int obj_id = readD();
		long price = readQ();
		System.out.println(getType() + " | id: " + obj_id + " | amount: " + price);
	}
}