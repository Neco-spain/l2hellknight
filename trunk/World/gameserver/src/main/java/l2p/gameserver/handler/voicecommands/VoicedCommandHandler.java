package l2p.gameserver.handler.voicecommands;

import java.util.HashMap;
import java.util.Map;

import l2p.commons.data.xml.AbstractHolder;
import l2p.gameserver.handler.voicecommands.impl.Cfg;
import l2p.gameserver.handler.voicecommands.impl.Debug;
import l2p.gameserver.handler.voicecommands.impl.Hellbound;
import l2p.gameserver.handler.voicecommands.impl.Help;
import l2p.gameserver.handler.voicecommands.impl.Offline;
import l2p.gameserver.handler.voicecommands.impl.Repair;
import l2p.gameserver.handler.voicecommands.impl.ServerInfo;
import l2p.gameserver.handler.voicecommands.impl.Wedding;
import l2p.gameserver.handler.voicecommands.impl.WhoAmI;

public class VoicedCommandHandler extends AbstractHolder
{
	private static final VoicedCommandHandler _instance = new VoicedCommandHandler();

	public static VoicedCommandHandler getInstance()
	{
		return _instance;
	}

	private Map<String, IVoicedCommandHandler> _datatable = new HashMap<String, IVoicedCommandHandler>();

	private VoicedCommandHandler()
	{
		registerVoicedCommandHandler(new Help());
		registerVoicedCommandHandler(new Hellbound());
		registerVoicedCommandHandler(new Cfg());
		registerVoicedCommandHandler(new Offline());
		registerVoicedCommandHandler(new Repair());
		registerVoicedCommandHandler(new ServerInfo());
		registerVoicedCommandHandler(new Wedding());
		registerVoicedCommandHandler(new WhoAmI());
		registerVoicedCommandHandler(new Debug());
	}

	public void registerVoicedCommandHandler(IVoicedCommandHandler handler)
	{
		String[] ids = handler.getVoicedCommandList();
		for(String element : ids)
			_datatable.put(element, handler);
	}

	public IVoicedCommandHandler getVoicedCommandHandler(String voicedCommand)
	{
		String command = voicedCommand;
		if(voicedCommand.indexOf(" ") != -1)
			command = voicedCommand.substring(0, voicedCommand.indexOf(" "));

		return _datatable.get(command);
	}

	@Override
	public int size()
	{
		return _datatable.size();
	}

	@Override
	public void clear()
	{
		_datatable.clear();
	}
}
