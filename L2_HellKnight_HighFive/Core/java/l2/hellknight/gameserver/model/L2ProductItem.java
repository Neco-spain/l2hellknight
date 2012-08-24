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

import java.util.ArrayList;

public class L2ProductItem
{
	private final int _productId;
	private final int _category;
	private final int _points;
	private ArrayList<L2ProductItemComponent> _components;

	public L2ProductItem(int productId, int category, int points)
	{
		_productId = productId;
		_category = category;
		_points = points;
	}

	public void setComponents(ArrayList<L2ProductItemComponent> a)
	{
		_components = a;
	}

	public ArrayList<L2ProductItemComponent> getComponents()
	{
		if(_components == null)
		{
			_components = new ArrayList<L2ProductItemComponent>();
		}

		return _components;
	}

	public int getProductId()
	{
		return _productId;
	}

	public int getCategory()
	{
		return _category;
	}

	public int getPoints()
	{
		return _points;
	}
}