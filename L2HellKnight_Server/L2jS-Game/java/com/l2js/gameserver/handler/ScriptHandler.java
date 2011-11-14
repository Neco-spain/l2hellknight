/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2js.gameserver.handler;

import gnu.trove.TIntObjectHashMap;

import java.util.logging.Logger;

import com.l2js.Config;

/**
 * @author L0ngh0rn
 */
public class ScriptHandler
{
	private static Logger _log = Logger.getLogger(ScriptHandler.class.getName());
	
	private TIntObjectHashMap<IScriptHandler> _datatable;
	
	public static ScriptHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private ScriptHandler()
	{
		_datatable = new TIntObjectHashMap<IScriptHandler>();
	}
	
	public void registerScriptHandler(IScriptHandler handler)
	{
		String[] ids = handler.getScriptList();
		for (int i = 0; i < ids.length; i++)
		{
			if (Config.DEBUG)
				_log.fine("Adding handler for command " + ids[i]);
			_datatable.put(ids[i].hashCode(), handler);
		}
	}
	
	public IScriptHandler getScriptHandler(String scriptCommand)
	{
		String command = scriptCommand;
		if (scriptCommand.indexOf(" ") != -1)
		{
			command = scriptCommand.substring(0, scriptCommand.indexOf(" "));
		}
		if (Config.DEBUG)
			_log.fine("getting handler for command: " + command + " -> " + (_datatable.get(command.hashCode()) != null));
		return _datatable.get(command.hashCode());
	}
	
	public int size()
	{
		return _datatable.size();
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final ScriptHandler _instance = new ScriptHandler();
	}
}
