package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Player;

public class ExUISetting extends L2GameServerPacket
{
	private final byte data[];

	public ExUISetting(Player player)
	{
		data = player.getKeyBindings();
	}

	@Override
	protected void writeImpl()
	{
		writeEx(0x70);
		writeD(data.length);
		writeB(data);
	}
}
