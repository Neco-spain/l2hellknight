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

import javolution.util.FastList;

/**
 * @author Zoey76
 */
public class L2ExtractableSkill
{
	private final int _hash;
	private final FastList<L2ExtractableProductItem> _product;
	
	public L2ExtractableSkill(int hash, FastList<L2ExtractableProductItem> products)
	{
		_hash = hash;
		_product = products;
	}
	
	public int getSkillHash()
	{
		return _hash;
	}
	
	public FastList<L2ExtractableProductItem> getProductItemsArray()
	{
		return _product;
	}
}