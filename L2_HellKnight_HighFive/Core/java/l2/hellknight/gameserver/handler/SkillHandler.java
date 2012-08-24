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
package l2.hellknight.gameserver.handler;

import gnu.trove.map.hash.TIntObjectHashMap;

import l2.hellknight.gameserver.model.skills.L2SkillType;


/**
 * This class ...
 *
 * @version $Revision: 1.1.4.4 $ $Date: 2005/04/03 15:55:06 $
 */
public class SkillHandler
{
	private final TIntObjectHashMap<ISkillHandler> _datatable;
	
	public static SkillHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected SkillHandler()
	{
		_datatable = new TIntObjectHashMap<>();
	}
	
	public void registerHandler(ISkillHandler handler)
	{
		L2SkillType[] types = handler.getSkillIds();
		for (L2SkillType t : types)
		{
			_datatable.put(t.ordinal(), handler);
		}
	}
	
	public ISkillHandler getHandler(L2SkillType skillType)
	{
		return _datatable.get(skillType.ordinal());
	}
	
	/**
	 * @return
	 */
	public int size()
	{
		return _datatable.size();
	}
	
	private static class SingletonHolder
	{
		protected static final SkillHandler _instance = new SkillHandler();
	}
}
