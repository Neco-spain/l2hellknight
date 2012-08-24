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
package l2.hellknight.gameserver.network.clientpackets;

import l2.hellknight.ExternalConfig;
import l2.hellknight.gameserver.datatables.ItemTable;
import l2.hellknight.gameserver.datatables.ProductItemTable;
import l2.hellknight.gameserver.model.L2ProductItem;
import l2.hellknight.gameserver.model.L2ProductItemComponent;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.items.L2Item;
import l2.hellknight.gameserver.network.serverpackets.*;

public class RequestBrBuyProduct extends L2GameClientPacket
{
	private static final String TYPE = "[C] D0:8C RequestBrBuyProduct";

	private int _productId;
	private int _count;

	@Override
	protected void readImpl()
	{
		_productId = readD();
		_count = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		if(_count > 99 || _count < 0)
		{
			return;
		}

		L2ProductItem product = ProductItemTable.getInstance().getProduct(_productId);
		if(product == null)
		{
			player.sendPacket(new ExBrBuyProduct(ExBrBuyProduct.RESULT_WRONG_PRODUCT));
			return;
		}

		long totalPoints = product.getPoints() * _count;

		if(totalPoints < 0)
		{
			player.sendPacket(new ExBrBuyProduct(ExBrBuyProduct.RESULT_WRONG_PRODUCT));
			return;
		}

		final long gamePointSize = ExternalConfig.GAME_POINT_ITEM_ID == -1 ? player.getgame_points() :  player.getInventory().getInventoryItemCount(ExternalConfig.GAME_POINT_ITEM_ID, -1);

		if(totalPoints > gamePointSize)
		{
			player.sendPacket(new ExBrBuyProduct(ExBrBuyProduct.RESULT_NOT_ENOUGH_POINTS));
			return;
		}

		int totalWeight = 0;
		for(L2ProductItemComponent com : product.getComponents())
		{
			totalWeight += com.getWeight();
		}
		totalWeight *= _count;

		int totalCount = 0;

		for(L2ProductItemComponent com : product.getComponents())
		{
			L2Item item = ItemTable.getInstance().getTemplate(com.getItemId());
			if(item == null)
			{
				player.sendPacket(new ExBrBuyProduct(ExBrBuyProduct.RESULT_WRONG_PRODUCT));
				return;
			}
			totalCount += item.isStackable() ? 1 : com.getCount() * _count;
		}

		if(!player.getInventory().validateCapacity(totalCount) || !player.getInventory().validateWeight(totalWeight))
		{
			player.sendPacket(new ExBrBuyProduct(ExBrBuyProduct.RESULT_INVENTORY_FULL));
			return;
		}

		for (L2ProductItemComponent $comp : product.getComponents())
		{
			player.getInventory().addItem("Buy Product" + _productId, $comp.getItemId(), $comp.getCount() * _count, player, null);
		}

		if(ExternalConfig.GAME_POINT_ITEM_ID == -1)
		{
			player.setgame_points(player.getgame_points() - totalPoints);
		}
		else
		{
			player.getInventory().destroyItemByItemId("Buy Product" + _productId, ExternalConfig.GAME_POINT_ITEM_ID, totalPoints, player, null);
		}

		StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);

		player.sendPacket(new ExBrGamePoint(player));
		player.sendPacket(new ExBrBuyProduct(ExBrBuyProduct.RESULT_OK));
	}

	@Override
	public String getType()
	{
		return TYPE;
	}
}