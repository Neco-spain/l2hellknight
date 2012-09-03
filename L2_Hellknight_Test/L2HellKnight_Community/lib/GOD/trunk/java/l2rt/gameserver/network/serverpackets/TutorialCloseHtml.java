package l2rt.gameserver.network.serverpackets;

public class TutorialCloseHtml extends L2GameServerPacket
{
	@Override
	protected final void writeImpl()
	{
		writeC(0xa9);
	}
}