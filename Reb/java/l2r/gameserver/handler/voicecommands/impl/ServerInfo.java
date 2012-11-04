package l2r.gameserver.handler.voicecommands.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import l2r.gameserver.GameServer;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.scripts.Functions;


public class ServerInfo extends Functions implements IVoicedCommandHandler
{
	private final String[] _commandList = new String[] { "rev", "ver", "date", "time" };

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if(command.equals("rev") || command.equals("ver"))
		{
			activeChar.sendMessage("Revision: " + GameServer.getInstance().getVersion().getRevisionNumber());
			activeChar.sendMessage("Build date: " + GameServer.getInstance().getVersion().getBuildDate());
		}
		else if(command.equals("date") || command.equals("time"))
		{
			activeChar.sendMessage(DATE_FORMAT.format(new Date(System.currentTimeMillis())));
			return true;
		}

		return false;
	}
}
