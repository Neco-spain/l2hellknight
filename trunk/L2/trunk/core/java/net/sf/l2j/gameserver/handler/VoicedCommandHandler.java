package net.sf.l2j.gameserver.handler;

import java.util.Map;
import java.util.logging.Logger;

import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.*;

public class VoicedCommandHandler
{
	private static Logger _log = Logger.getLogger(ItemHandler.class.getName());

	private static VoicedCommandHandler _instance;

	private Map<String, IVoicedCommandHandler> _datatable;

	public static VoicedCommandHandler getInstance()
	{
		if (_instance == null)
		{
			_instance = new VoicedCommandHandler();
		}
		return _instance;
	}

	private VoicedCommandHandler()
	{
		_datatable = new FastMap<String, IVoicedCommandHandler>();
		registerVoicedCommandHandler(new donate());
			if(Config.ENABLE_MENU)
				registerVoicedCommandHandler(new menu());
			if(Config.L2JMOD_ALLOW_WEDDING)
				registerVoicedCommandHandler(new Wedding());
			if(Config.BANKING_SYSTEM_ENABLED)
				registerVoicedCommandHandler(new Banking());
			_log.config("VoicedCommandHandler: Loaded " + _datatable.size() + " handlers.");
	}

	public void registerVoicedCommandHandler(IVoicedCommandHandler handler)
	{
		String[] ids = handler.getVoicedCommandList();
		for (int i = 0; i < ids.length; i++)
		{
			if (Config.DEBUG) _log.fine("Adding handler for command "+ids[i]);
			_datatable.put(ids[i], handler);
		}
	}

	public IVoicedCommandHandler getVoicedCommandHandler(String voicedCommand)
	{
		String command = voicedCommand;
		if (voicedCommand.indexOf(" ") != -1) 
		{
			command = voicedCommand.substring(0, voicedCommand.indexOf(" "));
		}
		if (Config.DEBUG)
			_log.fine("getting handler for command: "+command+
					" -> "+(_datatable.get(command) != null));
		return _datatable.get(command);
	}

    public int size()
    {
        return _datatable.size();
    }
}
