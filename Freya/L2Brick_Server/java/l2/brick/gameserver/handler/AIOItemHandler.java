package l2.brick.gameserver.handler;

import javolution.util.FastMap;

public class AIOItemHandler
{
	private static FastMap<String, IAIOItemHandler> _aioItemHandlers;
	
	private AIOItemHandler()
	{
		if(_aioItemHandlers == null)
			_aioItemHandlers = new FastMap<String, IAIOItemHandler>();
	}
	
	public static AIOItemHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public void registerAIOItemHandler(IAIOItemHandler handler)
	{
		String handlerBypass = handler.getBypass();
		_aioItemHandlers.put(handlerBypass, handler);
	}
	
	public IAIOItemHandler getAIOHandler(String bypass)
	{
		return _aioItemHandlers.get(bypass);
	}
	
	public int size()
	{
		return _aioItemHandlers.size();
	}
	
	private static final class SingletonHolder
	{
		private static final AIOItemHandler _instance = new AIOItemHandler();
	}
}