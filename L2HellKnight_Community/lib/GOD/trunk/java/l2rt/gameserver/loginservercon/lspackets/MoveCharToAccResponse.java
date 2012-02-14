package l2rt.gameserver.loginservercon.lspackets;

import l2rt.extensions.multilang.CustomMessage;
import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.loginservercon.AttLS;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.L2GameClient;

/**
 * @Author: Abaddon
 */
public class MoveCharToAccResponse extends LoginServerBasePacket
{
	public MoveCharToAccResponse(byte[] decrypt, AttLS loginServer)
	{
		super(decrypt, loginServer);
	}

	@Override
	public void read()
	{
		String account = readS();
		int changed = readD();

		L2GameClient client = getLoginServer().getCon().getAccountInGame(account);

		if(client == null)
			return;

		L2Player activeChar = client.getActiveChar();

		if(activeChar == null)
			return;

		if(changed == 2)
			Functions.show(new CustomMessage("scripts.commands.user.account.ResultTrue", activeChar), activeChar);
		else if(changed == 1)
			Functions.show(new CustomMessage("scripts.commands.user.account.ResultNoTarget", activeChar), activeChar);
		else if(changed == 0)
			Functions.show(new CustomMessage("scripts.commands.user.account.ResultWrongPass", activeChar), activeChar);
	}
}