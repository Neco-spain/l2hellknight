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
package com.l2js.gameserver.datatables;

/**
 * @author L0ngh0rn
 */
public class CharCustomContainer
{
	private int _value;
	private long _regTime;
	private long _time;
	
	public CharCustomContainer(int value, long regTime, long time)
	{
		_value = value;
		_regTime = regTime;
		_time = time;
	}
	
	public int getValue()
	{
		return _value;
	}
	
	public long getRegTime()
	{
		return _regTime;
	}
	
	public long getTime()
	{
		return _time;
	}
	
	public boolean isActive()
	{
		return (getTime() == 0) || (getRegTime() + getTime() > System.currentTimeMillis());
	}
}