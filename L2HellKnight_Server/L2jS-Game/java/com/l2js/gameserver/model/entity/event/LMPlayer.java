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
package com.l2js.gameserver.model.entity.event;

import com.l2js.Config;
import com.l2js.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author L0ngh0rn
 */
public class LMPlayer
{
	private L2PcInstance _player;
	private short _points;
	private short _credits;
	private String _hexCode;
	
	/**
	 * @param player
	 * @param credits
	 */
	public LMPlayer(L2PcInstance player, String hexCode)
	{
		_player = player;
		_points = 0;
		_credits = Config.LM_EVENT_PLAYER_CREDITS;
		_hexCode = hexCode;
	}
	
	/**
	 * @return the _player
	 */
	public L2PcInstance getPlayer()
	{
		return _player;
	}
	
	/**
	 * @param player the _player to set
	 */
	public void setPlayer(L2PcInstance player)
	{
		_player = player;
	}
	
	/**
	 * @return the _credits
	 */
	public short getCredits()
	{
		return _credits;
	}
	
	/**
	 * @param credits the _credits to set
	 */
	public void setCredits(short credits)
	{
		_credits = credits;
	}
	
	/**
	 * Decreases the credits of the player<br>
	 */
	public void decreaseCredits()
	{
		--_credits;
	}
	
	/**
	 * @return the _points
	 */
	public short getPoints()
	{
		return _points;
	}
	
	/**
	 * @param _points the _points to set
	 */
	public void setPoints(short points)
	{
		_points = points;
	}
	
	/**
	 * Decreases the credits of the player<br>
	 */
	public void increasePoints()
	{
		++_points;
	}
	
	/**
	 * @return the _hexCode
	 */
	public String getHexCode()
	{
		return _hexCode;
	}
}
