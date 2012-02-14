package l2rt.loginserver.gameservercon.lspackets;

public class TestConnection extends ServerBasePacket
{
	public TestConnection()
	{
		//System.out.println("LS: request sent");
		writeC(0x09);
	}
}