package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.L2Player;

public class ExBrPremiumState extends L2GameServerPacket
{
	private static final String _S__FE_BC_EXBRPREMIUMSTATE = "[S] FE:BC ExBrPremiumState";
	private L2Player _activeChar;
	private int _state;

	/**
	 * Если параметр 1 у игрока появляется желтый квадратик вокруг уровня, если что-то другое пропадает.
	 */
	public ExBrPremiumState(L2Player activeChar, int state)
	{
		_activeChar = activeChar;
		_state = state;
	}

	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0xcd);
		writeD(_activeChar.getObjectId());
		writeC(_state);
	}

	@Override
	public String getType()
	{
		return _S__FE_BC_EXBRPREMIUMSTATE;
	}
}
