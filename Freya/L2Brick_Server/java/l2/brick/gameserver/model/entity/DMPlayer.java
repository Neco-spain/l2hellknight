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
package l2.brick.gameserver.model.entity;

import l2.brick.gameserver.model.actor.instance.L2PcInstance;

public class DMPlayer
{
	private L2PcInstance _player;
	private short _points;
	private short _death;
	private String _hexCode;
	
	/**
	 * @param player
	 * @param points
	 */
	public DMPlayer(L2PcInstance player, String hexCode)
	{
		_player = player;
		_points = 0;
		_death = 0;
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
	 * @return the _points
	 */
	public short getPoints()
	{
		return _points;
	}

	/**
	 * @param points the _points to set
	 */
	public void setPoints(short points)
	{
		_points = points;
	}
	
	/**
	 * Increases the points of the player<br>
	 */
	public void increasePoints()
	{
		++_points;
	}

	/**
	 * @return the _death
	 */
	public short getDeath()
	{
		return _death;
	}

	/**
	 * @param death the _death to set
	 */
	public void setDeath(short death)
	{
		_death = death;
	}
	
	/**
	 * Increases the death of the player<br>
	 */
	public void increaseDeath()
	{
		++_death;
	}

	/**
	 * @return the _hexCode
	 */
	public String getHexCode()
	{
		return _hexCode;
	}
	
}
