/*
 * $Header: WorldObjectSet.java, 22/07/2005 14:11:29 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 22/07/2005 14:11:29 $
 * $Revision: 1 $
 * $Log: WorldObjectSet.java,v $
 * Revision 1  22/07/2005 14:11:29  luisantonioa
 * Added copyright notice
 *
 *
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
package com.l2js.util;

import java.util.Iterator;
import java.util.Map;

import javolution.util.FastMap;

import com.l2js.gameserver.model.L2Object;

/**
 * This class ...
 *
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */

public class WorldObjectSet<T extends L2Object> extends L2ObjectSet<T>
{
	private Map<Integer, T> _objectMap;
	
	public WorldObjectSet()
	{
		_objectMap = new FastMap<Integer, T>().shared();
	}

	@Override
	public int size()
	{
		return _objectMap.size();
	}

	@Override
	public boolean isEmpty()
	{
		return _objectMap.isEmpty();
	}
	
	@Override
	public void clear()
	{
		_objectMap.clear();
	}

	@Override
	public void put(T obj)
	{
		_objectMap.put(obj.getObjectId(), obj);
	}

	@Override
	public void remove(T obj)
	{
		_objectMap.remove(obj.getObjectId());
	}

	@Override
	public boolean contains(T obj)
	{
		return _objectMap.containsKey(obj.getObjectId());
	}

	@Override
	public Iterator<T> iterator()
	{
		return _objectMap.values().iterator();
	}
	
}