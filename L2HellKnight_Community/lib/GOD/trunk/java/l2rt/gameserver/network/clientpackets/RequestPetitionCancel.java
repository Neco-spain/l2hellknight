package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.util.Log;

public class RequestPetitionCancel extends L2GameClientPacket
{
	private String _text;

	@Override
	public void readImpl()
	{
		_text = readS(4096);
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		Log.LogPetition(activeChar, 0, "Cancel: " + _text);
	}
}