package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.handler.AdminCommandHandler;
import l2rt.gameserver.model.L2Player;

/**
 * This class handles all GM commands triggered by //command
 */
public class SendBypassBuildCmd extends L2GameClientPacket
{
	// format: cS
	public static int GM_MESSAGE = 9;
	public static int ANNOUNCEMENT = 10;

	private String _command;

	@Override
	public void readImpl()
	{
		_command = readS();

		if(_command != null)
			_command = _command.trim();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;

		String cmd = _command;

		if(!cmd.contains("admin_"))
			cmd = "admin_" + cmd;

		AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, cmd);
	}
}