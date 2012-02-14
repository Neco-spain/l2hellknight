package l2rt.gameserver.network.clientpackets;

public class RequestPVPMatchRecord extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{
		System.out.println("Unimplemented packet: " + getType() + " | size: " + _buf.remaining());
	}

	@Override
	public void runImpl()
	{}
}