/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2.brick.gameserver.handler;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastMap;

import l2.brick.gameserver.model.L2Effect;
import l2.brick.gameserver.scripting.L2ScriptEngineManager;

/**
 * @author BiggBoss
 */
public final class EffectHandler
{
	private static final class SingletonHolder
	{
		private static final EffectHandler _instance = new EffectHandler();
	}
	
	private static final Logger _log = Logger.getLogger(EffectHandler.class.getName());
	private final FastMap<Integer, Class<? extends L2Effect>> _handlers;
	
	private EffectHandler()
	{
		_handlers = new FastMap<Integer, Class<? extends L2Effect>>();
	}
	
	public void registerHandler(String name, Class<? extends L2Effect> func)
	{
		_handlers.put(name.hashCode(), func);
	}
	
	public final Class<? extends L2Effect> getHandler(String name)
	{
		return _handlers.get(name.hashCode());
	}
	
	public int size()
	{
		return _handlers.size();
	}
	
	public void executeScript()
	{
		try
		{
			File file = new File(L2ScriptEngineManager.SCRIPT_FOLDER, "handlers/EffectMasterHandler.java");
			L2ScriptEngineManager.getInstance().executeScript(file);
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Problems while running EffectMansterHandler", e);
		}
		_log.config("Loaded " + size() + " Effect handlers");
	}
	
	public static EffectHandler getInstance()
	{
		return SingletonHolder._instance;
	}
}