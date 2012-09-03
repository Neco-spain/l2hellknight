package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.L2Summon;

public class SetSummonRemainTime extends L2GameServerPacket
{
	private final int _maxFed;
	private final int _curFed;

	public SetSummonRemainTime(L2Summon summon)
	{
		_curFed = summon.getCurrentFed();
		_maxFed = summon.getMaxFed();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xD1);
		writeD(_maxFed);
		writeD(_curFed);
	}
}