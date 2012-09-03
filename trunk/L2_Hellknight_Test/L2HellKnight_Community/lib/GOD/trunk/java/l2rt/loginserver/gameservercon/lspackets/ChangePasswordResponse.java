package l2rt.loginserver.gameservercon.lspackets;

public class ChangePasswordResponse extends ServerBasePacket
{
	public ChangePasswordResponse(String account, boolean hasChanged)
	{
		writeC(0x06);
		writeS(account);
		writeD(hasChanged ? 1 : 0);
	}
}
