package l2rt.gameserver.network.clientpackets;

public class RequestSEKCustom extends L2GameClientPacket
{
	private int SlotNum, Direction;

	@Override
	public void runImpl()
	{
		System.out.println(getType() + " :: SlotNum " + SlotNum + " :: Direction " + Direction);
	}

	/**
	 * format: dd
	 */
	@Override
	public void readImpl()
	{
		SlotNum = readD();
		Direction = readD();
	}
}