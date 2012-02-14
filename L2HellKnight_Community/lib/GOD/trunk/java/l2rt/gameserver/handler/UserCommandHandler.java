package l2rt.gameserver.handler;

import gnu.trove.map.hash.TIntObjectHashMap;

public class UserCommandHandler
{
	private static UserCommandHandler _instance;

	private TIntObjectHashMap<IUserCommandHandler> _datatable;

	public static UserCommandHandler getInstance()
	{
		if(_instance == null)
			_instance = new UserCommandHandler();
		return _instance;
	}

	private UserCommandHandler()
	{
		_datatable = new TIntObjectHashMap<IUserCommandHandler>();
	}

	public void registerUserCommandHandler(IUserCommandHandler handler)
	{
		int[] ids = handler.getUserCommandList();
		for(int element : ids)
			_datatable.put(element, handler);
	}

	public IUserCommandHandler getUserCommandHandler(int userCommand)
	{
		return _datatable.get(userCommand);
	}

	/**
	 * @return
	 */
	public int size()
	{
		return _datatable.size();
	}

	public void clear()
	{
		_datatable.clear();
	}
}
