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
package com.l2js.gameserver.handler;

import java.util.Map;
import java.util.logging.Logger;

import javolution.util.FastMap;

import com.l2js.gameserver.handler.actionhandlers.*;
import com.l2js.gameserver.model.L2Object.InstanceType;

public class ActionHandler
{
	protected static Logger _log = Logger.getLogger(ActionHandler.class.getName());

	private Map<InstanceType, IActionHandler> _actions;
	private Map<InstanceType, IActionHandler> _actionsShift;

	public static ActionHandler getInstance()
	{
		return SingletonHolder._instance;
	}

	private ActionHandler()
	{
		_actions = new FastMap<InstanceType, IActionHandler>();
		registerActionHandler(new L2ArtefactInstanceAction());
		registerActionHandler(new L2DecoyAction());
		registerActionHandler(new L2DoorInstanceAction());
		registerActionHandler(new L2ItemInstanceAction());
		registerActionHandler(new L2NpcAction());
		registerActionHandler(new L2PcInstanceAction());
		registerActionHandler(new L2PetInstanceAction());
		registerActionHandler(new L2StaticObjectInstanceAction());
		registerActionHandler(new L2SummonAction());
		registerActionHandler(new L2TrapAction());
		_log.info("Loaded " + size() + "  ActionHandlers");

		_actionsShift = new FastMap<InstanceType, IActionHandler>();
		registerActionShiftHandler(new L2DoorInstanceActionShift());
		registerActionShiftHandler(new L2ItemInstanceActionShift());
		registerActionShiftHandler(new L2NpcActionShift());
		registerActionShiftHandler(new L2PcInstanceActionShift());
		registerActionShiftHandler(new L2StaticObjectInstanceActionShift());
		registerActionShiftHandler(new L2SummonActionShift());
		_log.info("Loaded " + sizeShift() + " ActionShiftHandlers");
	}

	public void registerActionHandler(IActionHandler handler)
	{
		_actions.put(handler.getInstanceType(), handler);
	}

	public void registerActionShiftHandler(IActionHandler handler)
	{
		_actionsShift.put(handler.getInstanceType(), handler);
	}

	public IActionHandler getActionHandler(InstanceType iType)
	{
		IActionHandler result = null;
		for (InstanceType t = iType; t != null; t = t.getParent())
		{
			result = _actions.get(t);
			if (result != null)
				break;
		}
		return result;
	}

	public IActionHandler getActionShiftHandler(InstanceType iType)
	{
		IActionHandler result = null;
		for (InstanceType t = iType; t != null; t = t.getParent())
		{
			result = _actionsShift.get(t);
			if (result != null)
				break;
		}
		return result;
	}

	public int size()
	{
		return _actions.size();
	}

	public int sizeShift()
	{
		return _actionsShift.size();
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final ActionHandler _instance = new ActionHandler();
	}
}