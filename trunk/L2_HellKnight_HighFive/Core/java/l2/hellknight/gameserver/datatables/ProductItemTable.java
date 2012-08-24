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
package l2.hellknight.gameserver.datatables;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.sql.*;
import java.util.*;

import l2.hellknight.L2DatabaseFactory;
import l2.hellknight.gameserver.model.L2ProductItem;
import l2.hellknight.gameserver.model.L2ProductItemComponent;

public class ProductItemTable
{
	private static final Logger _log = Logger.getLogger(ProductItemTable.class.getName());
	private Map<Integer, L2ProductItem> _itemsList = new TreeMap<>();

	public static final ProductItemTable getInstance()
	{
		return SingletonHolder._instance;
	}

	private ProductItemTable()
	{
		load();

		_log.info(String.format("ProductItemTable: loaded %d product item", _itemsList.size()));
	}

	private void load()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM product_items ORDER BY product_id");
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				int productId = rset.getInt("product_id");
				int category = rset.getInt("category");
				int points = rset.getInt("points");

				L2ProductItem pr = new L2ProductItem(productId,category, points);
				pr.setComponents(loadComponents(productId));

				_itemsList.put(productId, pr);
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception: error while loading product items "  + e, e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	private ArrayList<L2ProductItemComponent> loadComponents(int product_id)
	{
		ArrayList<L2ProductItemComponent> a = new ArrayList<>();

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM product_item_components WHERE product_id=" + product_id);
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				int item_id = rset.getInt("item_id");
				int count = rset.getInt("count");

				L2ProductItemComponent component = new L2ProductItemComponent(item_id, count);
				a.add(component);
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception: error while loading product item components for product: " + product_id  + e, e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		return a;
	}


	public Collection<L2ProductItem> getAllItems()
	{
		return _itemsList.values();
	}

	public L2ProductItem getProduct(int id)
	{
		return _itemsList.get(id);
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final ProductItemTable _instance = new ProductItemTable();
	}
}