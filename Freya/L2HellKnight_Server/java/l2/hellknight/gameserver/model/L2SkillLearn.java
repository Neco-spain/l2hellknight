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
package l2.hellknight.gameserver.model;

/**
 * This class ...
 *
 * @version $Revision: 1.2.4.2 $ $Date: 2005/03/27 15:29:33 $
 */
public final class L2SkillLearn
{
	// these two build the primary key
	private final int _id;
	private final int _level;
	
	// not needed, just for easier debug
	private final String _name;
	
	private final int _spCost;
	private final int _minLevel;
	private final int _costid;
	private final int _costcount;
	
	private final boolean _learnedByNpc;
	private final boolean _learnedByFs;
	private final boolean _isTransfer;
	private final boolean _isAutoGet;
	
	public L2SkillLearn(int id, int lvl, int minLvl, String name, int cost, int costid, int costcount, boolean npc, boolean fs, boolean transfer, boolean autoget)
	{
		_id = id;
		_level = lvl;
		_minLevel = minLvl;
		_name = name.intern();
		_spCost = cost;
		_costid = costid;
		_costcount = costcount;
		_learnedByNpc = npc;
		_learnedByFs = fs;
		_isTransfer = transfer;
		_isAutoGet = autoget;
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
	
	public int getIdCost()
	{
		return _costid;
	}
	
	public int getCostCount()
	{
		return _costcount;
	}
	
	/**
	 * Return true if skill can be learned by teachers
	 */
	public boolean isLearnedByNPC()
	{
		return _learnedByNpc;
	}
	
	/**
	 * Return true if skill can be learned by forgotten scroll
	 */
	public boolean isLearnedByFS()
	{
		return _learnedByFs;
	}
	
	public boolean isTransferSkill()
	{
		return _isTransfer;
	}
	
	public boolean isAutoGetSkill()
	{
		return _isAutoGet;
	}
}