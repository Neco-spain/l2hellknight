package l2rt.gameserver.loginservercon.gspackets;

public class PointConnection extends GameServerBasePacket
{
	public PointConnection(String player, int point)
	{
		writeC(0x1d);
		writeS(player);
		writeD(point);
	}
}