package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.L2Character;

/**
 * 0000: 3e 2a 89 00 4c 01 00 00 00                         .|...
 *
 * format   dd
 */
public class ChangeMoveType extends L2GameServerPacket
{
	public static int WALK = 0;
	public static int RUN = 1;

	private int _chaId;
	private boolean _running;

	public ChangeMoveType(L2Character cha)
	{
		_chaId = cha.getObjectId();
		_running = cha.isRunning();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x28);
		writeD(_chaId);
		writeD(_running ? 1 : 0);
		writeD(0); //c2
	}
}