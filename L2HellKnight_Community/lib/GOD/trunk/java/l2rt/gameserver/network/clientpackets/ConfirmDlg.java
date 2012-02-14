package l2rt.gameserver.network.clientpackets;

import l2rt.Config;
import l2rt.gameserver.model.L2Player;

public class ConfirmDlg extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int _messageId, _answer, _requestId;

	@Override
	public void readImpl()
	{
		_messageId = readD();
		_answer = readD();
		_requestId = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		switch(_requestId)
		{
			case 1:
				activeChar.summonCharacterAnswer(_answer);
				break;
			case 2:
				activeChar.reviveAnswer(_answer);
				break;
			case 3:
				activeChar.scriptAnswer(_answer);
				break;
			case 4:
				if(Config.ALLOW_WEDDING && activeChar.isEngageRequest())
					activeChar.engageAnswer(_answer);
				break;
		}
	}
}