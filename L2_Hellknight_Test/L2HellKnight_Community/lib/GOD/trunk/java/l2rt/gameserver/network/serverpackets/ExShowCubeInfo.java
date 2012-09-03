package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.instancemanager.KrateisCubeManager.EventPlayer;

import javolution.util.FastList;

public final class ExShowCubeInfo extends L2GameServerPacket
{
	private FastList<EventPlayer> _players = new FastList<EventPlayer>();

	public ExShowCubeInfo(FastList<EventPlayer> ep)
	{
		_players = ep;
	}

	@Override
	public void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x89); // Gracia Final
		// writeH(0x88); // Gracia Part 2
		writeD(0x00);
		writeD(_players.size());
		for(EventPlayer ep : _players)
		{
			writeS(ep._player);
			writeD(ep._points);
		}
	}
}