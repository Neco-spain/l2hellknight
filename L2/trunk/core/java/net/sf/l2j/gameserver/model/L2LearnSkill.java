/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.model;

/**
 * This class ...
 *
 * @version $Revision: 1.2.4.2 $ $Date: 2005/03/27 15:29:33 $
 */
public final class L2LearnSkill
{
	// these two build the primary key
	private final int _id;
	private final int _level;

	// not needed, just for easier debug
	private final String _name;

	private final int _spCost;
	private final int _minLevel;
	private final int _costid;
	private static int _costcount;
	private final int _bookid;
	private final int _bookid2;

	public L2LearnSkill(int id, int lvl, int minLvl, String name, int cost, int costid, int costcount, int spbId, int spbId2)
	{
		_id = id;
		_level = lvl;
		_minLevel = minLvl;
		_name = name.intern();
		_spCost = cost;
		_costid = costid;
		_costcount = costcount;
		_bookid = spbId;
		_bookid2 = spbId2;
	}

	/**
	 * @return Returns the id.
	 */
	public int getId()
	{
		return _id;
	}

	/**
	 * @return Returns the level.
	 */
	public int getLevel()
	{
		return _level;
	}

	/**
	 * @return Returns the minLevel.
	 */
	public int getMinLevel()
	{
		return _minLevel;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		return _name;
	}

	/**
	 * @return Returns the spCost.
	 */
	public int getSpCost()
	{
		return _spCost;
	}
	public int getCostId()
	{
		return _costid;
	}
	public static int getCostCount()
	{
		return _costcount;
	}
	public int getSpbId()
	{
		return _bookid;
	}
	public int getSpbId2()
	{
		return _bookid2;
	}

	public static int getCostCount(int costid) 
	{
		return 0;
	}
}