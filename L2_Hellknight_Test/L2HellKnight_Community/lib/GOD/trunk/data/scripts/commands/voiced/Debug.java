package commands.voiced;

import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.handler.IVoicedCommandHandler;
import l2rt.gameserver.handler.VoicedCommandHandler;
import l2rt.gameserver.model.L2Object;
import l2rt.gameserver.model.L2Player;
import l2rt.util.Log;
import l2rt.util.Util;

public class Debug extends Functions implements IVoicedCommandHandler, ScriptFile
{
	private String[] _commandList = new String[] { "mobDbg", "dbg" };

	public void onLoad()
	{
		VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
	}

	public void onReload()
	{}

	public void onShutdown()
	{}

	public boolean useVoicedCommand(String command, L2Player activeChar, String args)
	{
		command = command.intern();
		if(command.equalsIgnoreCase("mobDbg") || command.equalsIgnoreCase("dbg"))
		{
			L2Object target = activeChar.getTarget();
			if(target == null)
				target = activeChar;
			if(target.isCharacter())
			{
				Log.add(target.dump() + "AI: " + Util.dumpObject(target.getAI(), true, true, true) + "\n ========================================================================================== \n", "mobDbg");
				activeChar.sendMessage(target.getName() + "'s info dumped");
				return true;
			}
		}

		return false;
	}

	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
}